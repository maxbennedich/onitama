package onitama.ai.pondering;

import onitama.ai.Searcher;
import onitama.common.ILogger;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.GameState;
import onitama.model.Move;

public class PonderSearchTask {
    private final Move moveSearched;

    private Searcher searcher;

    PonderSearchTask(Move moveSearched, int ttBits, int player, GameState gameState, ILogger logger) {
        this.moveSearched = moveSearched;

        searcher = new Searcher(Searcher.MAX_DEPTH, ttBits, Integer.MAX_VALUE, true, logger, false);
        searcher.setState(player, gameState.board, gameState.cardState);
    }

    Move search() {
        searcher.start();
        Move bestMove = searcher.getBestMove(); // extract best move before releasing memory
        searcher.releaseMemory();
        return bestMove;
    }

    public PonderSearchStats getStats() {
        int depth = searcher.getScoreSearchNominalDepth();
        int score = searcher.getScore();
        long states = searcher.stats.getStatesEvaluated();
        long qStates = searcher.stats.getQuiescenceStatesEvaluated();

        String scoreString = score == Searcher.NO_SCORE ? " N/A" : String.format("%4d", score);
        String depthString = depth == -1 ? " -" : String.format("%2d", depth);

        return new PonderSearchStats(score, depth, String.format("%s  %-" + (Card.getMaxCardNameLength() + 6) + "s %s  %s", scoreString, moveSearched, depthString, Utils.formatNumber(states + qStates)));
    }

    void resizeTT(int ttBits) {
        searcher.resizeTTAsync(ttBits);
    }

    void stopSearch() {
        searcher.stop();
    }

    void timeout(int remainingTimeMs) {
        searcher.enableLog(true);
        searcher.setPriority(true);
        searcher.setRelativeTimeout(remainingTimeMs);
    }
}