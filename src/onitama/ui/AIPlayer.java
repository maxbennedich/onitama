package onitama.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import onitama.ai.Searcher;
import onitama.ai.TranspositionTable;
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
            ponderer = new Ponderer(player);
            ponderUIThread = new PonderUIThread(ponderer);
            ponderUIThread.start();
        }
    }

    @Override public void gameOver() {
        ponderUIThread.shutdown = true;
        ponderer.shutdown();
    }

    @Override public String getName() { return "AI (" + (player+1) + ")"; }

    private static int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(MAX_TT_PONDER_MEMORY / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    @Override public void opponentToMove(GameState gameState) {
        if (ponder) {
            ponderer.ponder(gameState);
            ponderUIThread.pondering = true;
        }
    }

    private Searcher startNewSearcher(GameState gameState) {
        Searcher searcher = new Searcher(searchParameters.maxDepth, searchParameters.ttBits, searchParameters.maxSearchTimeMs, true);
        searcher.setState(player, gameState.board, gameState.cardState);

        searcher.start();

        return searcher;
    }

    @Override public Move getMove(int turn, GameState gameState, Move opponentMove) {
        Move move = null;

        if (ponder && opponentMove != null) {
            ponderUIThread.pondering = false;
            move = ponderer.getBestMove(opponentMove, searchParameters.maxSearchTimeMs);
        }

        if (move == null)
            move = startNewSearcher(gameState).getBestMove();

        Output.printf("%nTurn %d: %s plays %s %c%c%c%c%n%n", turn + 1, getName(), move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny);

        return move;
    }

    static void silentSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
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

                silentSleep(5000);
            }
        }
    }

    static List<Pair<Move, GameState>> getPossibleMoves(int player, GameState gameState) {
        // create a dummy searcher to extract the list of possible moves
        Searcher searcher = new Searcher(50, 1, 0, false);
        searcher.setState(player, gameState.board, gameState.cardState);
        return searcher.getAllMoves();
    }

    static class Ponderer {
        final int player;

        final ExecutorService searcherExecutor = Executors.newCachedThreadPool();

        final ScheduledExecutorService ttResizeExecutor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> ttResizeFuture = null;
        volatile boolean ttResizing;
        final Object TT_RESIZE_LOCK = new Object();

        Map<Move, Pair<SearchTask, Future<Move>>> searchTasks;

        Ponderer(int player) {
            this.player = player;
        }

        public void ponder(GameState gameState) {
            searchTasks = new ConcurrentHashMap<>();

            List<Pair<Move, GameState>> movesToSearch = getPossibleMoves(1 - player, gameState);

            if (movesToSearch.isEmpty())
                return; // no moves available

            int ttBits = getTTBits(movesToSearch.size());
            ttResizing = true;

            // ensure that all search tasks exist before any task is started
            for (Pair<Move, GameState> move : movesToSearch)
                searchTasks.put(move.p, new Pair<SearchTask, Future<Move>>(new SearchTask(move.p, ttBits, player, move.q), null));

            for (Entry<Move, Pair<SearchTask, Future<Move>>> entry : searchTasks.entrySet())
                entry.getValue().q = searcherExecutor.submit(() -> {
                    Move move = entry.getValue().p.search();
                    submitTTResize();
                    return move;
                });
        }

        /**
         * Don't resize the TT immediately, wait for a few seconds to allow any other search tasks to complete. This avoids
         * repeatedly resizing the TT if several tasks end at about the same time.
         */
        void submitTTResize() {
            synchronized (TT_RESIZE_LOCK) {
                if (ttResizing) {
                    if (ttResizeFuture != null)
                        ttResizeFuture.cancel(false);
                    ttResizeFuture = ttResizeExecutor.schedule(() -> resizeTT(), 5, TimeUnit.SECONDS);
                }
            }
        }

        void resizeTT() {
            int tasksAlive = 0;
            for (Pair<SearchTask, Future<Move>> p : searchTasks.values())
                tasksAlive += p.p.done ? 0 : 1;

            int newTTBits = getTTBits(tasksAlive);

            for (Pair<SearchTask, Future<Move>> p : searchTasks.values())
                p.p.resizeTT(newTTBits);
        }

        private void stopTTResizing() {
            synchronized (TT_RESIZE_LOCK) {
                if (ttResizeFuture != null)
                    ttResizeFuture.cancel(false);
                ttResizing = false;
            }
        }

        public void shutdown() {
            stopTTResizing();
            ttResizeExecutor.shutdown();

            searchTasks.values().forEach(e -> e.p.stopSearch());
            searcherExecutor.shutdown();
        }

        /** @returns Value from future, or null if any exception occurs. */
        public <X> X getOrNull(Future<X> future) {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }

        public Move getBestMove(Move opponentMove, int remainingTimeMs) {
            stopTTResizing();

            Pair<SearchTask, Future<Move>> sm = searchTasks.remove(opponentMove);

            // request all threads except for the actual opponent move to stop, and then wait for them to shut down
            searchTasks.values().forEach(e -> e.p.stopSearch());
            searchTasks.values().forEach(e -> getOrNull(e.q));

            if (sm == null) {
                System.err.println("Could not find a searcher thread for move " + opponentMove); // probably a bug if this happens
                return null;
            }

            sm.p.timeout(remainingTimeMs);

            // resize TT to maximum size given a single worker thread
            sm.p.resizeTT(getTTBits(1));

            return getOrNull(sm.q);
        }
    }

    static class SearchTask {
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
            int depth = searcher.getPVScoreDepth();
            int score = searcher.getPVScore();
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
            searcher.log = true;
            searcher.setRelativeTimeout(remainingTimeMs);
        }
    }
}
