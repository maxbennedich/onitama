package onitama.model;

import onitama.ai.Searcher;

public class GameState {
    public String board;
    public CardState cardState;

    public GameState(String board, CardState cardState) {
        this.board = board;
        this.cardState = cardState;
    }

    /**
     * Note: This method is meant for the UI only and as such is quite inefficient.
     */
    public void move(int player, Move move) {
        // update board
        int p = move.px + move.py * Searcher.N;
        char[] boardChars = board.toCharArray();
        boardChars[move.nx + move.ny * Searcher.N] = boardChars[p];
        boardChars[p] = '.';
        board = new String(boardChars);

        // exchange card played
        cardState.playerCards[player][cardState.playerCards[player][0].id == move.card.id ? 0 : 1] = cardState.nextCard;
        cardState.nextCard = move.card;
    }

    /**
     * @return -1 if no player won, 0 if player 0 won, 1 if player 1 won.
     * Note: This method is meant for the UI only and as such is quite inefficient.
     */
    public int playerWon() {
        if (board.charAt(Searcher.N / 2) == 'W') return 0;
        if (board.charAt(Searcher.NN - 1 - (Searcher.N / 2)) == 'B') return 1;
        if (board.indexOf('B') == -1) return 0;
        if (board.indexOf('W') == -1) return 1;
        return -1;
    }
}