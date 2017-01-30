package onitama.ai.pondering;

import java.util.List;
import java.util.Map;
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

/**
 * Class that allows an AI player to ponder, i.e. consider next moves before the opponent's move is known.
 * <p>
 * Most literature recommends a single search, pondering just the most probable opponent move, assuming that this move will actually be played
 * by the opponent 50+% of the times ("ponder hit rate"). For this project, I have a assumed a much lower ponder hit rate, so instead a separate
 * search is started for every possible opponent move, and once the opponent moves, all irrelevant search threads are killed. This feature uses
 * dynamic TT resizing to make efficient use of the available memory; as search threads terminate, any remaining search threads can be assigned
 * more memory.
 */
public class Ponderer {
    private final int player;
    private final long maxPonderMemory;

    private final ExecutorService searcherExecutor = Executors.newCachedThreadPool();

    private final ScheduledExecutorService ttResizeExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> ttResizeFuture = null;
    private volatile int ttBits;
    private volatile boolean ttResizing;
    private final Object TT_RESIZE_LOCK = new Object();

    public Map<Move, Pair<SearchTask, Future<Move>>> searchTasks;
    private volatile int tasksRemaining;

    public Ponderer(int player, long maxPonderMemory) {
        this.player = player;
        this.maxPonderMemory = maxPonderMemory;
    }

    private int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(maxPonderMemory / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    public void ponder(GameState gameState) {
        List<Pair<Move, GameState>> movesToSearch = AIUtils.getPossibleMoves(1 - player, gameState);

        tasksRemaining = movesToSearch.size();
        searchTasks = new ConcurrentHashMap<>(tasksRemaining);

        if (tasksRemaining == 0)
            return; // no moves available

        ttBits = getTTBits(tasksRemaining);
        ttResizing = true;

        for (Pair<Move, GameState> moveAndState : movesToSearch) {
            SearchTask task = new SearchTask(moveAndState.p, ttBits, player, moveAndState.q);
            Future<Move> future = searcherExecutor.submit(() -> {
                Move move = task.search();
                --tasksRemaining;
                submitTTResize();
                return move;
            });
            searchTasks.put(moveAndState.p, new Pair<>(task, future));
        }
    }

    /**
     * Don't resize the TT immediately, wait for a few seconds to allow any other search tasks to complete. This avoids
     * repeatedly resizing the TT if several tasks end at about the same time.
     */
    private void submitTTResize() {
        synchronized (TT_RESIZE_LOCK) {
            if (ttResizing) {
                if (ttResizeFuture != null)
                    ttResizeFuture.cancel(false);
                ttResizeFuture = ttResizeExecutor.schedule(() -> resizeTT(), 5, TimeUnit.SECONDS);
            }
        }
    }

    // In theory, this method can be called before all search tasks have been created, or after a search task has been
    // created but before its search has started. Both of these situations are handled "correctly" by making sure that
    // the unstarted tasks will get the new TT size.
    private void resizeTT() {
        ttBits = getTTBits(tasksRemaining);

        for (Pair<SearchTask, Future<Move>> p : searchTasks.values())
            p.p.resizeTT(ttBits);
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
    private static <X> X getOrNull(Future<X> future) {
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