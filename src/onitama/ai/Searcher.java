package onitama.ai;

import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;
import onitama.model.Move;
import onitama.ui.Output;

/**
 * Improvements:
 * - Transposition table. By itself this improved search times by around 25%. Moreover, it makes it possible to store killer moves.
 * - Killer moves. Resulted in roughly 5x faster search times.
 * - Two killer moves. Tried this, and it did not decrease the number of states visited. If anything it did the opposite. Unclear why, I debugged the
 *   implementation and it seemed to work as intended. Possibly the two killer moves tend to be similar to each other (such as grabbing a certain
 *   opponent piece, leading to material difference and a high score), and in the case that the first move fails to produce a cut-off, a more different
 *   move is needed.
 * - Changed evaluation function from (piece count & king distance) to (piece count & weighted piece position). This resulted in a 90% win rate against
 *   an AI with the old function. That's a better improvement than increasing the search depth by 1!
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
 *
 * Ideas:
 * - Generate bit boards for each card/move and for each position.
 * - Optimize entries in TT table (high depth, exact scores)
 * - Pondering
 */
public class Searcher {
    static final int NO_SCORE = 1000; // some score that will never occur
    static final int INF_SCORE = 999; // "infinite" alpha beta values
    static final int TIME_OUT_SCORE = 998; // invalid score indicating that a time out occurred during recursion
    public static final int WIN_SCORE = 100;

    public static final int N = 5; // board dimension
    public static final int NN = N*N; // board dimension

    static final int KING_PIECE = 0;

    static final int MAX_DEPTH = 63;

    static final int EXACT_SCORE = 0;
    static final int LOWER_BOUND = 1;
    static final int UPPER_BOUND = 2;

    final int nominalDepth;
    final int ttBits;

    public static boolean LOGGING = true;

    public Searcher(int nominalDepth, int ttBits) {
        this.nominalDepth = nominalDepth;
        this.ttBits = ttBits;

        tt = new TranspositionTable(ttBits);
        stats = new Stats(tt);
    }

    int initialPlayer;

    int[] bitboardPlayer = { 0, 0 };
    int[] bitboardKing = { 0, 0 };
    long zobrist = 0;

    /** Triangular table of principal variations (best moves) for each ply. */
    int[] pvTable = new int[MAX_DEPTH * (MAX_DEPTH + 1) / 2];
    int[] pvLength = new int[MAX_DEPTH];

    CardState cardState;

    public Stats stats;

    public TranspositionTable tt;

    MoveGenerator[] moveGenerator = new MoveGenerator[MAX_DEPTH];
    public int currentDepthSearched;

    public void setState(int playerTurn, String board, CardState cardState) {
        initPlayer(playerTurn);
        initBoard(board);
        initCards(cardState);
    }

    void initPlayer(int playerTurn) {
        initialPlayer = playerTurn;
        if (initialPlayer == 1)
            zobrist ^= Zobrist.SHIFT_PLAYER; // to make hash values deterministic regardless of initial player

        for (int d = 0, player = initialPlayer; d < MAX_DEPTH; ++d, player = 1 - player)
            moveGenerator[d] = new MoveGenerator(d, player);
    }

    void initCards(CardState cardState) {
        this.cardState = new CardState(cardState.playerCards, cardState.nextCard);

        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                zobrist ^= Zobrist.CARD[p][cardState.playerCards[p][c].id];
    }

    void initBoard(String board) {
        for (int y = 0, bit = 1; y < N; ++y) {
            for (int x = 0; x < N; ++x, bit *= 2) {
                if (board.charAt(y*5+x) != '.') {
                    if (board.charAt(y*5+x) == 'w') { bitboardPlayer[0] |= bit; zobrist ^= Zobrist.PIECE[0][0][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'b') { bitboardPlayer[1] |= bit; zobrist ^= Zobrist.PIECE[1][0][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'W') { bitboardPlayer[0] |= bit; bitboardKing[0] |= bit; zobrist ^= Zobrist.PIECE[0][1][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'B') { bitboardPlayer[1] |= bit; bitboardKing[1] |= bit; zobrist ^= Zobrist.PIECE[1][1][y*5+x]; }
                }
            }
        }
    }

    public static void log(String str) {
        if (LOGGING)
            System.out.println(str);
    }

    Move getPVMove(int depth) {
        int cardId = pvTable[depth] & 15;
        int p = (pvTable[depth] >> 4) & 31;
        int n = pvTable[depth] >> 9;
        return new Move(Card.CARDS[cardId], p%N, p/N, n%N, n/N);
    }

    public Move getBestMove() {
        return getPVMove(0);
    }

    void logMove(boolean depthComplete, int score) {
        if (!LOGGING) return;

        double time = timer.elapsedTimeMs() / 1000.0;
        if (!depthComplete && time < 1)
            return;

        log(getMoveString(depthComplete, score));
    }

    public String getMoveString(boolean depthComplete, int score) {
        double time = timer.elapsedTimeMs() / 1000.0;

        String timeStr = String.format(time < 10 ? "%7.2f" : "%5.0f s", time);

        StringBuilder pvSb = new StringBuilder();
        for (int d = 0; d < pvLength[0]; ++d) {
            Move move = getPVMove(d);
            if (d > 0) pvSb.append(" | ");
            pvSb.append(String.format("%s %c%c-%c%c", move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny));
        }

        return String.format("%2d%2s%s%6d   %s", currentDepthSearched + 1, depthComplete ? "->" : "  ", timeStr, score, pvSb);
    }

    Timer timer;

    class Timer {
        long searchStartTime;
        long maxTimeMs;
        boolean timeUp = false;

        // Check for time-out every this number of states, to prevent calling System.currentTimeMillis() for every node
        private static final long TIMEOUT_CHECK_FREQUENCY_STATES = 10000;

        long nextStatesEvaluated = TIMEOUT_CHECK_FREQUENCY_STATES;

        Timer(long maxTimeMs) {
            searchStartTime = System.currentTimeMillis();
            this.maxTimeMs = maxTimeMs;
        }

        boolean timeIsUp() {
            if (timeUp)
                return true;

            if (stats.getStatesEvaluated() < nextStatesEvaluated)
                return false;

            nextStatesEvaluated = stats.getStatesEvaluated() + TIMEOUT_CHECK_FREQUENCY_STATES;
            return timeUp = elapsedTimeMs() > maxTimeMs;
        }

        long elapsedTimeMs() {
            return System.currentTimeMillis() - searchStartTime;
        }
    }

    public int start(long maxTimeMs) {
        timer = new Timer(maxTimeMs);

        log("depth  time  score  best moves");

        int score = NO_SCORE;
        for (currentDepthSearched = 0; currentDepthSearched < nominalDepth && Math.abs(score) != WIN_SCORE; ++currentDepthSearched) {
            stats.resetDepthSeen();

//          score = negamax(initialPlayer, searchDepth, 99, INF_SCORE);
//          score = negamax(initialPlayer, searchDepth, -INF_SCORE, -99);
            score = negamax(initialPlayer, currentDepthSearched, 0, 0, -INF_SCORE, INF_SCORE);

            if (timer.timeIsUp())
                break;
            logMove(true, score);
        }

        return score;
    }

    int quiesce(int player, int ply, int qd, int pvIdx, int alpha, int beta) {
        pvLength[ply] = 0; // default to no pv

        if (won(1-player))
            return -WIN_SCORE;

        stats.depthSeen(ply);

        // use current evaluation as a lower bound for the score (a higher score is likely possible by making a move)
        int standPat = score(player);
        if (standPat > alpha)
            alpha = standPat;
        if (alpha >= beta)
            return standPat;

        int pvNextIdx = pvIdx + MAX_DEPTH - ply;

        MoveGenerator mg = moveGenerator[ply];
        mg.generate(TranspositionTable.NO_ENTRY, MoveType.CAPTURE_OR_WIN);

        for (int move = 0; move < mg.moves; ++move) {
            stats.quiescenceStateEvaluated(ply);
            mg.move(move);

            // recursive call to find node score
            int score = -quiesce(1 - player, ply + 1, qd + 1, pvNextIdx, -beta, -alpha);
            if (timer.timeIsUp()) return TIME_OUT_SCORE;

            // undo move
            mg.unmove(move);

            if (score > alpha) {
                alpha = score;

                pvTable[pvIdx] = cardState.playerCards[player][mg.cardUsed[move]].id + (mg.oldPos[move] << 4) + (mg.newPos[move] << 9);
                System.arraycopy(pvTable, pvNextIdx, pvTable, pvIdx + 1, pvLength[ply + 1]);
                pvLength[ply] = pvLength[ply + 1] + 1;

                if (alpha >= beta)
                    break;
            }
        }

        return alpha;
    }

    int negamax(int player, int depth, int ply, int pvIdx, int alpha, int beta) {
        pvLength[ply] = 0; // default to no pv

        if (won(1-player))
            return -WIN_SCORE;

        // depth extensions/reductions would go here

        // end of nominal search depth -- do a queiscent search phase to play out any pending captures
        // TODO: is it worth storing/retrieving horizon nodes from the TT, or it is more efficient to reevaluate them?
        if (depth < 0)
            return quiesce(player, ply, 0, pvIdx, alpha, beta);
//        return score(player);

        stats.depthSeen(ply);

        int alphaOrig = alpha;

        int seenState = tt.get(zobrist);
        stats.ttLookup(ply);

        if (seenState != TranspositionTable.NO_ENTRY) {
            int seenDepth = (seenState >> 2) & 63;
            int seenScore = (seenState >> 8) & 255;
            if (seenScore >= 128) seenScore |= ~255; // to support negative numbers
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
        int killerOldPos = NN, killerCard = 0, killerNewPos = 0;

        int pvNextIdx = pvIdx + MAX_DEPTH - ply;

        MoveGenerator mg = moveGenerator[ply];
        mg.generate(seenState, MoveType.ALL);

        for (int move = 0; move < mg.moves; ++move) {
            stats.stateEvaluated(ply);
            mg.move(move);

            // recursive call to find node score
            int score = -negamax(1 - player, depth - 1, ply + 1, pvNextIdx, -beta, -alpha);
            if (timer.timeIsUp()) return TIME_OUT_SCORE;

            // undo move
            mg.unmove(move);

//            if (searchDepth - depth < 1) {
//                String SPACES = "                                                       ";
//                System.out.printf("%sMove %d: Player %d moving piece at %d,%d to %d,%d, using %s, score = %d, bestScore = %d, alpha = %d, beta = %d, alphaOrig = %d%n",
//                        SPACES.substring(0, (searchDepth - depth)*2), searchDepth - depth, player, mg.px, mg.py, nx, ny, cardState.playerCards[player][mg.card].name, score*(player==0?1:-1), bestScore, alpha, beta, alphaOrig);
//            }
//
//            System.out.printf(" --> KILLER candidate (%d): piece=%d, card=%d, move=%d, score=%d%n", searchDepth - depth, mg.piece, playerCards[player][0].id < playerCards[player][1].id ? mg.card : 1 - mg.card, mg.move / 2, score);

            if (score > bestScore) {
                bestScore = score;

                if (bestScore > alpha) {
                    alpha = bestScore;

                    pvTable[pvIdx] = cardState.playerCards[player][mg.cardUsed[move]].id + (mg.oldPos[move] << 4) + (mg.newPos[move] << 9);
                    System.arraycopy(pvTable, pvNextIdx, pvTable, pvIdx + 1, pvLength[ply + 1]);
                    pvLength[ply] = pvLength[ply + 1] + 1;
                }

                if (ply == 0)
                    logMove(false, score);

                killerOldPos = mg.oldPos[move];
                killerCard = cardState.playerCards[player][0].id < cardState.playerCards[player][1].id ? mg.cardUsed[move] : 1 - mg.cardUsed[move]; // 0 = lower card id, 1 = higher (card order may differ)
                killerNewPos = mg.newPos[move];

                // see if we've reached a state where continued evaluation can not possibly affect the outcome
                if (score == WIN_SCORE) {
                    stats.playerWinCutoff(player);
                    break;
                }
                if (alpha >= beta) {
                    stats.alphaBetaCutoff();
                    break;
                }
            }
        }

        int boundType = bestScore <= alphaOrig ? UPPER_BOUND : (bestScore >= beta ? LOWER_BOUND : EXACT_SCORE);
        tt.put(zobrist, boundType + (depth << 2) + ((bestScore & 255) << 8) + (killerOldPos << 16) + (killerCard << 21) + (killerNewPos << 22));
//        System.out.printf(" --> KILLER found (%d): piece=%d, card=%d, move=%d, score=%d%n", searchDepth - depth, killerPiece, killerCard, killerMove, bestScore);

        return bestScore;
    }

    enum MoveType {
        ALL,
        CAPTURE_OR_WIN,
    }

    class MoveGenerator {
        final int ply;
        final int player;

        int[] oldPos = new int[40], cardUsed = new int[40], newPos = new int[40];
        int moves;

        MoveGenerator(int ply, int player) {
            this.ply = ply;
            this.player = player;
        }

        void generate(int seenState, MoveType moveType) {
            moves = 0;

            // add killer move
            int killerOldPos = -1, killerCard = -1, killerNewPos = -1;
            stats.killerMoveLookup(ply);
            if (seenState != TranspositionTable.NO_ENTRY) {
                stats.killerMoveHit(ply);
                oldPos[moves] = killerOldPos = (seenState >> 16) & 31;
                int seenCard = (seenState >> 21) & 1;
                cardUsed[moves] = killerCard = cardState.playerCards[player][0].id < cardState.playerCards[player][1].id ? seenCard : 1 - seenCard; // 0 = lower card id, 1 = higher (card order may differ)
                newPos[moves] = killerNewPos = (seenState >> 22) & 31;
                ++moves;
            }

            for (int playerBitmask = bitboardPlayer[player], p = -1, pz = -1; ; ) {
                playerBitmask >>= (pz+1);
                if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
                p += pz + 1;

                for (int card = 0; card < GameDefinition.CARDS_PER_PLAYER; ++card) {
                    int moveBitmask = cardState.playerCards[player][card].moveBitmask[player][p];

                    if (moveType == MoveType.CAPTURE_OR_WIN)
                        moveBitmask &= (bitboardPlayer[1-player] | GameDefinition.WIN_BITMASK[player]); // only captures and wins

                    moveBitmask &= ~bitboardPlayer[player]; // exclude moves onto oneself

                    for (int np = -1, npz = -1; ; ) {
                        moveBitmask >>= (npz+1);
                        if ((npz = Integer.numberOfTrailingZeros(moveBitmask)) == 32) break;
                        np += npz + 1;

                        if (p == killerOldPos && card == killerCard && np == killerNewPos) continue; // killer move

                        // add move
                        oldPos[moves] = p;
                        cardUsed[moves] = card;
                        newPos[moves] = np;
                        ++moves;
                    }
                }
            }
        }

        // ----------------
        int newPosCopy;
        long prevZobrist;
        int prevBitboardP0, prevBitboardP1;
        int prevBitboardK0, prevBitboardK1;

        void move(int m) {
            newPosCopy = newPos[m];
            int newPosMask = 1 << newPos[m];

            prevZobrist = zobrist;
            prevBitboardP0 = bitboardPlayer[0];
            prevBitboardP1 = bitboardPlayer[1];
            prevBitboardK0 = bitboardKing[0];
            prevBitboardK1 = bitboardKing[1];

            if ((bitboardPlayer[1-player] & newPosMask) != 0) {
                // opponent player piece captured
                int capturedPiece = (bitboardKing[1-player] & newPosMask) != 0 ? 1 : 0;
                if (capturedPiece == 1)
                    bitboardKing[1-player] &= ~newPosMask;

                zobrist ^= Zobrist.PIECE[1 - player][capturedPiece][newPos[m]];
            }

            int posMask = 1 << oldPos[m];

            // remove piece from current position
            bitboardPlayer[player] &= ~posMask;

            // add piece to new position
            bitboardPlayer[player] |= newPosMask;
            bitboardPlayer[1-player] &= ~newPosMask; // if opponent piece taken

            boolean movedKing = bitboardKing[player] == posMask;
            if (movedKing) {
                bitboardKing[player] &= ~posMask;
                bitboardKing[player] |= newPosMask;
            }

            zobrist ^= Zobrist.PIECE[player][movedKing ? 1 : 0][oldPos[m]];
            zobrist ^= Zobrist.PIECE[player][movedKing ? 1 : 0][newPos[m]];

            zobrist ^= Zobrist.CARD[player][cardState.playerCards[player][cardUsed[m]].id];
            zobrist ^= Zobrist.CARD[player][cardState.nextCard.id];

            zobrist ^= Zobrist.SHIFT_PLAYER;

            Card tmpCard = cardState.nextCard;
            cardState.nextCard = cardState.playerCards[player][cardUsed[m]];
            cardState.playerCards[player][cardUsed[m]] = tmpCard;
        }

        void unmove(int m) {
            Card tmpCard = cardState.nextCard;
            cardState.nextCard = cardState.playerCards[player][cardUsed[m]];
            cardState.playerCards[player][cardUsed[m]] = tmpCard;

            bitboardPlayer[0] = prevBitboardP0;
            bitboardPlayer[1] = prevBitboardP1;
            bitboardKing[0] = prevBitboardK0;
            bitboardKing[1] = prevBitboardK1;
            zobrist = prevZobrist;
        }
    }

    public void printBoard() {
        Output.printBoard(bitboardPlayer, bitboardKing);

//        System.out.printf("\n\nScore: %d%n%n", score());
    }

    /** @return Whether the current board is a win for the given player. */
    boolean won(int player) {
        return bitboardKing[1-player] == 0 || bitboardKing[player] == GameDefinition.WIN_BITMASK[player];
    }

    /** Score for each position on the board. (Larger score is better.) */
    private static final int SCORE_1 = 0b01010_10001_00000_10001_01010;
    private static final int SCORE_2 = 0b00100_01010_10001_01010_00100;
    private static final int SCORE_3 = 0b00000_00100_01010_00100_00000;
    private static final int SCORE_4 = 0b00000_00000_00100_00000_00000;

    int score(int playerToEvaluate) {
        stats.leafEvaluated();

        int pieceScore0 =
                Integer.bitCount(bitboardPlayer[0] & SCORE_1) +
                2 * Integer.bitCount(bitboardPlayer[0] & SCORE_2) +
                3 * Integer.bitCount(bitboardPlayer[0] & SCORE_3) +
                4 * Integer.bitCount(bitboardPlayer[0] & SCORE_4);

        int pieceScore1 =
                Integer.bitCount(bitboardPlayer[1] & SCORE_1) +
                2 * Integer.bitCount(bitboardPlayer[1] & SCORE_2) +
                3 * Integer.bitCount(bitboardPlayer[1] & SCORE_3) +
                4 * Integer.bitCount(bitboardPlayer[1] & SCORE_4);

        int score = (Integer.bitCount(bitboardPlayer[0]) - Integer.bitCount(bitboardPlayer[1]))*20 + (pieceScore0 - pieceScore1);

        return score * (playerToEvaluate == 0 ? 1 : -1);
    }
}
