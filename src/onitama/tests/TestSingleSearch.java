package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.ui.console.Output;
import onitama.ui.console.UIUtils;

public class TestSingleSearch {
    static final int TT_BITS = 26; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 22;

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

    static String BOARD_GAME =
            "....." +
            "..Bb." +
            "..b.." +
            ".ww.." +
            "...W.";

    static String BOARD_GAME_2 =
            "....." +
            "..Bb." +
            "..w.." +
            "..w.." +
            "...W.";

    static String BOARD_GAME_MAX_9 =
            "b.B.." +
            "...bb" +
            "..bw." +
            "wW..w" +
            ".w...";

    public static void main(String ... args) throws Exception {
        Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS, 20000001, true, UIUtils.CONSOLE_LOGGER, true);

//        searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon)); // use this for testing!
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Tiger, Card.Rabbit}, {Card.Rooster, Card.Cobra}}, Card.Elephant)); // fast
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Tiger, Card.Cobra}, {Card.Rabbit, Card.Rooster}}, Card.Elephant)); // slow
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon)); // use this for testing!
//        searcher.setState(PLAYER_0, BOARD_GAME, new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Eel, Card.Crab}}, Card.Dragon));
//        searcher.setState(PLAYER_1, BOARD_GAME_2, new CardState(new Card[][] {{Card.Dragon, Card.Frog}, {Card.Eel, Card.Crab}}, Card.Monkey));
//        searcher.setState(PLAYER_1, BOARD_GAME_8, new CardState(new Card[][] {{Card.Eel, Card.Frog}, {Card.Dragon, Card.Monkey}}, Card.Crab));
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Elephant, Card.Boar}}, Card.Cobra)); // from Depth test
        searcher.setState(PLAYER_1, BOARD_GAME_MAX_9, new CardState(new Card[][] {{Card.Ox, Card.Crane}, {Card.Horse, Card.Tiger}}, Card.Eel));

//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Crab, Card.Frog}, {Card.Crane, Card.Horse}}, Card.Tiger)); // use this for testing!
//        searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.Crane, Card.Horse}, {Card.Frog, Card.Eel}}, Card.Monkey)); // use this for testing!

        Output.printBoard(searcher);

        long time = System.currentTimeMillis();

        searcher.start();
//        searcher.start(Integer.MAX_VALUE);

        time = System.currentTimeMillis() - time;

        System.out.println();
        searcher.logTTSize();
        searcher.stats.print();

        System.out.printf("%nElapsed time: %d ms%n", time);
    }
}
