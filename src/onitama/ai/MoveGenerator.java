package onitama.ai;

import static onitama.model.GameDefinition.CARDS_PER_PLAYER;
import static onitama.model.GameDefinition.WIN_BITMASK;
import static onitama.model.GameDefinition.WIN_POSITION;

import onitama.model.Card;

/**
 * This class is responsible for generating and ordering the possible moves from a given game state. There will be one
 * instance of this class per ply, to support depth-first searching of the game tree.
 * <p>
 * Moves are ordered as follows:
 * <ol>
 * <li>Best move, if available. This is the best move found during a more shallow search in iterative deepening and
 *     stored in the TT. (Note: it is ok to put this before winning moves, since the best move will always be the
 *     winning move, if such exists, thanks to the quiescence search.)</li>
 * <li>Winning moves; capturing the opponent's king, or moving the king to the winning position.</li>
 * <li>Piece captures.</li>
 * <li>History table heuristic. Moves are ordered by how often they produce non-capture, non-winning alpha-beta
 *     cutoffs.</li>
 * </ol>
 * <p>
 * <b>Implementation detail:</b>
 * Thanks to alpha-beta pruning and efficient move ordering, we typically only need to test ~2 moves on average before
 * a cutoff, out of typically 10-20 possible moves at any state. Therefore, this class implements lazy move generation
 * and lazy move ordering. Firstly, if available, only the best move is returned, and no moves are generated at all. If
 * the best move did not result in a cutoff, all legal moves are generated and scored. (Note that scoring all moves
 * after the best move has been evaluated has the added benefit of including history heuristic updates from the best
 * move.) A selection type of sort is used to fetch next moves as they are needed, to avoid sorting all moves when on
 * average only 1 or 2 will be needed.
 */
class MoveGenerator {
    private static final int MAX_MOVES = 40;

    private static final long WIN_SCORE = Long.MAX_VALUE;
    private static final long CAPTURE_SCORE = Long.MAX_VALUE - 1;

    private final int ply;
    private final int player;

    private MoveType moveType;

    int[] oldPos = new int[MAX_MOVES], cardUsed = new int[MAX_MOVES], newPos = new int[MAX_MOVES];
    private long[] moveScore = new long[MAX_MOVES];

    private int totalMoves, movesReturned;

    private boolean generatedAllMoves, generatedBestMove;
    private int bestMoveOldPos, bestMoveCard, bestMoveNewPos;

    private final SearchState state;
    private final SearchState prevState = new SearchState();

    private final long[][][] historyTable;

    private final Stats stats;

    static enum MoveType {
        ALL,
        CAPTURE_OR_WIN,
    }

    MoveGenerator(SearchState state, int ply, int player, long[][][] historyTable, Stats stats) {
        this.state = state;
        this.ply = ply;
        this.player = player;
        this.historyTable = historyTable;
        this.stats = stats;
    }

    void reset(int seenState, MoveType moveType) {
        this.moveType = moveType;
        generatedAllMoves = false;

        // Lazy move generation; start with just generating the best move, hopefully it will lead to a cutoff
        generateBestMove(seenState);
    }

    int getNextMoveIdx() {
        if (generatedBestMove) {
            generatedBestMove = false;
            return 0;
        }

        if (!generatedAllMoves)
            generateAllMoves();

        if (++movesReturned > totalMoves)
            return -1;

        // lazy move sorting
        long maxScore = -1;
        int move = -1;
        for (int i = 0; i < totalMoves; ++i)
            if (moveScore[i] > maxScore) { maxScore = moveScore[i]; move = i; } // TODO: try fetching the score straight from the history table
        moveScore[move] = -1; // so that we don't pick this move during the next call to this method

        // exchange [move] with [idx] ...
        return move;
    }

    private void generateBestMove(int seenState) {
        stats.bestMoveLookup(ply);
        if (seenState != TranspositionTable.NO_ENTRY) {
            stats.bestMoveHit(ply);
            oldPos[0] = bestMoveOldPos = (seenState >> 18) & 31;
            int seenCard = (seenState >> 23) & 1;
            cardUsed[0] = bestMoveCard = state.firstCardLower(player) ? seenCard : 1 - seenCard; // 0 = lower card id, 1 = higher (card order may differ)
            newPos[0] = bestMoveNewPos = (seenState >> 24) & 31;
            generatedBestMove = true;
        } else {
            bestMoveOldPos = bestMoveCard = bestMoveNewPos = -1;
            generatedBestMove = false;
        }
    }

    private void generateAllMoves() {
        generatedAllMoves = true;
        totalMoves = 0;
        movesReturned = 0;

        for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
            playerBitmask >>= (pz+1);
            if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
            p += pz + 1;

            for (int card = 0; card < CARDS_PER_PLAYER; ++card) {
                int moveBitmask = Card.CARDS[((state.cardBits >> 4 + player * 8 + card * 4) & 15)].moveBitmask[player][p];

                if (moveType == MoveType.CAPTURE_OR_WIN)
                    moveBitmask &= (state.bitboardPlayer[1-player] | WIN_BITMASK[player]); // only captures and wins

                moveBitmask &= ~state.bitboardPlayer[player]; // exclude moves onto oneself

                for (int np = -1, npz = -1; ; ) {
                    moveBitmask >>= (npz+1);
                    if ((npz = Integer.numberOfTrailingZeros(moveBitmask)) == 32) break;
                    np += npz + 1;

                    if (p == bestMoveOldPos && card == bestMoveCard && np == bestMoveNewPos) continue;

                    // add move
                    oldPos[totalMoves] = p;
                    cardUsed[totalMoves] = card;
                    newPos[totalMoves] = np;

                    int newPosMask = 1 << np;
                    if ((state.bitboardKing[1-player] & newPosMask) != 0) moveScore[totalMoves] = WIN_SCORE; // captured king
                    else if ((state.bitboardKing[player] == (1 << p) && np == WIN_POSITION[player])) moveScore[totalMoves] = WIN_SCORE; // moved king to winning position
                    else if ((state.bitboardPlayer[1-player] & newPosMask) != 0) moveScore[totalMoves] = CAPTURE_SCORE; // captured piece
                    else moveScore[totalMoves] = historyTable[player][p][np];

                    ++totalMoves;
                }
            }
        }
    }

    void move(int m) {
        prevState.copyFrom(state);
        state.move(player, oldPos[m], newPos[m], cardUsed[m]);
    }

    void unmove() {
        state.copyFrom(prevState);
    }
}