package onitama.ui.gui.configdialog;

import onitama.model.GameState;
import onitama.model.SearchParameters;

import static onitama.model.GameDefinition.NR_PLAYERS;

/** This class represents the final result of the configuration dialog, ready to be used to set up a new game. */
public class GameConfig {
    public GameState gameState;

    public boolean isAI[] = new boolean[NR_PLAYERS];

    public SearchParameters[] searchParameters = new SearchParameters[NR_PLAYERS];

    public boolean[] ponder = new boolean[NR_PLAYERS];

    public int startingPlayer;
}