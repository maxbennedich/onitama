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
 *
 * Ideas:
 * - Generate bit boards for each card/move and for each position.
 *
 */
public class Searcher {
    static final int NO_SCORE = 1000; // some score that will never occur
    static final int INF_SCORE = 999; // "infinite" alpha beta values
    static final int TIME_OUT_SCORE = 998; // invalid score indicating that a time out occurred during recursion
    static final int WIN_SCORE = 100;
    public static final int N = 5; // board dimension
    public static final int NN = N*N; // board dimension
    static final int KING_PIECE = 0;

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

    CardState cardState;

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

    public void setState(int playerTurn, String board, CardState cardState) {
        initPlayer(playerTurn);
        initBoard(board);
        initCards(cardState);
    }

    void initPlayer(int playerTurn) {
        initialPlayer = playerTurn;
        if (initialPlayer == 1)
            zobrist ^= Zobrist.SHIFT_PLAYER; // to make hash values deterministic regardless of initial player
    }

    void initCards(CardState cardState) {
        this.cardState = new CardState(cardState.playerCards, cardState.nextCard);

        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                zobrist ^= Zobrist.CARD[p][cardState.playerCards[p][c].id];
    }

    void initBoard(String board) {
/*        pieceCount[0] = pieceCount[1] = 1;
        for (int y = 0; y < N; ++y) {
            pieceAlive[0][y] = pieceAlive[1][y] = true;
            for (int x = 0; x < N; ++x) {
                if (board.charAt(y*5+x) == '.') board[y][x] = 0;
                else if (board.charAt(y*5+x) == 'w') { board[y][x] = (byte)(1 + (pieceCount[0] << 2)); piecePos[0][pieceCount[0]*2] = x; piecePos[0][pieceCount[0]*2+1] = y; ++pieceCount[0]; }
                else if (board.charAt(y*5+x) == 'W') { board[y][x] = 1; piecePos[0][0] = x; piecePos[0][1] = y; }
                else if (board.charAt(y*5+x) == 'b') { board[y][x] = (byte)(3 + (pieceCount[1] << 2)); piecePos[1][pieceCount[1]*2] = x; piecePos[1][pieceCount[1]*2+1] = y; ++pieceCount[1]; }
                else if (board.charAt(y*5+x) == 'B') { board[y][x] = 3; piecePos[1][0] = x; piecePos[1][1] = y; }
            }
        }*/

        pawnCount[0] = pawnCount[1] = 0;

        long piece = 1;
        for (int y = 0, bit = 1; y < N; ++y) {
            for (int x = 0; x < N; ++x, bit *= 2, piece *= 4) {
                if (board.charAt(y*5+x) != '.') {
                    boardOccupied |= bit;
                    if (board.charAt(y*5+x) == 'w') { /* |= 0 not needed */ zobrist ^= Zobrist.PIECE[0][0][y*5+x]; ++pawnCount[0]; }
                    else if (board.charAt(y*5+x) == 'b') { boardPieces |= piece; zobrist ^= Zobrist.PIECE[1][0][y*5+x]; ++pawnCount[1]; }
                    else if (board.charAt(y*5+x) == 'W') { boardPieces |= piece*2; zobrist ^= Zobrist.PIECE[0][1][y*5+x]; kingDist[0] = y + Math.abs(N/2 - x); }
                    else if (board.charAt(y*5+x) == 'B') { boardPieces |= piece*3; zobrist ^= Zobrist.PIECE[1][1][y*5+x]; kingDist[1] = N - 1 - y + Math.abs(N/2 - x); }
                }
            }
        }
    }

    public static void Log(String str) {
        if (LOGGING)
            System.out.println(str);
    }

    String bestMoveString = "N/A";
    public Move bestMove = null;
    int searchDepth = -1;

    void LogMove(int px, int py, int nx, int ny, Card card, int score) {
        bestMoveString = String.format("%d,%d -> %d,%d (%s)", px, py, nx, ny, card.name);
        bestMove = new Move(card, px, py, nx, ny);
        LogMove(false, score);
    }

    void LogMove(boolean depthComplete, int score) {
        double time = timer.elapsedTimeMs() / 1000.0;
        if (!depthComplete && time < 1)
            return;

        String timeStr = String.format(time < 10 ? "%7.2f" : "%5.0f s", time);
        Log(String.format("%2d%2s%s%6d   %s", searchDepth + 1, depthComplete ? "->" : "  ", timeStr, score, bestMoveString));
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

            if (fullStatesEvaluated < nextStatesEvaluated)
                return false;

            nextStatesEvaluated = fullStatesEvaluated + TIMEOUT_CHECK_FREQUENCY_STATES;
            return timeUp = elapsedTimeMs() > maxTimeMs;
        }

        long elapsedTimeMs() {
            return System.currentTimeMillis() - searchStartTime;
        }
    }

    public int start(long maxTimeMs) {
        timer = new Timer(maxTimeMs);

        Log("depth  time  score  move");

        int score = NO_SCORE;
        for (searchDepth = 0; searchDepth < maxDepth; ++searchDepth) {
            moveState = new MoveState[searchDepth + 1];
            for (int d = 0, player = initialPlayer; d <= searchDepth; ++d, player = 1 - player)
                moveState[d] = new MoveState(player);

//          score = negamax(initialPlayer, searchDepth, 99, INF_SCORE);
//          score = negamax(initialPlayer, searchDepth, -INF_SCORE, -99);
            score = negamax(initialPlayer, searchDepth, 0, -INF_SCORE, INF_SCORE);

            if (timer.timeIsUp())
                break;
            LogMove(true, score);
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

        void move(int player, int card, int m, long piece, int px, int py) {
            int mx = cardState.playerCards[player][card].moves[m], my = cardState.playerCards[player][card].moves[m + 1];
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

            zobrist ^= Zobrist.CARD[player][cardState.playerCards[player][card].id];
            zobrist ^= Zobrist.CARD[player][cardState.nextCard.id];

            zobrist ^= Zobrist.SHIFT_PLAYER;

            Card tmpCard = cardState.nextCard;
            cardState.nextCard = cardState.playerCards[player][card];
            cardState.playerCards[player][card] = tmpCard;
            passedCard = cardState.nextCard;

//          System.out.printf("----------%n%s", getHistory());
//          System.out.printf("Move %d: Player %d moving piece at %d,%d to %d,%d%n", depth, player, px, py, nx, ny);
//          printBoard();

        }

        void unmove(int player, int card) {
            Card tmpCard = cardState.nextCard;
            cardState.nextCard = cardState.playerCards[player][card];
            cardState.playerCards[player][card] = tmpCard;

            if (killedPawn)
                ++pawnCount[1-player];

            if (movedKing)
                kingDist[player] = prevKingDist;

            boardOccupied = prevBoardOccupied;
            boardPieces = prevBoardPieces;
            zobrist = prevZobrist;
        }
    }

    int negamax(int player, int depth, int ply, int alpha, int beta) {
        if (playerWonPreviousMove(player, ply))
            return -WIN_SCORE;

        // no remaining depth to search -- evaluate position and return (don't store/retrieve leaf nodes from the TT, it is more efficient to reevaluate them)
        if (depth < 0)
            return score() * (player == 0 ? 1 : -1);

        int alphaOrig = alpha;

        int seenState = tt.get(zobrist);
        ++ttLookups;

        if (seenState != TranspositionTable.NO_ENTRY) {
            int seenDepth = (seenState >> 2) & 63;
            int seenScore = (seenState >> 8) & 255;
            if (seenScore >= 128) seenScore |= ~255; // to support negative numbers
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

        MoveGenerator mg = moveState[ply].moveGenerator;
        mg.reset(seenState);

        // find all next moves
        for (boolean moreMoves = true; moreMoves; moreMoves = mg.next()) {
            long piece = mg.pieces & 3;

//            System.out.printf("depth = %d, player = %d, piece = %d, card = %d, move = %d%n", depth, mg.player, mg.piece, mg.card, mg.move);
            int mx = cardState.playerCards[player][mg.card].moves[mg.move], my = cardState.playerCards[player][mg.card].moves[mg.move + 1];
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
            moveState[ply].move(player, mg.card, mg.move, piece, mg.px, mg.py);

            // recursive call to find node score
            int score = -negamax(1 - player, depth - 1, ply + 1, -beta, -alpha);
            if (timer.timeIsUp()) return TIME_OUT_SCORE;

            // undo move
            moveState[ply].unmove(player, mg.card);

//            if (searchDepth - depth < 1) {
//                String SPACES = "                                                       ";
//                System.out.printf("%sMove %d: Player %d moving piece at %d,%d to %d,%d, using %s, score = %d, bestScore = %d, alpha = %d, beta = %d, alphaOrig = %d%n",
//                        SPACES.substring(0, (searchDepth - depth)*2), searchDepth - depth, player, mg.px, mg.py, nx, ny, cardState.playerCards[player][mg.card].name, score*(player==0?1:-1), bestScore, alpha, beta, alphaOrig);
//            }
//
//            System.out.printf(" --> KILLER candidate (%d): piece=%d, card=%d, move=%d, score=%d%n", searchDepth - depth, mg.piece, playerCards[player][0].id < playerCards[player][1].id ? mg.card : 1 - mg.card, mg.move / 2, score);

            // store 2 killer moves

            if (score > bestScore) {
                bestScore = score;

                if (ply == 0) {
                    LogMove(mg.px, mg.py, nx, ny, cardState.playerCards[player][mg.card], score);
//                    System.out.printf(" --> piece=%d, card=%d, move=%d%n", mg.piece, mg.card, mg.move);
                }

                killerPiece = mg.piece;
                killerCard = cardState.playerCards[player][0].id < cardState.playerCards[player][1].id ? mg.card : 1 - mg.card; // 0 = lower card id, 1 = higher (card order may differ)
                killerMove = mg.move / 2;

                // see if we've reached a state where continued evaluation can not possibly affect the outcome
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
        tt.put(zobrist, boundType + (depth << 2) + ((bestScore & 255) << 8) + (killerPiece << 16) + (killerCard << 21) + (killerMove << 22));
//        System.out.printf(" --> KILLER found (%d): piece=%d, card=%d, move=%d, score=%d%n", searchDepth - depth, killerPiece, killerCard, killerMove, bestScore);

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

        void reset(int seenState) {
//            System.out.printf("depth=%d, searchdepth=%d, kill=%s, state=%x%n", (seenState >> 2) & 255, searchDepth, kill, seenState);
            ++killerMoveLookups;
            if (seenState != TranspositionTable.NO_ENTRY) {
                ++killerMovesStored;
                killerMoves = 1;
                piece = killerPiece = (seenState >> 16) & 31;
                int seenCard = (seenState >> 21) & 1;
                card = killerCard = cardState.playerCards[player][0].id < cardState.playerCards[player][1].id ? seenCard : 1 - seenCard; // 0 = lower card id, 1 = higher (card order may differ)
                move = killerMove = ((seenState >> 22) & 3)*2;
//                if (((seenState >> 2) & 255) == 4 && piece == 22 && card == 1 && move == 0)
//                    System.out.printf("Trying killer move at depth %d, piece %d, card %d, move %d%n", (seenState >> 2) & 255, piece, card, move);
//                if (((seenState >> 2) & 255) == searchDepth-1) { piece = killerPiece = 7; card = killerCard = 0; move = killerMove = 0; /*System.out.printf("Trying killer move at depth %d, piece %d, card %d, move %d%n", (seenState >> 2) & 255, piece, card, move);*/ }
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
                if (move >= cardState.playerCards[player][card].moves.length) {
                    move = 0;
                    ++card;
                    if (card >= GameDefinition.CARDS_PER_PLAYER) {
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

    public void printBoard() {
        Output.printBoard(boardOccupied, boardPieces);

//        System.out.printf("\n\nScore: %d%n%n", score());
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
    boolean playerWonPreviousMove(int player, int ply) {
        return ply > 0 && (moveState[ply-1].killedKing || (moveState[ply-1].movedKing && moveState[ply-1].posx == N/2 && moveState[ply-1].posy == (N-1)*(1-player)));
    }

    /** Score for each position on the board. (Larger score is better.) */
    private static final int[] BOARD_SCORE = new int[] {
            0,1,2,1,0,
            1,2,3,2,1,
            2,3,4,3,2,
            1,2,3,2,1,
            0,1,2,1,0,
    };

    int score() {
        ++leavesEvaluated;

        int pieceScore[] = new int[2];

        int occupied = boardOccupied;
        long pieces = boardPieces;
        int px = 0, py = 0;

        for (int p = 0; p < NN; ++p) {
            if ((occupied & 1) == 1) {
                long piece = pieces & 3;
                int player = (int)(piece & 1);
                pieceScore[player] += BOARD_SCORE[p];
            }
            occupied >>= 1;
            if (occupied == 0) break; // no more pieces left
            pieces >>= 2;
            if (++px == N) { px = 0; ++py; }
        }
        return (pawnCount[0] - pawnCount[1])*20 + (pieceScore[0] - pieceScore[1]);

//        return (pawnCount[0] - pawnCount[1])*10 + (kingDist[1] - kingDist[0]);
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
