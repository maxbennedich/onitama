package onitama.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Pair;

public class TestMultipleSearchThreads {
    static final int TT_BITS = 24; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String BOARD_WIN_AT_13 =
            "b.Bbb" +
            "....." +
            ".b..." +
            ".wwW." +
            "w...w";

    public static void main(String ... args) throws Exception {
        new TestMultipleSearchThreads().testThreadForEachMove();
    }

    void testThreadForEachMove() throws InterruptedException {
        int player = PLAYER_0;

        Searcher.LOGGING = false;

        Searcher searcher = new Searcher(50, 1);
        searcher.setState(player, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon));
        searcher.printBoard();
        List<Pair<String, GameState>> movesToTest = searcher.getAllMoves();

        System.out.println("-------------");

        long time = System.currentTimeMillis();

        List<SearchThread> searchThreads = new ArrayList<>();
        for (Pair<String, GameState> move : movesToTest) {
            SearchThread t = new SearchThread(move.p, player, move.q.board, move.q.cardState);
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
            println("---------------------");
        }

        time = System.currentTimeMillis() - time;
        System.out.printf("%nElapsed time: %d ms%n", time);
    }

    class SearchThread extends Thread {
        final String initialMove;
        final int player;
        final String board;
        final CardState cardState;

        private Searcher searcher;
        boolean threadFinished = false;

        SearchThread(String initialMove, int player, String board, CardState cardState) {
            this.initialMove = initialMove;
            this.player = player;
            this.board = board;
            this.cardState = cardState;

            searcher = new Searcher(50, TT_BITS);
            searcher.setState(1 - player, board, cardState);
        }

        @Override public void run() {
            searcher.start(1000000);

            threadFinished = true;
        }

        Pair<Integer, String> getStats() {
            int depth = searcher.getPVScoreDepth();
            int score = searcher.getPVScore();
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
