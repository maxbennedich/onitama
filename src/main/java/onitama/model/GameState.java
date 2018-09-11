package onitama.model;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

import onitama.ai.SearchState;

/** A more user friendly version of the {@link SearchState}, mainly used for the UI. */
public class GameState {
    /** To prevent stuck games when AI plays itself. */
    public static int MAX_PLIES_BEFORE_DRAW = 198;

    public static final String INITIAL_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "rrRrr";

    public String board;
    public CardState cardState;
    public int nrPliesPlayed;

    public GameState(CardState cardState) {
        this(INITIAL_BOARD, cardState);
    }

    public GameState(String board, CardState cardState) {
        this.board = board;
        this.cardState = new CardState(cardState.playerCards, cardState.nextCard); // defensive copy
        this.nrPliesPlayed = 0;
    }

    /**
     * Note: This method is meant for the UI only and as such is quite inefficient.
     * @return Whether the move captured an opponent piece.
     */
    public boolean move(int player, Move move) {
        // update board
        int p = move.px + move.py * N;
        char[] boardChars = board.toCharArray();
        boolean capture = boardChars[move.nx + move.ny * N] != '.';
        boardChars[move.nx + move.ny * N] = boardChars[p];
        boardChars[p] = '.';
        board = new String(boardChars);

        // exchange card played
        cardState.playerCards[player][cardState.playerCards[player][0].id == move.card.id ? 0 : 1] = cardState.nextCard;
        cardState.nextCard = move.card;

        ++nrPliesPlayed;

        return capture;
    }

    /**
     * @return -1 if no player won, 0 if player 0 won, 1 if player 1 won.
     * Note: This method is meant for the UI only and as such is quite inefficient.
     */
    public int playerWon() {
        if (board.charAt(N / 2) == 'R') return 0;
        if (board.charAt(NN - 1 - (N / 2)) == 'B') return 1;
        if (board.indexOf('B') == -1) return 0;
        if (board.indexOf('R') == -1) return 1;
        return -1;
    }

    public boolean isDraw() {
        return nrPliesPlayed >= MAX_PLIES_BEFORE_DRAW;
    }

    public boolean gameOver() {
        return playerWon() != -1 || isDraw();
    }

    public int nrMovesPlayed() {
        return (nrPliesPlayed + 1) / 2;
    }

    public CellType cellAt(int x, int y) {
        char c = board.charAt(y * N + x);
        return c == '.' ? EMPTY : c == 'r' ? PLAYER_0 : c == 'b' ? PLAYER_1 : c == 'R' ? PLAYER_0_KING : PLAYER_1_KING;
    }

    private static CellType EMPTY = new CellType(-1, false);
    private static CellType PLAYER_0 = new CellType(0, false);
    private static CellType PLAYER_0_KING = new CellType(0, true);
    private static CellType PLAYER_1 = new CellType(1, false);
    private static CellType PLAYER_1_KING = new CellType(1, true);

    public static class CellType {
        public final int player;
        public final boolean king;

        private CellType(int player, boolean king) {
            this.player = player;
            this.king = king;
        }

        public boolean isEmpty() {
            return player < 0;
        }
    }
}