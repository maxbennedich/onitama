package onitama.ai;

public class Stats {
    static class PlyStats {
        long ttLookups = 0;
        long ttHits = 0;

        long bestMoveLookups = 0;
        long bestMoveHits = 0;

        long statesEvaluated = 0;
        long quiescenceStatesEvaluated = 0;
    }

    private PlyStats[] plyStats = new PlyStats[Searcher.MAX_DEPTH];

    private int maxDepthSearched = -1;

    private long statesEvaluated = 0;
    private long quiescenceStatesEvaluated = 0;
    private long leavesEvaluated = 0;
    private long[] playerWinCutoffs = {0, 0};
    private long alphaBetaCutoffs = 0;

    private final TranspositionTable tt;

    Stats(TranspositionTable tt) {
        this.tt = tt;

        for (int d = 0; d < Searcher.MAX_DEPTH; ++d)
            plyStats[d] = new PlyStats();
    }

    void resetDepthSeen() { maxDepthSearched = -1; }
    void depthSeen(int ply) { if (ply > maxDepthSearched) maxDepthSearched = ply; }
    public int getMaxDepthSeen() { return maxDepthSearched + 1; }

    void stateEvaluated(int ply) { ++statesEvaluated; ++plyStats[ply].statesEvaluated; }
    void quiescenceStateEvaluated(int ply) { ++quiescenceStatesEvaluated; ++plyStats[ply].quiescenceStatesEvaluated; }
    void leafEvaluated() { ++ leavesEvaluated; }
    void alphaBetaCutoff() { ++ alphaBetaCutoffs; }
    void playerWinCutoff(int player) { ++playerWinCutoffs[player]; }

    void ttLookup(int ply) { ++plyStats[ply].ttLookups; }
    void ttHit(int ply) { ++plyStats[ply].ttHits; }
    void bestMoveLookup(int ply) { ++plyStats[ply].bestMoveLookups; }
    void bestMoveHit(int ply) { ++plyStats[ply].bestMoveHits; }

    public long getStatesEvaluated() { return statesEvaluated; }
    public long getQuiescenceStatesEvaluated() { return quiescenceStatesEvaluated; }

    public void print() {
        System.out.printf("Max depth searched: %d%n", getMaxDepthSeen());
        System.out.printf("States evaluated: %d%n", statesEvaluated);
        System.out.printf("Quiescence states evaluated: %d%n", quiescenceStatesEvaluated);
        System.out.printf("Leaves evaluated: %d%n", leavesEvaluated);
        System.out.printf("Player win cutoffs: %d / %d%n", playerWinCutoffs[0], playerWinCutoffs[1]);
        System.out.printf("Alpha/beta cutoffs: %d%n", alphaBetaCutoffs);

        System.out.printf("TT fill rate: %.2f %%%n", 100.0*tt.usedEntries()/tt.sizeEntries());
        StringBuilder stats = new StringBuilder();
        long sumHits = 0, sumLookups = 0;
        for (int ply = 0; ply < Searcher.MAX_DEPTH && plyStats[ply].ttLookups > 0; ++ply) {
            stats.append(String.format(" %d: %.2f%% ", ply + 1, 100.0*plyStats[ply].ttHits/plyStats[ply].ttLookups));
            sumHits += plyStats[ply].ttHits;
            sumLookups += plyStats[ply].ttLookups;
        }
        System.out.printf("TT hit rate: %.2f %% (%d / %d) --%s%n", 100.0*sumHits/sumLookups, sumHits, sumLookups, stats);

        stats = new StringBuilder();
        sumHits = sumLookups = 0;
        for (int ply = 0; ply < Searcher.MAX_DEPTH && plyStats[ply].statesEvaluated > 0; ++ply) {
            stats.append(String.format(" %d: %.2f%% ", ply + 1, 100.0*plyStats[ply].bestMoveHits/plyStats[ply].bestMoveLookups));
            sumHits += plyStats[ply].bestMoveHits;
            sumLookups += plyStats[ply].bestMoveLookups;
        }
        System.out.printf("Best move hit rate: %.2f %% (%d / %d) --%s%n", 100.0*sumHits/sumLookups, sumHits, sumLookups, stats);

        stats = new StringBuilder();
        sumHits = sumLookups = 0;
        int ply = 0;
        for (ply = 0; ply < Searcher.MAX_DEPTH && plyStats[ply].statesEvaluated > 0; ++ply) {
            stats.append(String.format(" %d: %.2f ", ply + 1, (double)plyStats[ply].statesEvaluated/plyStats[ply].bestMoveLookups));
            sumHits += plyStats[ply].statesEvaluated;
            sumLookups += plyStats[ply].bestMoveLookups;
        }
        System.out.printf("Branching factor: %.2f --%s%n", (double)sumHits/sumLookups, stats);

        stats = new StringBuilder();
        sumHits = sumLookups = 0;
        for (; ply < Searcher.MAX_DEPTH && plyStats[ply].bestMoveLookups > 0; ++ply) {
            stats.append(String.format(" %d: %.2f ", ply + 1, (double)plyStats[ply].quiescenceStatesEvaluated/plyStats[ply].bestMoveLookups));
            sumHits += plyStats[ply].statesEvaluated;
            sumLookups += plyStats[ply].bestMoveLookups;
        }
        System.out.printf("Quiescence branching factor: %s%n", stats);
    }
}