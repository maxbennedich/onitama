package onitama.ai;

import java.util.concurrent.Semaphore;

/**
 * Hash map of seen board states with fixed size which uses a two-tier storage scheme, with one depth-preferred entry,
 * and one replace-always entry.
 *
 * The keys in the map are 64 bit Zobrist keys representing the full state of the board. Although hash collisions
 * are possible due to the birthday paradox and the large number of states visited, they are rare in practice,
 * and even if a collision occurs, it is highly unlikely that it will have any measurable effect. Read this
 * for a more in-depth analysis: http://www.craftychess.com/hyatt/collisions.html
 *
 * TODO: make index part of key to compress keys better
 */
public class TranspositionTable {
    public static final int BYTES_PER_ENTRY = 8 + 4;

    static final int NO_ENTRY = Integer.MAX_VALUE;

    /** Semaphore used to prevent tables from different threads from resizing at the same time (which could lead to OOM). */
    private static final Semaphore RESIZE_SEMAPHORE = new Semaphore(1);

    private int ttBits;

    private long[] keys;
    private int[] states;

    public TranspositionTable(int ttBits) {
        this.ttBits = ttBits;

        keys = new long[1 << ttBits];
        states = new int[1 << ttBits];
    }

    void put(long key, int state) {
        int idx = (int)(key & ((1 << ttBits) - 2)); // clear last bit (to support two tiers)
        if (keys[idx] != 0) {
            int existingDepth = (states[idx] >> 2) & 63;
            int newDepth = (state >> 2) & 63;
            if (newDepth >= existingDepth) {
                keys[idx+1] = keys[idx]; // replace the replace-always entry with the depth-preferred entry
                states[idx+1] = states[idx];
            } else {
                ++idx;
            }
        }
        keys[idx] = key;
        states[idx] = state;
    }

    int get(long key) {
        int idx = (int)(key & ((1 << ttBits) - 2)); // clear last bit (to support two tiers)
        if (keys[idx] == key) return states[idx];
        if (keys[idx+1] == key) return states[idx+1];
        return NO_ENTRY;
    }

    public int sizeEntries() {
        return 1 << ttBits;
    }

    public long sizeBytes() {
        return (long)sizeEntries() * BYTES_PER_ENTRY;
    }

    /**
     * Attempts to change the size of this table, carrying over all stored entries. This is a no-op if the new size is the same as the current size.
     * @return False if another thread is currently doing a resize (two resizes can not run at the same time). Otherwise true.
     */
    boolean resize(int newTTBits) {
        if (newTTBits == ttBits)
            return true;

        // The section below will hold on to two TTs at the same time during the resize operation. If called simultaneously
        // for multiple threads, it could cause OOMs, so lock down access to a single thread at a time.
        if (!RESIZE_SEMAPHORE.tryAcquire())
            return false;

        TranspositionTable newTT = new TranspositionTable(newTTBits);

        for (int n = 0; n < 1 << ttBits; ++n)
            if (keys[n] != 0)
                newTT.put(keys[n], states[n]);

        ttBits = newTTBits;
        keys = newTT.keys;
        states = newTT.states;

        RESIZE_SEMAPHORE.release();

        return true;
    }

    /** Replaces this table with a single entry table. Destructive operation intended to release the memory held by this instance. */
    void truncate() {
        ttBits = 1;
        keys = new long[1];
        states = new int[1];
    }

    // Slow since it loops over all entries. (Intended for post-game analysis only.)
    int usedEntries() {
        int used = 0;
        for (int n = 0; n < sizeEntries(); ++n)
            if (states[n] != 0)
                ++used;
        return used;
    }
}