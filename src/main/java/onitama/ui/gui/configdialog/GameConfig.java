package onitama.ui.gui.configdialog;

import onitama.model.GameState;
import onitama.model.SearchParameters;

/** This class represents the final result of the configuration dialog, ready to be used to set up a new game. */
public class GameConfig {
    public GameState gameState;

    public boolean isAI[] = new boolean[2];

    public SearchParameters[] searchParameters = new SearchParameters[2];

    public boolean[] ponder = new boolean[2];
}