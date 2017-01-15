package onitama.model;

public class GameDefinition {
    public static final int CARDS_PER_PLAYER = 2;

    public static final int[] WIN_BITMASK = {
            0b00000_00000_00000_00000_00100,
            0b00100_00000_00000_00000_00000,
            };
}
