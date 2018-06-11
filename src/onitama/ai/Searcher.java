package onitama.ai;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

import java.util.ArrayList;
import java.util.List;

import onitama.ai.MoveGenerator.MoveType;
import onitama.common.ILogger;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

/**
 * Features implemented/tried:
 * - Transposition table. By itself this improved search times by around 25%, using a replace-always scheme. Moreover, it makes it possible to store the
 *   best move for each node. Changing this to a depth-preferred scheme gave much better results when the TT was filling up (>25% full), but sometimes
 *   resulted in worse results. Using a two-tier table, storing one depth-preferred entry and one most recent entry, gave the best result. Huge improvements
 *   were seen for searches where the TT was filling up. However, for situations where the TT is sparsely populated, such as fast game play with a large TT,
 *   this does not matter much. An example search from the initial board state with a 29 bit (6 GB) TT searched 22 plies in 10742 seconds and 34754M +
 *   4085M nodes with a replace-always scheme, and 1935 s and 5721M + 730M nodes with the two-tier scheme (>90% populated TT).
 * - Best move. Found during iterative deepening, stored in the TT, and searched first. Resulted in roughly 5x faster search times.
 * - Two best moves. Tried this, and it did not decrease the number of states visited. If anything it did the opposite. Unclear why, I debugged the
 *   implementation and it seemed to work as intended. Possibly the two best moves tend to be similar to each other (such as grabbing a certain
 *   opponent piece, leading to material difference and a high score), and in the case that the first move fails to produce a cut-off, a more different
 *   move is needed.
 * - Changed evaluation function from (piece count & king distance) to (piece count & weighted piece position). This resulted in a 90% win rate against
 *   an AI with the old function. That's a better improvement than increasing the search depth by 1! Adding mobility to the equation did not improve
 *   the win rate.
 * - Quiescence search. If there are pending captures or wins once the horizon node is reached, keep searching until a quiet stage is reached. This resulted
 *   in an 80-90% win rate against an AI without quiescence search with the same nominal depth. However, since it searches more states, it uses a bit more
 *   time. Adjusting for this, i.e. searching at unlimited depth (with iterative deepening) for a fixed period of time per move, the win rate was 63-68%
 *   (times tested were 30, 50, 100 and 1000 ms / move). In general, wins will be detected in at least one depth less. For the default test case in
 *   {@link onitama.tests.TestSingleSearch}, it means that the win is found at depth 12 search in 21 seconds rather than depth 13 search in 41 seconds.
 * - Endgame table. Problematic since there are 131,040 combinations of 5 cards, and it would take a long time to pre-calculate all of them. Possibly a
 *   few background threads can calculate endgames once the 5 cards are known. Searching the default test case to the end (depth 12), we have that 0.02% of
 *   all states analyzed have 2 pieces, 0.25% have 3 pieces, 1.4% have 4 pieces, 4.7% have 5 pieces, and 12.1% have 6 pieces. There are 635,400 possible
 *   board states with 4 pieces, not including card permutations, so it may be feasible to calculate all the endgames up to 4 pieces during runtime, but
 *   likely not more than that. This might not make much of a difference during general game play. (Can try this by extending the depth when the piece
 *   count is small.)
 * - Bitboards for move generation and validation. This resulted in a 4x speedup over iterating over all board squares and moves.
 * - Storing the result from the quiescence search in the TT (or even use the TT for all quiescence nodes). Preliminary testing to store and retrieve
 *   the quiescence scores actually made the search take twice as long. Should experiment more with this, it feels like it could be improved.
 * - Move ordering. Made a huge difference. This lowers the overall branching factor, meaning an exponential savings in nodes visited (bigger saving the
 *   more plies are searched). Most important was capture moves: Instead of trying the best move (from the TT) followed by all other moves unordered, try
 *   the best move, then captures, then non-captures. This resulted in 10-20x less nodes visited overall in the test suite (TestVariousBoardsAndCards).
 *   Visiting winning positions first reduced the number of visited nodes by another 20%. Finally, history heuristic was implemented, which
 *   cut the number of visited nodes in half in the test suite. For a more extensive test searching a 6 piece board to depth 19, the elapsed time
 *   went from 22400 seconds to 23 seconds, with the latter ending up finding the win at move 20 through the TT (the former did not), i.e. it was
 *   at least 1000x times faster, and probably a lot more than that in order to find the same result.
 * - Principal Variation Search. Trivial change, which reduced the number of visited nodes by roughly 10% on average. For some test cases, it barely
 *   made a difference, although for some it cut the number of visited nodes in half. Running AI vs AI tests with a fixed time per move (200, 2000
 *   and 5000 ms) showed a very slight improvement with a 52.5 - 54 % win rate for the PV search version.
 * - Aspiration windows. Experimented with this and did not find that it helped.
 * - Check evasion during quiescence search: this did not help, it lead to quite a bit more nodes searched during the quiescence search, without
 *   finding a win faster.
 * - Dynamic resizing of TT. This allows the TT to change size during the search, carrying over all stored entries. Experiments show that this works
 *   quite well (at least with a two-tiered TT); a search with a small TT that is later adjusted to a larger size, does not seem to suffer in the
 *   long run from initially having started out small. One use case for this feature is to start many searches simultaneously with small TTs, and
 *   increasing the size gradually as some searches finish and there are fewer remaining.
 * - Pondering. Most literature recommends a single search pondering just the most probable opponent move, assuming that this move will actually be
 *   played by the opponent 50+% of the times ("ponder hit rate"). For this project, I have a assumed a much lower ponder hit rate, so instead a
 *   separate search is started for every possible opponent move, and once the opponent moves, all irrelevant search threads are killed. This
 *   feature uses dynamic TT resizing to make efficient use of the available memory.
 * - Parallelization through multiple identical search threads started at the same time with a shared TT: Scaled quite bad, with a speedup of
 *   around 1.2 - 1.25 with 2 - 3 threads (on a 2x4 core system), with no TT locking at all. The number of nodes visited was 60-70% of the single
 *   threaded search.
 *
 * Future ideas:
 * - Compress TT better
 */
public class Searcher {
    public static final int NO_SCORE = 1000; // some score that will never occur, used as an uninitialized value
    private static final int INF_SCORE = 999; // "infinite" alpha beta values
    private static final int TIME_OUT_SCORE = 998; // invalid score indicating that a time out occurred during recursion
    public static final int WIN_SCORE = 500;

    public static final int MAX_DEPTH = 63;

    private static final int EXACT_SCORE = 0;
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 2;

    private static final int INVALID_MOVE_HASH = NN;

    private static final long YIELD_CHECK_FREQUENCY = 100000;

    private final TranspositionTable tt = new TranspositionTable(1);

    /** If this is not 0, the transposition table will be resized to this bit size at the earliest opportunity. */
    private volatile int requestedTTResizeBits = 0;

    private int initialTTBits;

    public final Stats stats;
    private final SearchTimer timer;

    private boolean yield;
    private long nextYieldCheckStateCount = YIELD_CHECK_FREQUENCY;

    private final int nominalDepth;

    private ILogger logger;
    private boolean logEnabled;

    private int initialPlayer;

    private final SearchState state = new SearchState();

    /** Triangular table of principal variations (best moves) for each ply. */
    private int[] pvTable = new int[MAX_DEPTH * (MAX_DEPTH + 1) / 2];
    private int[] pvLength = new int[MAX_DEPTH];

    private int pvScore = NO_SCORE;
    private int pvScoreNominalDepth = -1;
    private int pvScoreLineDepth = -1;

    /** History heuristic table, used for move ordering. */
    private long[][][] historyTable = new long[2][NN][NN];

    private MoveGenerator[] moveGenerator = new MoveGenerator[MAX_DEPTH];

    private int currentDepthSearched;

    public Searcher(int nominalDepth, int ttBits, long maxTimeMs, boolean priority, ILogger logger, boolean logEnabled) {
        this.nominalDepth = nominalDepth;
        this.initialTTBits = ttBits;
        this.yield = priority;
        this.logger = logger;
        this.logEnabled = logEnabled;

        stats = new Stats(tt);
        timer = new SearchTimer(maxTimeMs, 10000);
    }

    public void setState(int playerTurn, String board, CardState cardState) {
        state.initPlayer(playerTurn);
        state.initBoard(board);
        state.initCards(cardState);

        initialPlayer = playerTurn;
        for (int d = 0, player = initialPlayer; d < MAX_DEPTH; ++d, player = 1 - player)
            moveGenerator[d] = new MoveGenerator(state, d, player, historyTable, stats);
    }

    public int start() {
        tt.resizeBlocking(initialTTBits);

        timer.reset();

        log(" depth    time  score  best moves");

        int score = NO_SCORE;
        for (currentDepthSearched = 1; ; ++currentDepthSearched) {
            stats.resetDepthSeen();

            score = negamax(initialPlayer, currentDepthSearched, 0, 0, -INF_SCORE, INF_SCORE);
            if (timer.timeIsUp(stats.getStatesEvaluated()))
                break;

            logMove(true, score);

            if (currentDepthSearched == nominalDepth || Math.abs(score) == WIN_SCORE)
                break;
        }

        return score;
    }

    private int negamax(int player, int depth, int ply, int pvIdx, int alpha, int beta) {
        pvLength[ply] = 0; // default to no pv

        if (state.won(1-player))
            return -WIN_SCORE;

        // depth extensions/reductions would go here

        // end of nominal search depth -- do a queiscence search phase to play out any pending captures and wins
        if (depth == 0)
            return quiesce(player, ply, 0, pvIdx, alpha, beta);

        stats.depthSeen(ply);

        int alphaOrig = alpha;

        if (requestedTTResizeBits > 0 && tt.resize(requestedTTResizeBits))
            requestedTTResizeBits = 0;

        yield(stats.getStatesEvaluated());

        int seenState = tt.get(state.zobrist);
        stats.ttLookup(ply);

        if (seenState != TranspositionTable.NO_ENTRY) {
            int seenDepth = (seenState >> 2) & 63;
            int seenScore = (seenState << 14) >> 22; // bits 8-17 with sign extension, supports range -512..511
            if (seenDepth >= depth || seenScore == WIN_SCORE || seenScore == -WIN_SCORE) {
                // we've visited this exact state before, at the same or earlier move, so we know the score or its bound
                stats.ttHit(ply);

                int seenBoundType = seenState & 3;

                if (seenBoundType == EXACT_SCORE)
                    return seenScore;

                if (seenBoundType == LOWER_BOUND) {
                    if (seenScore > alpha) alpha = seenScore;
                } else { // UPPER_BOUND
                    if (seenScore < beta) beta = seenScore;
                }
                if (alpha >= beta)
                    return seenScore;
            }
        }

        int bestScore = -INF_SCORE;
        int bestMoveHash = INVALID_MOVE_HASH;
        int pvNextIdx = pvIdx + MAX_DEPTH - ply;

        MoveGenerator mg = moveGenerator[ply];
        mg.reset(seenState, MoveType.ALL);

        boolean firstMove = true;
        for (int move; (move = mg.getNextMoveIdx()) != -1; ) {
            stats.stateEvaluated(ply);
            mg.move(move);

            // principal variation search
            int score;
            if (firstMove) {
                score = -negamax(1 - player, depth - 1, ply + 1, pvNextIdx, -beta, -alpha);
                firstMove = false;
            } else {
                score = -negamax(1 - player, depth - 1, ply + 1, pvNextIdx, -alpha - 1, -alpha); // null window search
                if (score > alpha && score < beta) // if it failed high, search again but the entire window
                    score = -negamax(1 - player, depth - 1, ply + 1, pvNextIdx, -beta, -alpha);
            }
            if (timer.timeIsUp(stats.getStatesEvaluated())) return TIME_OUT_SCORE;

            mg.unmove();

            if (score > bestScore) {
                bestScore = score;
                if (bestScore > alpha)
                    alpha = bestScore;

                // update PV so that we can report the best score and the series that leads there
                pvTable[pvIdx] = ((state.cardBits >> 4 + player * 8 + mg.cardUsed[move] * 4) & 15) + (mg.oldPos[move] << 4) + (mg.newPos[move] << 9);
                System.arraycopy(pvTable, pvNextIdx, pvTable, pvIdx + 1, pvLength[ply + 1]);
                pvLength[ply] = pvLength[ply + 1] + 1;

                if (ply == 0) {
                    pvScore = score;
                    pvScoreNominalDepth = currentDepthSearched;
                    pvScoreLineDepth = pvLength[0];

                    logMove(false, score);
                }

                bestMoveHash = getMoveHash(player, mg, move);

                // see if we've reached a state where continued evaluation can not possibly affect the outcome
                if (score == WIN_SCORE)
                    break;

                if (alpha >= beta) {
                    // update history table, indicating that this is a good move (since it's causing a cutoff)
                    // don't include wins or captures in the history, since that is already handled by the move ordering
                    boolean capturedPiece = (state.bitboardPlayer[1-player] & (1 << mg.newPos[move])) != 0;
                    if (!capturedPiece)
                        historyTable[player][mg.oldPos[move]][mg.newPos[move]] += depth * depth; // give less weight to moves near the leaves, or they will dominate the table

                    break;
                }
            }
        }

        int boundType = bestScore <= alphaOrig ? UPPER_BOUND : (bestScore >= beta ? LOWER_BOUND : EXACT_SCORE);
        tt.put(state.zobrist, boundType + (depth << 2) + ((bestScore & 1023) << 8) + (bestMoveHash << 18));

        return bestScore;
    }

    private int quiesce(int player, int ply, int qd, int pvIdx, int alpha, int beta) {
        pvLength[ply] = 0; // default to no pv

        if (state.won(1-player))
            return -WIN_SCORE;

        stats.depthSeen(ply);

        // use current evaluation as a lower bound for the score (a higher score is likely possible by making a move)
        stats.leafEvaluated();
        int standPat = state.score(player);
        if (standPat > alpha)
            alpha = standPat;
        if (alpha >= beta)
            return standPat;

        int pvNextIdx = pvIdx + MAX_DEPTH - ply;

        MoveGenerator mg = moveGenerator[ply];
        mg.reset(TranspositionTable.NO_ENTRY, MoveType.CAPTURE_OR_WIN);

        for (int move; (move = mg.getNextMoveIdx()) != -1; ) {
            stats.quiescenceStateEvaluated(ply);
            mg.move(move);

            // recursive call to find node score
            int score = -quiesce(1 - player, ply + 1, qd + 1, pvNextIdx, -beta, -alpha);
            if (timer.timeIsUp(stats.getStatesEvaluated())) return TIME_OUT_SCORE;

            // undo move
            mg.unmove();

            if (score > alpha) {
                alpha = score;

                pvTable[pvIdx] = ((state.cardBits >> 4 + player * 8 + mg.cardUsed[move] * 4) & 15) + (mg.oldPos[move] << 4) + (mg.newPos[move] << 9);
                System.arraycopy(pvTable, pvNextIdx, pvTable, pvIdx + 1, pvLength[ply + 1]);
                pvLength[ply] = pvLength[ply + 1] + 1;

                if (alpha >= beta)
                    break;
            }
        }

        return alpha;
    }

    /**
     * If running more search threads than processors available, it's important to force the threads to yield regularly
     * to prevent CPU starvation (which notably could affect the UI thread).
     */
    private void yield(long nrStatesVisited) {
        // don't yield too often to prevent too frequent context switches (which hurts performance)
        if (!yield || nrStatesVisited < nextYieldCheckStateCount)
            return;

        Thread.yield();

        nextYieldCheckStateCount = nrStatesVisited + YIELD_CHECK_FREQUENCY;
    }

    private int getMoveHash(int player, MoveGenerator mg, int move) {
        int cardUsed = state.firstCardLower(player) ? mg.cardUsed[move] : 1 - mg.cardUsed[move]; // 0 = lower card id, 1 = higher (card order may differ)
        return mg.oldPos[move] + (cardUsed << 5) + (mg.newPos[move] << 6);
    }

    /** @return Move at the given depth for the current principal variation. */
    private Move getPVMove(int depth) {
        int cardId = pvTable[depth] & 15;
        int p = (pvTable[depth] >> 4) & 31;
        int n = pvTable[depth] >> 9;
        return new Move(Card.CARDS[cardId], p%N, p/N, n%N, n/N, getScore(), getScoreSearchPVLineDepth(), stats.getCompactStats());
    }

    /** The currently best scoring move (the first move of the principal variation). */
    public Move getBestMove() {
        return getPVMove(0);
    }

    /** The currently best estimated score for the game being searched (the result of playing the principal variation). */
    public int getScore() {
        return pvScore;
    }

    /** The nominal search depth used to calculate the {@link #getScore}. */
    public int getScoreSearchNominalDepth() {
        return pvScoreNominalDepth;
    }

    /** The actual depth in the PV line used to calculate the {@link #getScore}, if greater than {@link #getScoreSearchNominalDepth()}. */
    public int getScoreSearchPVLineDepth() {
        return Math.max(pvScoreNominalDepth, pvScoreLineDepth);
    }

    private void log(String str) {
        if (logEnabled)
            logger.logSearch(str);
    }

    private void logMove(boolean depthComplete, int score) {
        if (!logEnabled) return;

        double time = timer.elapsedTimeMs() / 1000.0;
        if (!depthComplete && time < 1)
            return;

        log(getPrincipalVariationMoveString(depthComplete, score));
    }

    public String getPrincipalVariationMoveString(boolean depthComplete, int score) {
        double time = timer.elapsedTimeMs() / 1000.0;

        String timeStr = String.format(time < 10 ? "%7.2f" : "%5.0f s", time);

        StringBuilder pvSb = new StringBuilder();
        for (int d = 0; d < pvLength[0]; ++d) {
            if (d > 0) pvSb.append(" | ");
            pvSb.append(getPVMove(d));
        }

        return String.format("%2d/%2d%2s%s%6d   %s", currentDepthSearched, stats.getMaxDepthSeen(), depthComplete ? "->" : "  ", timeStr, score, pvSb);
    }

    /**
     * Call this method to gracefully stop an ongoing search. The best move found so far can be obtained through {@link #getBestMove()}.
     * This call is not blocking. The search will typically stop within a few milliseconds.
     */
    public void stop() {
        timer.stop();
    }

    /** Suspend (pause) search. Blocking call. Does not return until the search is suspended, which will typically happen within a few milliseconds. */
    public void suspend() {
        timer.suspend();
    }

    /** Resume a paused search. */
    public void resume() {
        timer.resume();
    }

    /** Adjust the timeout for an ongoing search, to let it run for the additional period provided. */
    public void setRelativeTimeout(long remainingTimeMs) {
        timer.setRelativeTimeout(remainingTimeMs);
    }

    /** Set the priority for the searcher thread. Should be false for background threads, like ponder threads. */
    public void setPriority(boolean priority) {
        yield = !priority;
    }

    /** Releases the majority of memory held by this instance (such as the TT). Moves and non-TT related statistics is still available after this call. */
    public void releaseMemory() {
        tt.truncate();
    }

    /**
     * Issues a resize request to the transposition table and returns immediately. The resize will typically happen within a few milliseconds.
     * This is a no-op if the new size is the same as the current size. If the searcher has not yet started searching, this is the size that
     * its TT will be given once the search starts.
     */
    public void resizeTTAsync(int ttBits) {
        initialTTBits = requestedTTResizeBits = ttBits;
    }

    public void logTTSize() {
        log(String.format("Transposition table size: %d entries (%s)", tt.sizeEntries(), tt.sizeFormatted()));
    }

    public void enableLog(boolean enabled) {
        logEnabled = enabled;
    }

    public Pair<int[], int[]> getBitboardsForPrinting() {
        return new Pair<>(state.bitboardPlayer, state.bitboardKing);
    }

    /** Convenience method to get a list of [moves, game states] resulting from each valid move from the search start position. Not optimized for speed. */
    public List<Pair<Move, GameState>> getAllMoves() {
        List<Pair<Move, GameState>> moves = new ArrayList<>();

        MoveGenerator mg = moveGenerator[0];
        mg.reset(TranspositionTable.NO_ENTRY, MoveType.ALL);
        for (int mi; (mi = mg.getNextMoveIdx()) != -1; ) {
            mg.move(mi);

            int op = mg.oldPos[mi], np = mg.newPos[mi];
            Move move = new Move(Card.CARDS[state.cardBits&15], op%N, op/N, np%N, np/N);

            moves.add(new Pair<>(move, AIUtils.getGameState(state)));

            mg.unmove();
        }

        return moves;
    }
}
