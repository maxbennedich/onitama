package onitama.model;

public class SearchParameters {
    public final int ttBits;
    public final int maxDepth;
    public final int maxSearchTimeMs;

    public SearchParameters(int ttBits, int maxDepth, int maxSearchTimeMs) {
        this.ttBits = ttBits;
        this.maxDepth = maxDepth;
        this.maxSearchTimeMs = maxSearchTimeMs;
    }

    @Override public String toString() {
        return String.format("TT = %d bits, Depth = %d, Time = %d ms", ttBits, maxDepth, maxSearchTimeMs);
    }
}
