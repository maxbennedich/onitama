package onitama.model;

public class SearchParameters {
    public int ttBits;
    public int maxDepth;
    public int maxSearchTimeMs;

    public SearchParameters(int ttBits, int maxDepth, int maxSearchTimeMs) {
        this.ttBits = ttBits;
        this.maxDepth = maxDepth;
        this.maxSearchTimeMs = maxSearchTimeMs;
    }
}
