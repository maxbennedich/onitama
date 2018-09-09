package onitama.ui.gui;

/** Helper class with colors used in the GUI. */
public class GuiColor {
    public static final String BOARD_SPACING = "000";

    // [player / no piece][selected]
    public static final String BOARD_PIECE[][] = {{ "f40", "fb0" }, { "36f", "7cf" }, { "fff", "ccc" }};

    // [enabled / disabled]
    public static final String[] CARD_CENTER = { "555", "444" };
    public static final String[] CARD_LEGAL_MOVE = { "7a7", "696" };
    public static final String[] CARD_NO_MOVE = { "fff", "ddd" };

    public static final String CARD_SELECTED_BACKGROUND = "ed0";

    // [player / no piece]
    public static final String[] CONFIG_CARD_SELECTED_BACKGROUND = { "f62", "59f", "ed0" };

    // [player / equal]
    public static final String[] EVALUATION_SCORE = { "f40", "36f", "000" };
}