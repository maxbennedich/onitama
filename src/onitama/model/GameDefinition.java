package onitama.model;

public class GameDefinition {
    public static final int CARDS_PER_PLAYER = 2;

    /** Board dimension */
    public static final int N = 5;

    /** Board dimension squared */
    public static final int NN = N*N;

    public static final int[] WIN_POSITION = { N/2, N/2 + N * (N - 1) };

    public static final int[] WIN_BITMASK = {
            0b00000_00000_00000_00000_00100,
            0b00100_00000_00000_00000_00000,
            };
}
