package onitama.ai.pondering;

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

import onitama.ai.AIUtils;
import onitama.ai.TranspositionTable;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class Ponderer {
    final int player;
    final long maxPonderMemory;

    final ExecutorService searcherExecutor = Executors.newCachedThreadPool();

    final ScheduledExecutorService ttResizeExecutor = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> ttResizeFuture = null;
    volatile boolean ttResizing;
    final Object TT_RESIZE_LOCK = new Object();

    public Map<Move, Pair<SearchTask, Future<Move>>> searchTasks;

    public Ponderer(int player, long maxPonderMemory) {
        this.player = player;
        this.maxPonderMemory = maxPonderMemory;
    }

    int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(maxPonderMemory / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    public void ponder(GameState gameState) {
        searchTasks = new ConcurrentHashMap<>();

        List<Pair<Move, GameState>> movesToSearch = AIUtils.getPossibleMoves(1 - player, gameState);

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

        Pair<SearchTask, Future<Move>> searchToKeep = searchTasks.remove(opponentMove);

        // request all threads except for the actual opponent move to stop, and then wait for them to shut down
        searchTasks.values().forEach(e -> e.p.stopSearch());
        searchTasks.values().forEach(e -> getOrNull(e.q));

        if (searchToKeep == null) {
            System.err.println("Could not find a searcher thread for move " + opponentMove); // probably a bug if this happens
            return null;
        }

        searchToKeep.p.timeout(remainingTimeMs);

        // resize TT to maximum size given a single worker thread
        searchToKeep.p.resizeTT(getTTBits(1));

        return getOrNull(searchToKeep.q);
    }
}