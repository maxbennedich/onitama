package onitama.ai.pondering;

public class PonderSearchStats {
    public final int score;
    public final int depth;
    public final String stats;

    public PonderSearchStats(int score, int depth, String stats) {
        this.score = score;
        this.depth = depth;
        this.stats = stats;
    }
}