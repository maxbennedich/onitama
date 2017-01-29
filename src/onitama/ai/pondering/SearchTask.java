package onitama.ai.pondering;

import onitama.ai.Searcher;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class SearchTask {
    final Move moveTested;
    int ttBits;
    final int player;
    final GameState gameState;

    boolean done = false;

    private Searcher searcher;

    SearchTask(Move moveTested, int ttBits, int player, GameState gameState) {
        this.moveTested = moveTested;
        this.ttBits = ttBits;
        this.player = player;
        this.gameState = gameState;

        searcher = new Searcher(50, ttBits, Integer.MAX_VALUE, false);
        searcher.setState(player, gameState.board, gameState.cardState);
    }

    public Move search() {
        searcher.start();
        searcher.releaseMemory();
        done = true;
        return searcher.getBestMove();
    }

    public Pair<Integer, String> getStats() {
        int depth = searcher.getScoreSearchDepth();
        int score = searcher.getScore();
        long states = searcher.stats.getStatesEvaluated();
        long qStates = searcher.stats.getQuiescenceStatesEvaluated();
        return new Pair<>(score, String.format("%s: %d (%d plies, %d states)", moveTested, score, depth, states + qStates));
    }

    public void resizeTT(int ttBits) {
        searcher.resizeTTAsync(ttBits);
    }

    public void stopSearch() {
        searcher.stop();
    }

    public void timeout(int remainingTimeMs) {
        searcher.enableLog(true);
        searcher.setRelativeTimeout(remainingTimeMs);
    }
}