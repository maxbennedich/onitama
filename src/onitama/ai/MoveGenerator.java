package onitama.ai;

import onitama.model.Card;
import onitama.model.GameDefinition;

class MoveGenerator {
    private final int MAX_MOVES = 40;

    final int ply;
    final int player;

    MoveType moveType;

    int[] oldPos = new int[MAX_MOVES], cardUsed = new int[MAX_MOVES], newPos = new int[MAX_MOVES];
    long[] moveScore = new long[MAX_MOVES];

    int totalMoves, movesReturned;

    boolean generatedAllMoves, generatedBestMove;
    int bestMoveOldPos, bestMoveCard, bestMoveNewPos;

    // move order:
    // 1. best move (will always include winning moves thanks to quiescence search)
    // 2. winning moves (king capture and moving king to winning position)
    // 3. piece captures
    // 4. history table heuristic
    final long WIN = Long.MAX_VALUE;
    final long CAPTURE = Long.MAX_VALUE - 1;

    final SearchState state;
    final SearchState prevState = new SearchState();

    final long[][][] historyTable;

    final Stats stats;

    enum MoveType {
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

        // Lazy move generation -- start with just generating the best move (found during previous search in iterative deepening and stored in the TT)
        // This move always needs to be tested, but will hopefully lead to a cut-off, so we don't need to generate the remaining moves.
        // If there is no cut-off, we will at least benefit from the history heuristic updates that this move produces.
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

        long maxScore = -1;
        int move = -1;
        for (int i = 0; i < totalMoves; ++i)
            if (moveScore[i] > maxScore) { maxScore = moveScore[i]; move = i; }
        moveScore[move] = -1; // so that we don't pick this move during the next call to this method
        return move;
    }

    void generateBestMove(int seenState) {
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

    void generateAllMoves() {
        generatedAllMoves = true;
        totalMoves = 0;
        movesReturned = 0;

        for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
            playerBitmask >>= (pz+1);
            if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
            p += pz + 1;

            for (int card = 0; card < GameDefinition.CARDS_PER_PLAYER; ++card) {
                int moveBitmask = Card.CARDS[((state.cardBits >> 4 + player * 8 + card * 4) & 15)].moveBitmask[player][p];

                if (moveType == MoveType.CAPTURE_OR_WIN)
                    moveBitmask &= (state.bitboardPlayer[1-player] | GameDefinition.WIN_BITMASK[player]); // only captures and wins

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
                    if ((state.bitboardKing[1-player] & newPosMask) != 0) moveScore[totalMoves] = WIN; // captured king
                    else if ((state.bitboardKing[player] == (1 << p) && np == GameDefinition.WIN_POSITION[player])) moveScore[totalMoves] = WIN; // moved king to winning position
                    else if ((state.bitboardPlayer[1-player] & newPosMask) != 0) moveScore[totalMoves] = CAPTURE; // captured piece
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