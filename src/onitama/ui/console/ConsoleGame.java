package onitama.ui.console;

import onitama.model.CardState;
import onitama.model.GameState;
import onitama.ui.Player;

public class ConsoleGame {
    static final String EXTRA_CARD_NAME = "Extra";

    public static void launch() {
        Output.printWelcomeMessage();
        CardState cardState = Input.queryStartCards();
        Player[] players = Input.queryPlayers();
        Output.println();

        GameSimulator game = new GameSimulator(players, new GameState(cardState));
        game.play();
    }
}
