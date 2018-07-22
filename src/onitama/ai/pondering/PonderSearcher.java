package onitama.ai.pondering;

import onitama.ai.Searcher;
import onitama.common.ILogger;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;
import onitama.model.SearchParameters;

public class PonderSearcher implements Comparable<PonderSearcher> {
    public static final PonderSearcher SHUT_DOWN_SEARCH_THREADS = new PonderSearcher();

    public final Move moveSearched;

    private Searcher searcher;

    private int pliesSearched = 0;
    boolean searchCompleted = false;
    private Move bestMove;

    private PonderSearcher() { moveSearched = null; }

    PonderSearcher(Move moveSearched, int ttBits, int player, GameState gameState, ILogger logger) {
        this.moveSearched = moveSearched;

        searcher = new Searcher(new SearchParameters(ttBits, Searcher.MAX_NOMINAL_DEPTH, Integer.MAX_VALUE), logger, false);
        searcher.setState(player, gameState.board, gameState.cardState);
    }

    void searchNextPly() {
        Pair<Integer, Boolean> searchResult = searcher.searchNextPly();

        ++pliesSearched;

        if (searchCompleted = searchResult.q) {
            bestMove = searcher.getBestMove(); // extract best move before releasing memory
            searcher.releaseMemory();
        }
    }

    public boolean isSearchCompleted() { return searchCompleted; }

    public Move getBestMove() { return bestMove; }

    public PonderSearchStats getStats() {
        int depth = searcher.getScoreSearchNominalDepth();
        int score = searcher.getScore();
        long states = searcher.stats.getStatesEvaluated();
        long qStates = searcher.stats.getQuiescenceStatesEvaluated();

        String scoreString = score == Searcher.NO_SCORE ? " N/A" : String.format("%4d", score);
        String depthString = depth == -1 ? " -" : String.format("%2d", depth);

        return new PonderSearchStats(score, depth, String.format("%s  %-" + (Card.MAX_CARD_NAME_LENGTH + 6) + "s %s  %s", scoreString, moveSearched, depthString, Utils.formatNumber(states + qStates)));
    }

    public void resizeTT(int ttBits) {
        Ponderer.log("Resizing " + this + " to " + ttBits + " bits");
        searcher.resizeTTAsync(ttBits);
    }

    void stopSearch() {
        Ponderer.log("stopping " + this);
        searcher.stop();
    }

    void timeout(int remainingTimeMs) {
        searcher.enableLog(true);
        searcher.setRelativeTimeout(remainingTimeMs);
    }

    @Override public int compareTo(PonderSearcher ps) {
        // put shut-down token at the very end of the queue, to empty the queue before shutting down
        if (this == SHUT_DOWN_SEARCH_THREADS)
            return Integer.MAX_VALUE;
        if (ps == SHUT_DOWN_SEARCH_THREADS)
            return -Integer.MAX_VALUE;

        return pliesSearched - ps.pliesSearched;
    }

    @Override public String toString() {
        return String.format("%s (%d plies)", moveSearched, pliesSearched);
    }
}