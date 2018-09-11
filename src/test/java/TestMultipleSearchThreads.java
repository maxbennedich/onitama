package onitama.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import onitama.ai.Searcher;
import onitama.ai.TranspositionTable;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;
import onitama.model.SearchParameters;
import onitama.ui.console.Output;

public class TestMultipleSearchThreads {
    static final long MAX_TT_MEMORY = 5L * 1024 * 1024 * 1024;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String BOARD_WIN_AT_13 =
            "b.Bbb" +
            "....." +
            ".b..." +
            ".rrR." +
            "r...r";

    public static void main(String ... args) throws Exception {
        new TestMultipleSearchThreads().testThreadForEachMove();
    }

    static int getTTBits(int nrSearchThreads) {
        return 63 - Long.numberOfLeadingZeros(MAX_TT_MEMORY / nrSearchThreads / TranspositionTable.BYTES_PER_ENTRY);
    }

    void testThreadForEachMove() throws InterruptedException {
        int player = PLAYER_0;

        Searcher searcher = new Searcher(new SearchParameters(1, Searcher.MAX_NOMINAL_DEPTH, 0), Utils.NO_LOGGER, false);
        searcher.setState(player, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon));
        Output.printBoard(searcher);
        List<Pair<Move, GameState>> movesToTest = searcher.getAllMoves();

        if (movesToTest.isEmpty()) {
            System.out.println("No moves available!");
            return;
        }

        int ttBits = getTTBits(movesToTest.size());
        System.out.printf("%nAvailable moves = %d%nTT bits = %d (%.2f GB)%n", movesToTest.size(), ttBits, movesToTest.size() * (1L << ttBits) * TranspositionTable.BYTES_PER_ENTRY / 1024.0 / 1024.0 / 1024.0);

        System.out.println("-------------");

        long time = System.currentTimeMillis();

        List<SearchThread> searchThreads = new ArrayList<>();
        for (Pair<Move, GameState> move : movesToTest) {
            SearchThread t = new SearchThread(move.p, ttBits, player, move.q.board, move.q.cardState);
            t.start();
            searchThreads.add(t);
        }

        List<Pair<Integer, String>> completedStats = new ArrayList<>();

        while (!searchThreads.isEmpty()) {
            Thread.sleep(1000);

            List<Pair<Integer, String>> threadStats = new ArrayList<>();
            for (Iterator<SearchThread> it = searchThreads.iterator(); it.hasNext(); ) {
                SearchThread t = it.next();
                Pair<Integer, String> stats = t.getStats();
                if (t.threadFinished) {
                    completedStats.add(stats);
                    it.remove();
                } else {
                    threadStats.add(stats);
                }
            }
            threadStats.addAll(completedStats);

            threadStats.sort((a, b) -> a.p.compareTo(b.p));
            threadStats.forEach(stats -> println(stats.q));

            // see if we should resize the TTs
            int newTTBits = getTTBits(searchThreads.size());
            if (newTTBits != ttBits) {
                ttBits = newTTBits;
                searchThreads.forEach(t -> t.searcher.resizeTTAsync(newTTBits));
            }
            System.out.printf("Search threads = %d  -  TT bits = %d (%.2f GB)%n", searchThreads.size(), ttBits, searchThreads.size() * (1L << ttBits) * TranspositionTable.BYTES_PER_ENTRY / 1024.0 / 1024.0 / 1024.0);


            println("---------------------");
        }

        time = System.currentTimeMillis() - time;
        System.out.printf("%nElapsed time: %d ms%n", time);
    }

    class SearchThread extends Thread {
        final Move initialMove;
        final int player;
        final String board;
        final CardState cardState;

        private Searcher searcher;
        boolean threadFinished = false;

        SearchThread(Move initialMove, int ttBits, int player, String board, CardState cardState) {
            this.initialMove = initialMove;
            this.player = player;
            this.board = board;
            this.cardState = cardState;

            searcher = new Searcher(new SearchParameters(ttBits, Searcher.MAX_NOMINAL_DEPTH, 1000000), Utils.NO_LOGGER, false);
            searcher.setState(1 - player, board, cardState);
        }

        @Override public void run() {
            searcher.start();

            threadFinished = true;
        }

        Pair<Integer, String> getStats() {
            int depth = searcher.getScoreSearchNominalDepth();
            int score = searcher.getScore();
            long states = searcher.stats.getStatesEvaluated();
            long qStates = searcher.stats.getQuiescenceStatesEvaluated();
            return new Pair<>(score, String.format("%s: %d (%d plies, %d states)", initialMove, score, depth, states + qStates));
        }
    }

    public static void println() {
        print("\n");
    }

    public static void println(String string) {
        print(string + "\n");
    }

    public static void printf(String format, Object... args) {
        print(String.format(format, args));
    }

    public static synchronized void print(String string) {
        System.out.print(string);
    }
}
