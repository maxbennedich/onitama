package onitama.model;

import onitama.ai.Searcher;

public class GameDefinition {
    public static final int CARDS_PER_PLAYER = 2;

    public static final int[] WIN_POSITION = { Searcher.N/2, Searcher.N/2 + Searcher.N * (Searcher.N - 1) };

    public static final int[] WIN_BITMASK = {
            0b00000_00000_00000_00000_00100,
            0b00100_00000_00000_00000_00000,
            };
}
