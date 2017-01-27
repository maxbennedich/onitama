package onitama.ui;

import java.util.ArrayList;
import java.util.List;

import onitama.ai.AIUtils;
import onitama.ai.pondering.Ponderer;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;
import onitama.model.SearchParameters;

public class AIPlayer extends Player {

    private final SearchParameters searchParameters;

    private final boolean ponder;

    private Ponderer ponderer;
    private PonderUIThread ponderUIThread;

    private static final long MAX_TT_PONDER_MEMORY = (long)(0.5 * 1024 * 1024 * 1024);

    public AIPlayer(int player, SearchParameters searchParameters, boolean ponder) {
        super(player);
        this.searchParameters = searchParameters;
        this.ponder = ponder;

        if (ponder) {
            ponderer = new Ponderer(player, MAX_TT_PONDER_MEMORY);
            ponderUIThread = new PonderUIThread(ponderer);
            ponderUIThread.start();
        }
    }

    @Override public void gameOver() {
        ponderUIThread.shutdown = true;
        ponderer.shutdown();
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

        if (move == null)
            move = AIUtils.startNewSearcher(player, gameState, searchParameters).getBestMove();

        Output.printf("%nTurn %d: %s plays %s %c%c%c%c%n%n", turn + 1, getName(), move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny);

        return move;
    }

    static class PonderUIThread extends Thread {
        private final Ponderer ponderer;

        public volatile boolean pondering = false;

        public volatile boolean shutdown = false;

        PonderUIThread(Ponderer ponderer) {
            this.ponderer = ponderer;
        }

        @Override public void run() {
            while (!shutdown) {
                if (pondering) {
                    List<Pair<Integer, String>> threadStats = new ArrayList<>();
                    ponderer.searchTasks.forEach((move, pair) -> {
                        threadStats.add(pair.p.getStats());
                    });

                    threadStats.sort((a, b) -> a.p.compareTo(b.p));
                    threadStats.forEach(stats -> System.out.println(stats.q));
                    System.out.println("------------------------");
                }

                UIUtils.silentSleep(5000);
            }
        }
    }
}
