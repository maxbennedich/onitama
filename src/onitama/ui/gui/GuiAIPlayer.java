package onitama.ui.gui;

import onitama.ui.AIPlayer;
import onitama.ui.gui.Gui.AISearchTask;

/** Wrapper around an {@link AIPlayer} and an {@link AISearchTask}, allowing the search and GUI task to be stopped. */
class GuiAIPlayer {
    AIPlayer player;
    AISearchTask moveTask;

    void stopSearch() {
        if (player != null)
            player.stopSearch();

        if (moveTask != null)
            moveTask.stop();
    }

    boolean enabled() {
        return player != null;
    }
}