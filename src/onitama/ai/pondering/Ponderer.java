package onitama.ai.pondering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import onitama.ai.AIUtils;
import onitama.ai.TranspositionTable;
import onitama.common.ILogger;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

/**
 * Class that allows an AI player to ponder, i.e. consider next moves before the opponent's move is known.
 * <p>
 * Most literature recommends a single search, pondering just the most probable opponent move, assuming that this move will actually be played
 * by the opponent 50+% of the times ("ponder hit rate"). For this project, I have a assumed a much lower ponder hit rate, so instead a separate
 * search is started for every possible opponent move, and once the opponent moves, all irrelevant search processes are killed. This feature uses
 * dynamic TT resizing to make efficient use of the available memory; as search processes terminate, any remaining process can be assigned
 * more memory.
 */
public class Ponderer {
    private final int player;
    private final long maxPonderMemory;
    private final int nrPonderThreads;
    private final ILogger logger;

    private List<Thread> ponderThreads;

    public Map<Move, PonderSearcher> allSearchers;
    private PriorityBlockingQueue<PonderSearcher> searchQueue;
    private AtomicInteger searchersRemaining;
    private final Object searcherCompletedLock = new Object();

    private final ScheduledExecutorService ttResizeExecutor = Executors.newScheduledThreadPool(1, DAEMON_THREAD_FACTORY);
    private ScheduledFuture<?> ttResizeFuture = null;
    private volatile int ttBits;
    private volatile boolean ttResizing;
    private final Object ttResizeLock = new Object();

    /** Factory which creates daemon threads, allowing the application to exit even if these threads are alive. */
    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        @Override public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    };

    public Ponderer(int player, long maxPonderMemory, double cpuUtilization, ILogger logger) {
        this.player = player;
        this.maxPonderMemory = maxPonderMemory;
        this.nrPonderThreads = getCores(cpuUtilization);
        this.logger = logger;
    }

    private static int getCores(double cpuUtilization) {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        return Math.min(Math.max((int)(maxThreads * cpuUtilization + 0.5), 1), maxThreads);
    }

    private int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(maxPonderMemory / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    public static void main(String ... args) {
        Ponderer ponderer = new Ponderer(1, 100*1024*1024, 0.5, Utils.NO_LOGGER);
        String board =
                "b.Bbb" +
                "....." +
                ".b..." +
                ".wwW." +
                "w...w";
        CardState cards = new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon);
        GameState gameState = new GameState(board, cards);
        ponderer.ponder(gameState);
        Utils.sleepAndLogException(100000);
        Move move = ponderer.getBestMove(new Move(Card.Crane, 2, 3, 3, 4), 10000);
        System.out.println("best move = " + move);
    }

    public void ponder(GameState gameState) {
        List<Pair<Move, GameState>> movesToSearch = AIUtils.getPossibleMoves(1 - player, gameState);

        searchersRemaining = new AtomicInteger(movesToSearch.size());
        if (searchersRemaining.intValue() == 0)
            return; // no moves available

        System.out.println("Moves available:");
        for (Pair<Move, GameState> p : movesToSearch) System.out.println(p.p);
        System.out.println();

        ttBits = getTTBits(searchersRemaining.intValue());
        ttResizing = true;

        allSearchers = new ConcurrentHashMap<>();
        searchQueue = new PriorityBlockingQueue<>();

        for (Pair<Move, GameState> moveAndState : movesToSearch) {
            PonderSearcher searcher = new PonderSearcher(moveAndState.p, ttBits, player, moveAndState.q, logger);
            allSearchers.put(moveAndState.p, searcher);
            searchQueue.add(searcher);
        }

        ponderThreads = new ArrayList<>();
        for (int t = 0; t < nrPonderThreads; ++t) {
            final int tt = t;
            Thread thread = new Thread(() -> ponderThread(tt));
            thread.start();
            ponderThreads.add(thread);
        }

    }

    void ponderThread(int id) {
        try {
            while (true) {
                PonderSearcher searcher = searchQueue.take();
                if (searcher == PonderSearcher.SHUT_DOWN_SEARCH_THREADS) {
                    log("thread " + id + " - Shutdown token on thread with size = " + searchQueue.size());
                    searchQueue.add(PonderSearcher.SHUT_DOWN_SEARCH_THREADS);
                    break;
                }
                log("thread " + id + " - searching one ply for move " + searcher);
                searcher.searchNextPly();
                if (searcher.isSearchCompleted()) {
                    int searchersRemainingCount = searchersRemaining.decrementAndGet();
                    log("thread " + id + " - ponder move "+searcher+" analyzed fully; searchers remaining = " + searchersRemainingCount);
                    synchronized (searcherCompletedLock) {
                        searcherCompletedLock.notifyAll();
                    }
                    submitTTResize();
                } else {
                    log("thread " + id + " - finished searching one ply for move " + searcher + ", putting back in queue");
                    searchQueue.add(searcher);
                }
            }
        } catch (InterruptedException ie) { ie.printStackTrace(); }
        log("thread " + id + " - ponder thread ends");
    }

    /**
     * Don't resize the TT immediately, wait for a few seconds to allow any other search tasks to complete. This avoids
     * repeatedly resizing the TT if several tasks end at about the same time.
     */
    private void submitTTResize() {
        synchronized (ttResizeLock) {
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
        ttBits = getTTBits(searchersRemaining.intValue());

        for (PonderSearcher p : allSearchers.values())
            if (!p.searchCompleted)
                p.resizeTT(ttBits);
    }

    private void stopTTResizing() {
        synchronized (ttResizeLock) {
            if (ttResizeFuture != null)
                ttResizeFuture.cancel(false);
            ttResizing = false;
        }
    }

    public void shutdown() {
        stopTTResizing();

        allSearchers.values().forEach(s -> s.stopSearch());
        searchQueue.add(PonderSearcher.SHUT_DOWN_SEARCH_THREADS);
        ponderThreads.forEach(t -> { try { t.join(); log("stopped " + t);} catch (InterruptedException ie) { ie.printStackTrace(); }});
    }

    static long t0 = System.currentTimeMillis();

    static void log(String str) {
        System.out.printf("%5d - %s%n", System.currentTimeMillis() - t0, str);
    }

    public Move getBestMove(Move opponentMove, int remainingTimeMs) {
        long timestamp = System.nanoTime();

        stopTTResizing();

        log("Shutting down searchers");
        for (PonderSearcher searcher : allSearchers.values())
            if (!opponentMove.equals(searcher.moveSearched))
                searcher.stopSearch();

        log("Waiting for searchers to die");
        synchronized (searcherCompletedLock) {
            while (allSearchers.values().stream().filter(s -> !opponentMove.equals(s.moveSearched)).anyMatch(s -> !s.isSearchCompleted())) {
                log("searcher remaining, waiting...; number remaining = " + allSearchers.values().stream().filter(s -> !opponentMove.equals(s.moveSearched) && !s.isSearchCompleted()).count());
                try {
                    searcherCompletedLock.wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        log("all searchers dead");

        PonderSearcher searcherToKeep = allSearchers.get(opponentMove);
        searcherToKeep.resizeTT(getTTBits(1));
        int elapsedMs = (int)((System.nanoTime() - timestamp + 500_000) / 1_000_000);
        log("adjusting timeout to " + Math.max(0, remainingTimeMs - elapsedMs) + " ms");
        searcherToKeep.timeout(Math.max(0, remainingTimeMs - elapsedMs));

        searchQueue.add(PonderSearcher.SHUT_DOWN_SEARCH_THREADS);
        ponderThreads.forEach(t -> { try { t.join(); log("stopped " + t);} catch (InterruptedException ie) { ie.printStackTrace(); }});

        return searcherToKeep.getBestMove();
    }
}