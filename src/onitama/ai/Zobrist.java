package onitama.ai;

import static onitama.model.GameDefinition.NN;

import java.util.Random;

import onitama.model.GameDefinition;

/**
 * Zobrist hashing for creating (almost) unique hash codes for board states.
 */
public class Zobrist {
    static final long[][][] PIECE = new long[2][2][NN]; // [player][type][position]
    static final long[][] CARD = new long[2][GameDefinition.CARDS_PER_PLAYER * 2 + 1];
    static final long SHIFT_PLAYER;

    static {
        Random rnd = new Random(0);

        for (int p = 0; p < 2; ++p) {
            for (int t = 0; t < 2; ++t)
                for (int n = 0; n < NN; ++n)
                    PIECE[p][t][n] = rnd.nextLong();

            for (int c = 0; c < CARD[p].length; ++c)
                CARD[p][c] = rnd.nextLong();
        }

        SHIFT_PLAYER = rnd.nextLong();
    }
}