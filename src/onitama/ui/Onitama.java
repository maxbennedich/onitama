package onitama.ui;

import onitama.model.CardState;
import onitama.model.GameState;

public class Onitama {
    static final String EXTRA_CARD_NAME = "Extra";

    private static final String START_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    private Player[] players;

    public static void main(String ... args) throws Exception {
        new Onitama().run();
    }

    private void run() {
        Output.printWelcomeMessage();
        CardState cardState = Input.queryStartCards();
        players = Input.queryPlayers();
        Output.println();

        GameSimulator game = new GameSimulator(players, new GameState(START_BOARD, cardState));
        game.play();
    }

}
