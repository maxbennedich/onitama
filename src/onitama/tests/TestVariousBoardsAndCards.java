package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;

/*
 */
public class TestVariousBoardsAndCards {

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

    static String BOARD_CORNERS =
            "bb..." +
            "B...w" +
            "b...w" +
            "b...W" +
            "...ww";

    static String BOARD_WIN_AT_18 =
            "....." +
            "..Bb." +
            "..b.." +
            ".ww.." +
            "...W.";

    static String[] BOARDS = {EMPTY_BOARD, BOARD_WIN_AT_13, BOARD_CORNERS, BOARD_WIN_AT_18};
    static int[] DEPTHS = {11, 11, 11, 13};

    static CardState[] CARDS = {
            new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon),
            new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Elephant, Card.Boar}}, Card.Cobra),
            new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Eel, Card.Crab}}, Card.Dragon),
    };

    public static void main(String ... args) throws Exception {
        Searcher.LOGGING = false;

        new TestVariousBoardsAndCards().testBoard();
    }

    private void testBoard() {
        long totalTime = System.currentTimeMillis();
        long totalStates = 0, totalQStates = 0;

        for (int board = 0; board < BOARDS.length; ++board) {
            for (int cards = 0; cards < CARDS.length; ++cards) {
                Searcher searcher = new Searcher(DEPTHS[board], 26);

                searcher.setState(0, BOARDS[board], CARDS[cards]);

                long time = System.currentTimeMillis();
                int score = searcher.start(Integer.MAX_VALUE);
                time = System.currentTimeMillis() - time;

                long states = searcher.stats.getStatesEvaluated();
                long qStates = searcher.stats.getQuiescenceStatesEvaluated();
                totalStates += states;
                totalQStates += qStates;

                System.out.printf("Depth %d, board %d, cards %d: score = %d, states = %d + %d, time = %d ms%n", DEPTHS[board], board, cards, score, states, qStates, time);
            }
        }
        System.out.println();

        System.out.printf("Total states: %d + %d = %d%n", totalStates, totalQStates, totalStates+totalQStates);
        System.out.printf("Total time: %d ms%n", System.currentTimeMillis() - totalTime);
    }
}
