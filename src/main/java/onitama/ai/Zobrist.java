package onitama.ai;

import static onitama.model.GameDefinition.CARDS_PER_GAME;
import static onitama.model.GameDefinition.NN;
import static onitama.model.GameDefinition.NR_PIECE_TYPES;
import static onitama.model.GameDefinition.NR_PLAYERS;

import java.util.Random;

/**
 * Zobrist hashing for creating (almost) unique hash codes for board states.
 */
public class Zobrist {
    static final long[][][] PIECE = new long[NR_PLAYERS][NR_PIECE_TYPES][NN];
    static final long[][] CARD = new long[NR_PLAYERS][CARDS_PER_GAME];
    static final long SHIFT_PLAYER;

    static {
        Random rnd = new Random(0);

        for (int p = 0; p < NR_PLAYERS; ++p) {
            for (int t = 0; t < NR_PIECE_TYPES; ++t)
                for (int n = 0; n < NN; ++n)
                    PIECE[p][t][n] = rnd.nextLong();

            for (int c = 0; c < CARD[p].length; ++c)
                CARD[p][c] = rnd.nextLong();
        }

        SHIFT_PLAYER = rnd.nextLong();
    }
}