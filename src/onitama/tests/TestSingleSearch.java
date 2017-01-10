package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;

public class TestSingleSearch {
    static final int TT_BITS = 26; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 13;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String EMPTY_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    static String BOARD_WIN_AT_13 =
            "b.Bbb" +
            "....." +
            ".b..." +
            ".wwW." +
            "w...w";

    static String BOARD_GAME_7 =
            "..B.b" +
            "...w." +
            "..b.." +
            ".w.w." +
            "..W..";

    static String BOARD_GAME_8 = // win after 15
            "....b" +
            "...B." +
            "..bw." +
            ".w..." +
            "..W..";

    public static void main(String ... args) throws Exception {
        Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS);

        searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon)); // use this for testing!
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Tiger, Card.Rabbit}, {Card.Rooster, Card.Cobra}}, Card.Elephant)); // fast
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Tiger, Card.Cobra}, {Card.Rabbit, Card.Rooster}}, Card.Elephant)); // slow
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon)); // use this for testing!
//        searcher.setState(PLAYER_1, BOARD_GAME_7, new CardState(new Card[][] {{Card.Crab, Card.Frog}, {Card.Eel, Card.Monkey}}, Card.Dragon)); // use this for testing!
//        searcher.setState(PLAYER_1, BOARD_GAME_8, new CardState(new Card[][] {{Card.Eel, Card.Frog}, {Card.Dragon, Card.Monkey}}, Card.Crab)); // use this for testing!

        System.out.printf("Transposition table size: %d entries (%.0f MB)%n", searcher.tt.sizeEntries(), searcher.tt.sizeBytes() / 1024.0 / 1024.0);

        searcher.printBoard();

        long time = System.currentTimeMillis();

        searcher.start(10000);
//        searcher.start(Integer.MAX_VALUE);

        time = System.currentTimeMillis() - time;

        System.out.println();
        searcher.printStats();

        System.out.printf("%nElapsed time: %d ms%n", time);
    }
}
