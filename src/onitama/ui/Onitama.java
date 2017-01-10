package onitama.ui;

import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Move;

public class Onitama {
    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static final String EXTRA_CARD_NAME = "Extra";

    static final String START_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    Player[] players;

    public static void main(String ... args) throws Exception {
        new Onitama().run();
    }

    void run() {
        Output.printWelcomeMessage();
        CardState cardState = Input.queryStartCards();
        players = Input.queryPlayers();

        GameState game = new GameState(START_BOARD, cardState);

        System.out.println();

        int playerWon;
        for (int m = 0; (playerWon = game.playerWon()) == -1; ++m) {
            Output.printGameState(game);

            int player = m & 1;
            Move move = players[player].getMove(m / 2, game);
            game.move(player, move);
        }

        Output.printGameState(game);

        System.out.printf("Player %d won!", playerWon + 1);
    }

}
