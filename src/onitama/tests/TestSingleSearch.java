package onitama.tests;

import onitama.Card;
import onitama.Searcher;

public class TestSingleSearch {
    static final int TT_BITS = 26; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 13;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String EMPTY_BOARD =
            "ooQoo" +
            "....." +
            "....." +
            "....." +
            "xx#xx";

    static String BOARD_WIN_AT_13 =
            "o.Qoo" +
            "....." +
            ".o..." +
            ".xx#." +
            "x...x";

    public static void main(String ... args) throws Exception {
        Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS);

        searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon); // use this for testing!
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new Card[][] {{Card.Tiger, Card.Rabbit}, {Card.Rooster, Card.Cobra}}, Card.Elephant); // fast
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new Card[][] {{Card.Tiger, Card.Cobra}, {Card.Rabbit, Card.Rooster}}, Card.Elephant); // slow
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon); // use this for testing!

        System.out.printf("Transposition table size: %d entries (%.0f MB)%n", searcher.tt.sizeEntries(), searcher.tt.sizeBytes() / 1024.0 / 1024.0);

        searcher.printBoard();

        long time = System.currentTimeMillis();

        searcher.start(Integer.MAX_VALUE);

        time = System.currentTimeMillis() - time;

        System.out.println();
        searcher.printStats();

        System.out.printf("%nElapsed time: %d ms%n", time);
    }
}
