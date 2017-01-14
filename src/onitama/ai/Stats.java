package onitama.ai;

public class Stats {
    static class PlyStats {
        long ttLookups = 0;
        long ttHits = 0;

        long killerMoveLookups = 0;
        long killerMoveHits = 0;

        long fullStatesEvaluated = 0;
        long quiescenceFullStatesEvaluated = 0;
    }

    private PlyStats[] plyStats = new PlyStats[Searcher.MAX_DEPTH];

    private int maxDepthSearched = -1;

    private long statesEvaluated = 0;
    private long fullStatesEvaluated = 0;
    private long quiescenceStatesEvaluated = 0;
    private long quiescenceFullStatesEvaluated = 0;
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

    void stateEvaluated() { ++statesEvaluated; }
    void fullStateEvaluated(int ply) { ++fullStatesEvaluated; ++plyStats[ply].fullStatesEvaluated; }
    void quiescenceStateEvaluated() { ++quiescenceStatesEvaluated; }
    void quiescenceFullStateEvaluated(int ply) { ++quiescenceFullStatesEvaluated; ++plyStats[ply].quiescenceFullStatesEvaluated; }
    void leafEvaluated() { ++ leavesEvaluated; }
    void alphaBetaCutoff() { ++ alphaBetaCutoffs; }
    void playerWinCutoff(int player) { ++playerWinCutoffs[player]; }

    void ttLookup(int ply) { ++plyStats[ply].ttLookups; }
    void ttHit(int ply) { ++plyStats[ply].ttHits; }
    void killerMoveLookup(int ply) { ++plyStats[ply].killerMoveLookups; }
    void killerMoveHit(int ply) { ++plyStats[ply].killerMoveHits; }

    public long getFullStatesEvaluated() { return fullStatesEvaluated; }
    public long getQuiescenceFullStatesEvaluated() { return quiescenceFullStatesEvaluated; }

    public void print() {
        System.out.printf("Max depth searched: %d%n", maxDepthSearched + 1);
        System.out.printf("States evaluated: %d / %d%n", fullStatesEvaluated, statesEvaluated);
        System.out.printf("Quiescence states evaluated: %d / %d%n", quiescenceFullStatesEvaluated, quiescenceStatesEvaluated);
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
        for (int ply = 0; ply < Searcher.MAX_DEPTH && plyStats[ply].fullStatesEvaluated > 0; ++ply) {
            stats.append(String.format(" %d: %.2f%% ", ply + 1, 100.0*plyStats[ply].killerMoveHits/plyStats[ply].killerMoveLookups));
            sumHits += plyStats[ply].killerMoveHits;
            sumLookups += plyStats[ply].killerMoveLookups;
        }
        System.out.printf("Killer move hit rate: %.2f %% (%d / %d) --%s%n", 100.0*sumHits/sumLookups, sumHits, sumLookups, stats);

        stats = new StringBuilder();
        sumHits = sumLookups = 0;
        int ply = 0;
        for (ply = 0; ply < Searcher.MAX_DEPTH && plyStats[ply].fullStatesEvaluated > 0; ++ply) {
            stats.append(String.format(" %d: %.2f ", ply + 1, (double)plyStats[ply].fullStatesEvaluated/plyStats[ply].killerMoveLookups));
            sumHits += plyStats[ply].fullStatesEvaluated;
            sumLookups += plyStats[ply].killerMoveLookups;
        }
        System.out.printf("Branching factor: %.2f --%s%n", (double)sumHits/sumLookups, stats);

        stats = new StringBuilder();
        sumHits = sumLookups = 0;
        for (; ply < Searcher.MAX_DEPTH && plyStats[ply].killerMoveLookups > 0; ++ply) {
            stats.append(String.format(" %d: %.2f ", ply + 1, (double)plyStats[ply].quiescenceFullStatesEvaluated/plyStats[ply].killerMoveLookups));
            sumHits += plyStats[ply].fullStatesEvaluated;
            sumLookups += plyStats[ply].killerMoveLookups;
        }
        System.out.printf("Quiescence branching factor: %s%n", stats);
    }
}