package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;

public class TestGrowingTT {
    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String BOARD_GAME =
            "....." +
            "..Bb." +
            "..b.." +
            ".ww.." +
            "...W.";

    static String BOARD_GAME_MAX_9 =
            "b.B.." +
            "...bb" +
            "..bw." +
            "wW..w" +
            ".w...";

    public static void main(String ... args) throws Exception {
        int ttBits = 14;

        SearchThread t = new SearchThread(ttBits);
        t.start();

        while (!t.threadFinished) {
            Thread.sleep(500);
            if (ttBits < 24) {
                System.out.println("RESIZE");
                t.searcher.resizeTTAsync(++ttBits);
            }
        }
    }

    static class SearchThread extends Thread {
        private Searcher searcher;

        volatile boolean threadFinished = false;

        SearchThread(int ttBits) {
            searcher = new Searcher(50, ttBits);
//            searcher.setState(PLAYER_0, BOARD_GAME, new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Eel, Card.Crab}}, Card.Dragon));
            searcher.setState(PLAYER_1, BOARD_GAME_MAX_9, new CardState(new Card[][] {{Card.Ox, Card.Crane}, {Card.Horse, Card.Tiger}}, Card.Eel));
        }

        @Override public void run() {
            long time = System.currentTimeMillis();

            searcher.start(1000000);

            time = System.currentTimeMillis() - time;

            threadFinished = true;

            System.out.println();
            searcher.stats.print();
            System.out.printf("%nElapsed time: %d ms%n", time);
        }
    }
}
