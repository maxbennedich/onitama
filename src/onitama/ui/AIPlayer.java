package onitama.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import onitama.ai.Searcher;
import onitama.ai.TranspositionTable;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;
import onitama.model.SearchParameters;

public class AIPlayer extends Player {

    private final SearchParameters searchParameters;

    private SearchThreadMonitor searchThreadMonitor;
    private final boolean ponder;

    private static final long MAX_TT_PONDER_MEMORY = 5L * 1024 * 1024 * 1024;

    public AIPlayer(int player, SearchParameters searchParameters, boolean ponder) {
        super(player);
        this.searchParameters = searchParameters;
        this.ponder = ponder;
    }

    @Override public String getName() { return "AI (" + (player+1) + ")"; }

    private static int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(MAX_TT_PONDER_MEMORY / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    @Override public void opponentToMove(GameState gameState) {
        if (!ponder)
            return;

        searchThreadMonitor = new SearchThreadMonitor(player, gameState);
        searchThreadMonitor.start();
    }

    static class SearchThreadMonitor extends Thread {
        final int player;
        final GameState gameState;

        List<SearchThread> searchThreads;

        volatile boolean requestStop = false;
        volatile boolean threadFinished = false;

        SearchThreadMonitor(int player, GameState gameState) {
            this.player = player;
            this.gameState = gameState;
        }

        @Override public void run() {
            // create a dummy searcher to extract the list of possible moves
            Searcher searcher = new Searcher(50, 1, false);
            searcher.setState(1 - player, gameState.board, gameState.cardState);
            List<Pair<Move, GameState>> movesToSearch = searcher.getAllMoves();

            if (movesToSearch.isEmpty()) {
                System.out.println("No moves available!");
                threadFinished = true;
                return;
            }

            int ttBits = getTTBits(movesToSearch.size());
            System.out.printf("%nAvailable moves = %d%nTT bits = %d (%.2f GB)%n", movesToSearch.size(), ttBits, movesToSearch.size() * (1L << ttBits) * TranspositionTable.BYTES_PER_ENTRY / 1024.0 / 1024.0 / 1024.0);

            searchThreads = new ArrayList<>();
            for (Pair<Move, GameState> move : movesToSearch) {
                SearchThread t = new SearchThread(move.p, ttBits, player, move.q.board, move.q.cardState);
                t.start();
                searchThreads.add(t);
            }

            while (!requestStop) {
                List<Pair<Integer, String>> threadStats = new ArrayList<>();
                int activeThreads = 0;
                for (Iterator<SearchThread> it = searchThreads.iterator(); it.hasNext(); ) {
                    SearchThread t = it.next();
                    threadStats.add(t.getStats());
                    activeThreads += t.threadFinished ? 0 : 1;
                }

                threadStats.sort((a, b) -> a.p.compareTo(b.p));
                threadStats.forEach(stats -> System.out.println(stats.q));

                if (activeThreads == 0)
                    break;

                // see if we should resize the TTs
                int newTTBits = getTTBits(activeThreads);
                if (newTTBits != ttBits) {
                    ttBits = newTTBits;
                    searchThreads.stream().filter(t -> !t.threadFinished).forEach(t -> t.searcher.resizeTTAsync(newTTBits));
                }
                System.out.printf("Search threads = %d  -  TT bits = %d (%.2f GB)%n", activeThreads, ttBits, activeThreads * (1L << ttBits) * TranspositionTable.BYTES_PER_ENTRY / 1024.0 / 1024.0 / 1024.0);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) { /* ignore */ }

                System.out.println("---------------------");
            }

            threadFinished = true;
        }

        SearchThread stopAndGetSearchThreadForMove(Move move) {
            requestStop = true;

            // locate the thread corresponding to the move made and remove it from the list
            SearchThread searchThread = null;
            for (Iterator<SearchThread> it = searchThreads.iterator(); it.hasNext(); ) {
                SearchThread t = it.next();
                if (t.moveTested.equals(move)) {
                    searchThread = t;
                    it.remove();
                    break;
                }
            }

            if (searchThread == null)
                System.err.println("Could not find a searcher thread for move " + move);

            // request all other threads to stop, and then wait for them to shut down
            searchThreads.forEach(SearchThread::stopSearch);
            searchThreads.forEach(AIPlayer::waitForShutdown);

            if (searchThread != null)
                searchThread.resizeTT(getTTBits(1));

            return searchThread;
        }
    }

    static void waitForShutdown(SearchThread t) {
        try {
            t.join(2000); // ample time for the thread to shut down (should take at most a few ms under normal circumstances)
        } catch (InterruptedException e) { /* ignore */ }
        if (!t.threadFinished)
            System.err.println("Failed to shut down search thread '" + t.moveTested + "'");
    }

    static class SearchThread extends Thread {
        private static final Object SEARCHER_LOCK = new Object();

        final Move moveTested;
        final int player;
        final String board;
        final CardState cardState;

        private Searcher searcher;
        volatile boolean threadFinished = false;

        private Pair<Integer, String> stats = null;
        private Move bestMove = null;

        SearchThread(Move moveTested, int ttBits, int player, String board, CardState cardState) {
            this.moveTested = moveTested;
            this.player = player;
            this.board = board;
            this.cardState = cardState;

            searcher = new Searcher(50, ttBits, false);
            searcher.setState(player, board, cardState);
        }

        public void resizeTT(int ttBits) {
            synchronized (SEARCHER_LOCK) {
                if (searcher != null)
                    searcher.resizeTTAsync(ttBits);
            }
        }

        @Override public void run() {
            searcher.start(Integer.MAX_VALUE);
            threadFinished = true;

            // store the relevant information and dispose of the searcher, so it can be GCed
            synchronized (SEARCHER_LOCK) {
                bestMove = getSearcherBestMove();
                stats = getSearcherStats();
                searcher = null;
            }
        }

        Move getBestMove() {
            synchronized (SEARCHER_LOCK) {
                return searcher == null ? bestMove : getSearcherBestMove();
            }
        }

        Pair<Integer, String> getStats() {
            synchronized (SEARCHER_LOCK) {
                return searcher == null ? stats : getSearcherStats();
            }
        }

        void stopSearch() {
            synchronized (SEARCHER_LOCK) {
                if (searcher != null)
                    searcher.stop();
            }
        }

        private Move getSearcherBestMove() {
            return searcher.getBestMove();
        }

        private Pair<Integer, String> getSearcherStats() {
            int depth = searcher.getPVScoreDepth();
            int score = searcher.getPVScore();
            long states = searcher.stats.getStatesEvaluated();
            long qStates = searcher.stats.getQuiescenceStatesEvaluated();
            return new Pair<>(score, String.format("%s: %d (%d plies, %d states)", moveTested, score, depth, states + qStates));
        }

        public Move getBestMoveRelativeTimeout(int maxSearchTimeMs) {
            synchronized (SEARCHER_LOCK) {
                // search finished already
                if (searcher == null)
                    return bestMove;

                // keep searching
                searcher.log = true;
                searcher.setRelativeTimeout(maxSearchTimeMs);
            }

            // wait for search to finish
            try {
                join();
            } catch (InterruptedException e) { /* ignore */ }

            return bestMove;
        }
    }

    private Searcher startNewSearcher(GameState gameState) {
        Searcher searcher = new Searcher(searchParameters.maxDepth, searchParameters.ttBits, true);
        searcher.setState(player, gameState.board, gameState.cardState);

        searcher.start(searchParameters.maxSearchTimeMs);

        return searcher;
    }

    @Override public Move getMove(int turn, GameState gameState, Move opponentMove) {
        SearchThread searchThread = null;
        Move move = null;

        if (ponder && opponentMove != null) {
            searchThread = searchThreadMonitor.stopAndGetSearchThreadForMove(opponentMove);
            if (searchThread != null)
                move = searchThread.getBestMoveRelativeTimeout(searchParameters.maxSearchTimeMs);
        }

        if (move == null) {
            Searcher searcher = startNewSearcher(gameState);
            move = searcher.getBestMove();
        }

        Output.printf("%nTurn %d: %s plays %s %c%c%c%c%n%n", turn + 1, getName(), move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny);

        return move;
    }
}
