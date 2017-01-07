package onitama;

public class Searcher {
    static final int NO_SCORE = 1000; // some score that will never occur
    static final int INF_SCORE = 999; // "infinite" alpha beta values
    static final int WIN_SCORE = 100;
    static final int N = 5; // board dimension
    static final int NN = N*N; // board dimension
    static final int KING_PIECE = 0;
    static final int CARDS_PER_PLAYER = 2;

    static final int EXACT_SCORE = 0;
    static final int LOWER_BOUND = 1;
    static final int UPPER_BOUND = 2;

    final int maxDepth;
    final int ttBits;

    public static boolean LOGGING = true;

    public Searcher(int maxDepth, int ttBits) {
        this.maxDepth = maxDepth;
        this.ttBits = ttBits;

        tt = new TranspositionTable(ttBits);
    }

    int initialPlayer;

    int boardOccupied = 0;
    long boardPieces = 0;
    long zobrist = 0;

//    byte[][] board = new byte[N][N];
//    boolean[][] pieceAlive = new boolean[2][N];
//    int[][] piecePos = new int[2][N*2];

    // evaluation metrics
    int[] pawnCount = new int[2];
    int[] kingDist = new int[2];

    Card[][] playerCards = new Card[2][CARDS_PER_PLAYER];
    Card nextCard;

//    int[] pieceHistory = new int[256*2];
//    int[] moveHistory = new int[256*2];
//    Card[] passedCardHistory = new Card[256];

    long statesEvaluated = 0;
    public long fullStatesEvaluated = 0;
    long leavesEvaluated = 0;
    long[] playerWinCutoffs = {0, 0};
    long alphaBetaCutoffs = 0;
    long ttLookups = 0, ttHits = 0;

    public TranspositionTable tt;

    MoveState[] moveState;

    public void setState(int playerTurn, String board, Card[][] playerCards, Card nextCard) {
        initPlayer(playerTurn);
        initBoard(board);
        initCards(playerCards, nextCard);
    }

    void initPlayer(int playerTurn) {
        initialPlayer = playerTurn;
        if (initialPlayer == 1)
            zobrist ^= Zobrist.SHIFT_PLAYER; // to make hash values deterministic regardless of initial player
    }

    void initCards(Card[][] playerCards, Card nextCard) {
        this.playerCards = playerCards;
        this.nextCard = nextCard;

        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < CARDS_PER_PLAYER; ++c)
                zobrist ^= Zobrist.CARD[p][playerCards[p][c].id];
    }

    void initBoard(String board) {
/*        pieceCount[0] = pieceCount[1] = 1;
        for (int y = 0; y < N; ++y) {
            pieceAlive[0][y] = pieceAlive[1][y] = true;
            for (int x = 0; x < N; ++x) {
                if (board.charAt(y*5+x) == '.') board[y][x] = 0;
                else if (board.charAt(y*5+x) == 'x') { board[y][x] = (byte)(1 + (pieceCount[0] << 2)); piecePos[0][pieceCount[0]*2] = x; piecePos[0][pieceCount[0]*2+1] = y; ++pieceCount[0]; }
                else if (board.charAt(y*5+x) == '#') { board[y][x] = 1; piecePos[0][0] = x; piecePos[0][1] = y; }
                else if (board.charAt(y*5+x) == 'o') { board[y][x] = (byte)(3 + (pieceCount[1] << 2)); piecePos[1][pieceCount[1]*2] = x; piecePos[1][pieceCount[1]*2+1] = y; ++pieceCount[1]; }
                else if (board.charAt(y*5+x) == 'Q') { board[y][x] = 3; piecePos[1][0] = x; piecePos[1][1] = y; }
            }
        }*/

        pawnCount[0] = pawnCount[1] = 0;

        long piece = 1;
        for (int y = 0, bit = 1; y < N; ++y) {
            for (int x = 0; x < N; ++x, bit *= 2, piece *= 4) {
                if (board.charAt(y*5+x) != '.') {
                    boardOccupied |= bit;
                    if (board.charAt(y*5+x) == 'x') { /* |= 0 not needed */ zobrist ^= Zobrist.PIECE[0][0][y*5+x]; ++pawnCount[0]; }
                    else if (board.charAt(y*5+x) == 'o') { boardPieces |= piece; zobrist ^= Zobrist.PIECE[1][0][y*5+x]; ++pawnCount[1]; }
                    else if (board.charAt(y*5+x) == '#') { boardPieces |= piece*2; zobrist ^= Zobrist.PIECE[0][1][y*5+x]; kingDist[0] = y + Math.abs(N/2 - x); }
                    else if (board.charAt(y*5+x) == 'Q') { boardPieces |= piece*3; zobrist ^= Zobrist.PIECE[1][1][y*5+x]; kingDist[1] = N - 1 - y + Math.abs(N/2 - x); }
                }
            }
        }
    }

    public static void Log(String str) {
        if (LOGGING)
            System.out.println(str);
    }

    String bestMoveString = "N/A";
    int searchDepth = -1;

    void LogMove(int px, int py, int nx, int ny, Card card, int score) {
        bestMoveString = String.format("%d,%d -> %d,%d (%s)", px, py, nx, ny, card.name);
        LogMove(false, score);
    }

    void LogMove(boolean depthComplete, int score) {
        double time = (System.currentTimeMillis() - searchStartTime) / 1000.0;
        if (!depthComplete && time < 1)
            return;

        String timeStr = String.format(time < 10 ? "%7.2f" : "%5.0f s", time);
        Log(String.format("%2d%2s%s%6d   %s", searchDepth + 1, depthComplete ? "->" : "  ", timeStr, score, bestMoveString));
    }

    long searchStartTime;

    public int start(long maxTimeMs) {
        searchStartTime = System.currentTimeMillis();

        Log("depth  time  score  move");

        int score = NO_SCORE;
        for (searchDepth = 0; searchDepth < maxDepth; ++searchDepth) {
            moveState = new MoveState[searchDepth + 1];
            for (int d = searchDepth, player = initialPlayer; d >= 0; --d, player = 1 - player)
                moveState[d] = new MoveState(player);

//            score = negamax(initialPlayer, searchDepth, 99, INF_SCORE);
            score = negamax(initialPlayer, searchDepth, -INF_SCORE, INF_SCORE);

            LogMove(true, score);

            long elapsedTime = System.currentTimeMillis() - searchStartTime;
            if (elapsedTime > maxTimeMs)
                break;
        }

        return score;
    }

    class MoveState {
        final MoveGenerator moveGenerator;

        int posx, posy;
        boolean killedKing, killedPawn, movedKing;
        long prevZobrist;
        int prevBoardOccupied;
        long prevBoardPieces;
        int prevKingDist;

        int pieceX, pieceY;
        Card passedCard;

        MoveState(int player) {
            moveGenerator = new MoveGenerator(player);
        }

        void move(int depth, int player, int card, int m, long piece, int px, int py) {
            int mx = playerCards[player][card].moves[m], my = playerCards[player][card].moves[m + 1];
            if (player == 1) { mx *= -1; my *= -1; }

            posx = px + mx;
            posy = py + my;

            int newPosBit = posx + posy*5;
            int newPosMask = 1 << newPosBit;

            prevZobrist = zobrist;

            boolean newPosOccupied = (boardOccupied & newPosMask) != 0;
            killedKing = false;
            killedPawn = false;

            if (newPosOccupied) {
                int pieceOnNewPos = (int)(boardPieces >> (2*newPosBit));

                // opponent player piece taken
                if ((pieceOnNewPos & 2) != 0) {
                    killedKing = true;
                } else {
                    killedPawn = true;
                    --pawnCount[1-player];
                }

                zobrist ^= Zobrist.PIECE[1 - player][killedKing ? 1 : 0][newPosBit];
            }

            prevBoardOccupied = boardOccupied;
            prevBoardPieces = boardPieces;

            movedKing = (piece & 2) == 2;

            prevKingDist = -1;
            if (movedKing) {
                if (player == 0) { prevKingDist = kingDist[0]; kingDist[0] = posy + Math.abs(N/2 - posx); }
                else { prevKingDist = kingDist[1]; kingDist[1] = N - 1 - posy + Math.abs(N/2 - posx); }
            }

            // remove piece from current position
            int posBit = px + py*5;
            int posMask = 1 << posBit;
            boardOccupied &= ~posMask;
            long pieceMask = 3L << (2*posBit);
            boardPieces &= ~pieceMask;

            // add piece to new position
            boardOccupied |= newPosMask;
            long newPieceMask = 3L << (2*newPosBit);
            boardPieces &= ~newPieceMask;
            boardPieces |= piece << (2*newPosBit);

            pieceX = px;
            pieceY = py;

            zobrist ^= Zobrist.PIECE[player][movedKing ? 1 : 0][posBit];
            zobrist ^= Zobrist.PIECE[player][movedKing ? 1 : 0][newPosBit];

            zobrist ^= Zobrist.CARD[player][playerCards[player][card].id];
            zobrist ^= Zobrist.CARD[player][nextCard.id];

            zobrist ^= Zobrist.SHIFT_PLAYER;

            Card tmpCard = nextCard;
            nextCard = playerCards[player][card];
            playerCards[player][card] = tmpCard;
            passedCard = nextCard;

//          System.out.printf("----------%n%s", getHistory());
//          System.out.printf("Move %d: Player %d moving piece at %d,%d to %d,%d%n", depth, player, px, py, nx, ny);
//          printBoard();

        }

        void unmove(int player, int card) {
            Card tmpCard = nextCard;
            nextCard = playerCards[player][card];
            playerCards[player][card] = tmpCard;

            if (killedPawn)
                ++pawnCount[1-player];

            if (movedKing)
                kingDist[player] = prevKingDist;

            boardOccupied = prevBoardOccupied;
            boardPieces = prevBoardPieces;
            zobrist = prevZobrist;
        }
    }

    int negamax(int player, int depth, int alpha, int beta) {
        if (playerWonPreviousMove(player, depth))
            return -WIN_SCORE;

        // maximum depth reached -- evaluate position and return (don't store/retrieve leaf nodes from the TT, it is more efficient to reevaluate them)
        if (depth < 0)
            return score() * (player == 0 ? 1 : -1);

        int alphaOrig = alpha;

        int seenState = tt.get(zobrist);
        ++ttLookups;

        if (seenState != TranspositionTable.NO_ENTRY) {
            int seenDepth = (seenState >> 2) & 255;
            int seenScore = (seenState >> 10) & 1023;
            if (seenScore >= 512) seenScore |= ~1023; // to support negative numbers
            if (seenDepth >= depth || seenScore == WIN_SCORE || seenScore == -WIN_SCORE) {
                // we've visited this exact state before, at the same or earlier move, so we know the score or its bound
                ++ttHits;

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
        int killerPiece = NN, killerCard = 0, killerMove = 0;

        MoveGenerator mg = moveState[depth].moveGenerator;
//        System.out.printf("depth=%d%n", depth);
        mg.reset(true/*depth >= searchDepth - 2 && true*/, seenState);

        // find all next moves
        for (boolean moreMoves = true; moreMoves; moreMoves = mg.next()) {
            long piece = mg.pieces & 3;

//            System.out.printf("depth = %d, player = %d, piece = %d, card = %d, move = %d%n", depth, mg.player, mg.piece, mg.card, mg.move);
            int mx = playerCards[player][mg.card].moves[mg.move], my = playerCards[player][mg.card].moves[mg.move + 1];
            if (player == 1) { mx *= -1; my *= -1; }

            ++statesEvaluated;

            int nx = mg.px + mx;
            int ny = mg.py + my;

            if (nx < 0 || ny < 0 || nx >= N || ny >= N) continue; // outside board

            int newPosBit = nx + ny*5;
            int newPosMask = 1 << newPosBit;

            boolean newPosOccupied = (boardOccupied & newPosMask) != 0;

            if (newPosOccupied) {
                int pieceOnNewPos = (int)(boardPieces >> (2*newPosBit));
                if ((pieceOnNewPos & 1) == player) continue; // trying to move onto oneself
            }

            ++fullStatesEvaluated;
            moveState[depth].move(depth, player, mg.card, mg.move, piece, mg.px, mg.py);

            // see if we've reached a state where continued evaluation can not possibly affect the outcome
            int score = -negamax(1 - player, depth - 1, -beta, -alpha);

            // undo move
            moveState[depth].unmove(player, mg.card);

//            if (searchDepth - depth <= 1) {
//                String SPACES = "                                                       ";
//                System.out.printf("%sMove %d: Player %d moving piece at %d,%d to %d,%d, using %s, score = %d, bestScore = %d, alpha = %d, beta = %d, alphaOrig = %d%n",
//                        SPACES.substring(0, (searchDepth - depth)*2), searchDepth - depth, player, mg.px, mg.py, nx, ny, playerCards[player][mg.card].name, score*(player==0?1:-1), bestScore, alpha, beta, alphaOrig);
//            }

            if (score > bestScore) {
                if (depth == searchDepth) {
                    LogMove(mg.px, mg.py, nx, ny, playerCards[player][mg.card], score);
//                    System.out.printf(" --> piece=%d, card=%d, move=%d%n", mg.piece, mg.card, mg.move);
                }

                killerPiece = mg.piece;
                killerCard = playerCards[player][0].id < playerCards[player][1].id ? mg.card : 1 - mg.card; // 0 = lower card id, 1 = higher (card order may differ)
                killerMove = mg.move / 2;
                // stored move should be 2, not 0!
//                System.out.printf(" --> KILLER piece=%d, card=%d, move=%d, score=%d%n", mg.piece, mg.card, mg.move, score);

                bestScore = score;
                if (score == WIN_SCORE) {
                    ++playerWinCutoffs[player];
                    break;
                }
                if (bestScore > alpha) alpha = bestScore;
                if (alpha >= beta) {
                    ++alphaBetaCutoffs;
                    break;
                }
            }
        }

        int boundType = bestScore <= alphaOrig ? UPPER_BOUND : (bestScore >= beta ? LOWER_BOUND : EXACT_SCORE);
//        if (Math.abs(bestScore) == 100)
//            System.out.printf("Storing depth=%d, score = %d for zobrist %x%n", depth, bestScore, zobrist);
        tt.put(zobrist, boundType + (depth << 2) + ((bestScore & 1023) << 10) + (killerPiece << 20) + (killerCard << 25) + (killerMove << 26));

        return bestScore;
    }

    class MoveGenerator {
        final int player;

        int occupied;
        long pieces;
        int px, py;

        int piece, card, move;

        int killerMoves;
        int killerPiece, killerCard, killerMove;

        MoveGenerator(int player) {
            this.player = player;
        }

        void reset(boolean kill, int seenState) {
//            System.out.printf("depth=%d, searchdepth=%d, kill=%s, state=%x%n", (seenState >> 2) & 255, searchDepth, kill, seenState);
            ++killerMoveLookups;
            if (kill && seenState != TranspositionTable.NO_ENTRY) {
                ++killerMovesStored;
                killerMoves = 1;
                piece = killerPiece = (seenState >> 20) & 31;
                int seenCard = (seenState >> 25) & 1;
                card = killerCard = playerCards[player][0].id < playerCards[player][1].id ? seenCard : 1 - seenCard; // 0 = lower card id, 1 = higher (card order may differ)
                move = killerMove = ((seenState >> 26) & 3)*2;
//                if (((seenState >> 2) & 255) == 4 && piece == 22 && card == 1 && move == 0)
//                    System.out.printf("Trying killer move at depth %d, piece %d, card %d, move %d%n", (seenState >> 2) & 255, piece, card, move);
                px = piece % 5; py = piece / 5;
                pieces = boardPieces >> (piece * 2);
                occupied = boardOccupied >> piece;
            } else {
                killerMoves = 0;

                occupied = boardOccupied;
                pieces = boardPieces;
                px = py = 0;

                piece = card = move = 0;
                killerPiece = -1;
            }

            moveToNextValidPiece();
//            System.out.printf("Returning piece = %d%n", piece);
        }

        private void moveToNextValidPiece() {
            while ((occupied & 1) == 0 || (pieces & 1) != player) {
                advancePiece();
                if (occupied == 0) { piece = NN; return; } // no more pieces left
            }
        }

        private void advancePiece() {
            ++piece;
            occupied >>= 1;
            pieces >>= 2;
            if (++px == N) { px = 0; ++py; }
        }

        boolean next() {
            if (--killerMoves == 0) {
                occupied = boardOccupied;
                pieces = boardPieces;
                px = py = 0;

                piece = card = move = 0;

                moveToNextValidPiece();
            } else {
                if (piece == NN)
                    return false;

                // advance iterator
                move += 2;
                if (move >= playerCards[player][card].moves.length) {
                    move = 0;
                    ++card;
                    if (card >= CARDS_PER_PLAYER) {
                        card = 0;
                        advancePiece();
                        moveToNextValidPiece();
                    }
                }
            }

            if (piece == killerPiece && card == killerCard && move == killerMove)
                return next();

            return piece < NN;
        }
    }

    char[] markers = new char[] {'•', 'x', 'o', '#', 'Ø'};

    public void printBoard() {
        for (int y = 0, bit = 1, piece = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x, bit *= 2, piece += 2) {
                int c = (boardOccupied & bit) == 0 ? 0 : 1 + ((int)(boardPieces >> piece) & 3);
                System.out.printf("%c", markers[c]);
            }
            System.out.println();
        }
        System.out.printf("Score: %d%n%n", score());
    }

    void printHistory(int depth) {
        System.out.println(getHistory(depth));
    }

    String getHistory(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int d = maxDepth - 1; d >= depth; --d) {
            sb.append(String.format("Move %d: Player %d moving piece at %d,%d to %d,%d, using %s%n", d + 1, d&1,
                    moveState[d].pieceX, moveState[d].pieceY,
                    moveState[d].posx, moveState[d].posy,
                    moveState[d].passedCard.name));
        }
        return sb.toString();
    }

    /** @return Whether the previous move (if such exists) resulted in a win. */
    boolean playerWonPreviousMove(int player, int depth) {
        return depth < searchDepth && (moveState[depth+1].killedKing || (moveState[depth+1].movedKing && moveState[depth+1].posx == N/2 && moveState[depth+1].posy == (N-1)*(1-player)));
    }

    int score() {
        ++leavesEvaluated;

        return (pawnCount[0] - pawnCount[1])*10 + (kingDist[1] - kingDist[0]);
    }

    int killerMoveLookups = 0, killerMovesStored = 0;
    public void printStats() {
        System.out.printf("States evaluated: %d / %d%n", fullStatesEvaluated, statesEvaluated);
        System.out.printf("Leaves evaluated: %d%n", leavesEvaluated);
        System.out.printf("Player win cutoffs: %d / %d%n", playerWinCutoffs[0], playerWinCutoffs[1]);
        System.out.printf("Alpha/beta cutoffs: %d%n", alphaBetaCutoffs);
        System.out.printf("TT hit rate: %.2f %% (%d / %d)%n", 100.0*ttHits/ttLookups, ttHits, ttLookups);
        System.out.printf("TT fill rate: %.2f %%%n", 100.0*tt.usedEntries()/tt.sizeEntries());
        System.out.printf("Killer move hit rate: %.2f %% (%d / %d)%n", 100.0*killerMovesStored/killerMoveLookups, killerMovesStored, killerMoveLookups);
        System.out.printf("Branching factor: %.2f%n", (double)fullStatesEvaluated/killerMoveLookups);
    }
}
