package onitama.ai.pondering;

import onitama.ai.Searcher;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class SearchTask {
    private final Move moveSearched;

    private Searcher searcher;

    SearchTask(Move moveSearched, int ttBits, int player, GameState gameState) {
        this.moveSearched = moveSearched;

        searcher = new Searcher(50, ttBits, Integer.MAX_VALUE, false);
        searcher.setState(player, gameState.board, gameState.cardState);
    }

    Move search() {
        searcher.start();
        searcher.releaseMemory();
        return searcher.getBestMove();
    }

    public Pair<Integer, String> getStats() {
        int depth = searcher.getScoreSearchDepth();
        int score = searcher.getScore();
        long states = searcher.stats.getStatesEvaluated();
        long qStates = searcher.stats.getQuiescenceStatesEvaluated();
        return new Pair<>(score, String.format("%s: %d (%d plies, %d states)", moveSearched, score, depth, states + qStates));
    }

    void resizeTT(int ttBits) {
        searcher.resizeTTAsync(ttBits);
    }

    void stopSearch() {
        searcher.stop();
    }

    void timeout(int remainingTimeMs) {
        searcher.enableLog(true);
        searcher.setRelativeTimeout(remainingTimeMs);
    }
}