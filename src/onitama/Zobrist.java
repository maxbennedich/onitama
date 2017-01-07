package onitama;

import java.util.Random;

/**
 * Zobrist hashing for creating (almost) unique hash codes for board states.
 */
public class Zobrist {
    static final long[][][] PIECE = new long[2][2][Searcher.NN]; // [player][type][position]
    static final long[][] CARD = new long[2][Card.NR_CARDS];
    static final long SHIFT_PLAYER;

    static {
        Random rnd = new Random(0);

        for (int p = 0; p < 2; ++p) {
            for (int t = 0; t < 2; ++t)
                for (int n = 0; n < Searcher.NN; ++n)
                    PIECE[p][t][n] = rnd.nextLong();

            for (int c = 0; c < Card.NR_CARDS; ++c)
                CARD[p][c] = rnd.nextLong();
        }

        SHIFT_PLAYER = rnd.nextLong();
    }
}