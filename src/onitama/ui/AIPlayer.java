package onitama.ui;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import onitama.ai.Searcher;
import onitama.ai.pondering.PonderSearchStats;
import onitama.ai.pondering.Ponderer;
import onitama.common.ILogger;
import onitama.common.Utils;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.SearchParameters;

public class AIPlayer extends Player {
    /** Maximum amount of memory allocated for all ponder threads. */
    private static final long MAX_TT_PONDER_MEMORY = (long)(0.5 * 1024 * 1024 * 1024);

    private final SearchParameters searchParameters;

    private final boolean ponder;

    private Ponderer ponderer;
    private PonderUIThread ponderUIThread;

    private Searcher searcher;

    public AIPlayer(int player, SearchParameters searchParameters, boolean ponder, ILogger logger) {
        super(player, logger);
        this.searchParameters = searchParameters;
        this.ponder = ponder;

        if (ponder) {
            ponderer = new Ponderer(player, MAX_TT_PONDER_MEMORY, logger);
            ponderUIThread = new PonderUIThread(ponderer);
            ponderUIThread.setDaemon(true);
            ponderUIThread.start();
        }
    }

    @Override public void gameOver() {
        stopPonder();
    }

    @Override public String getName() { return "AI (" + (player+1) + ")"; }

    @Override public void opponentToMove(GameState gameState) {
        if (ponder) {
            ponderer.ponder(gameState);
            ponderUIThread.pondering = true;
        }
    }

    @Override public Move getMove(int turn, GameState gameState, Move opponentMove) {
        Move move = null;

        if (ponder && opponentMove != null) {
            ponderUIThread.pondering = false;
            move = ponderer.getBestMove(opponentMove, searchParameters.maxSearchTimeMs);
        }

        if (move == null) {
            searcher = new Searcher(searchParameters.maxDepth, searchParameters.ttBits, searchParameters.maxSearchTimeMs, false, logger, true);
            searcher.setState(player, gameState.board, gameState.cardState);
            searcher.start();
            move = searcher.getBestMove();
        }

        logger.logSearch(move.stats);
        logger.logMove(String.format("Turn %d: %s plays %s", turn + 1, getName(), move));

        return move;
    }

    private void stopPonder() {
        if (ponder) {
            ponderUIThread.pondering = false;
            ponderUIThread.shutdown = true;
            ponderer.shutdown();
        }
    }

    public void stopSearch() {
        stopPonder();

        if (searcher != null)
            searcher.stop();
    }

    private class PonderUIThread extends Thread {
        private final Ponderer ponderer;

        private volatile boolean pondering = false;

        private volatile boolean shutdown = false;

        private PonderUIThread(Ponderer ponderer) {
            this.ponderer = ponderer;
        }

        @Override public void run() {
            while (!shutdown) {
                if (pondering) {
                    List<PonderSearchStats> threadStats = ponderer.searchTasks.values().stream()
                            .map(pair -> pair.p.getStats())
                            .sorted(Comparator.comparingInt((PonderSearchStats stats) -> stats.score).thenComparingInt(stats -> stats.depth))
                            .collect(Collectors.toList());
                    logger.logPonder(threadStats);
                }

                Utils.silentSleep(1000);
            }
        }
    }
}
