package onitama.model;

public class GameDefinition {
    public static final int NR_PLAYERS = 2;
    public static final int CARDS_PER_PLAYER = 2;
    public static final int CARDS_PER_GAME = CARDS_PER_PLAYER * NR_PLAYERS + 1;

    /** Pawn, King */
    public static final int NR_PIECE_TYPES = 2;

    /** Board dimension */
    public static final int N = 5;

    /** Board dimension squared */
    public static final int NN = N*N;

    /** Used as display name, e.g. "Red player to move". */
    public static final String[] PLAYER_COLOR = { "Red", "Blue" };

    public static final int[] WIN_POSITION = { N/2, N/2 + N * (N - 1) };

    public static final int[] WIN_BITMASK = {
            0b00000_00000_00000_00000_00100,
            0b00100_00000_00000_00000_00000,
            };

    public static String getPosition(int n) {
        return getPosition(n % N, n / N);
    }

    public static String getPosition(int x, int y) {
        return (char)('a'+x) + "" +  (char)('5'-y);
    }
}
