package onitama.ai;

/**
 * Hash map of seen board states with fixed size which overwrites existing entries.
 *
 * The keys in the map are 64 bit Zobrist keys representing the full state of the board. Although hash collisions
 * are possible due to the birthday paradox and the large number of states visited, they are rare in practice,
 * and even if a collision occurs, it is highly unlikely that it will have any measurable effect. Read this
 * for a more in-depth analysis: http://www.craftychess.com/hyatt/collisions.html
 *
 * TODO: make index part of key to compress keys better
 * TODO: don't overwrite entry if depth is less (shallower entries are more valuable)
 */
public class TranspositionTable {
    public static final int NO_ENTRY = Integer.MAX_VALUE;

    /** Look at this many entries to find a best hash position to replace the state. */
//    private static final int HASH_WINDOW = 4;

    private final int hashBits;

    long[] keys;
    int[] state;

    public TranspositionTable(int hashBits) {
        this.hashBits = hashBits;

        keys = new long[1 << hashBits];
        state = new int[1 << hashBits];

//      keys = new long[(1 << hashBits) + HASH_WINDOW - 1];
//      state = new int[(1 << hashBits) + HASH_WINDOW - 1];
    }

    // XXX this optimization doesn't really help, it reduces the # visited states by 1-3 %, but costs slightly more than it saves
//    void put(long key, int score) {
//        int idx = (int)(key & ((1 << hashBits) - 1)) - 1;
//        for (int n = 0; n < HASH_WINDOW; ++n) {
//            ++idx;
//            if (keys[idx] == 0 || keys[idx] == key)
//                break;
//        }
//        keys[idx] = key;
//        state[idx] = score;
//    }
//
//    int get(long key) {
//        int idx = (int)(key & ((1 << hashBits) - 1));
//        for (int n = 0; n < HASH_WINDOW; ++n, ++idx)
//            if (keys[idx] == key)
//                return state[idx];
//        return NO_ENTRY;
//    }

    void put(long key, int score) {
        int idx = (int)(key & ((1 << hashBits) - 1));
        keys[idx] = key;
        state[idx] = score;
    }

    int get(long key) {
        int idx = (int)(key & ((1 << hashBits) - 1));
        return keys[idx] == key ? state[idx] : NO_ENTRY;
    }

    public int sizeEntries() {
        return 1 << hashBits;
    }

    public long sizeBytes() {
        return (long)sizeEntries() * (8 + 4);
    }

    // Slow since it loops over all entries. (Intended for post-game analysis only.)
    int usedEntries() {
        int used = 0;
        for (int n = 0; n < sizeEntries(); ++n)
            if (state[n] != 0)
                ++used;
        return used;
    }
}