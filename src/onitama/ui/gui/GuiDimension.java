package onitama.ui.gui;

import static onitama.model.GameDefinition.N;

/** Helper class with various hard coded GUI dimensions (such as the move log window that needs to be a certain width to fit move strings). */
public class GuiDimension {
    public static final int MOVE_LOG_WIDTH = 260;
    public static final int PONDER_STATS_WIDTH = 235;
    public static final int LOG_HEIGHT = 325;

    public static final int BOARD_CELL_WIDTH = 60;
    public static final int BOARD_CELL_SPACING = 5;

    public static final int BOARD_SIZE = BOARD_CELL_WIDTH * N + BOARD_CELL_SPACING * (N + 1);

    public static final int CARD_CELL_WIDTH = 12;
    public static final int CARD_CELL_SPACING = 2;
    public static final int SPACE_BETWEEN_PLAYER_CARDS = 40;

    public static final int CONFIG_CARD_CELL_WIDTH = 10;
    public static final int CONFIG_CARD_CELL_SPACING = 1;
}