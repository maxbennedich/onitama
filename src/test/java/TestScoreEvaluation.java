package onitama.tests;

import onitama.ai.SearchState;
import onitama.ai.evaluation.PstEvaluator;
import onitama.model.Card;
import onitama.model.CardState;

public class TestScoreEvaluation {
    static String BOARD =
            "..B.." +
            "...b." +
            ".bbw." +
            "wW..." +
            ".w..w";

    public static void main(String ... args) throws Exception {
        SearchState state = new SearchState(s -> new PstEvaluator(s));
        state.initPlayer(0);
        state.initBoard(BOARD);
        state.initCards(new CardState(new Card[][] {{ Card.Boar, Card.Eel }, { Card.Elephant, Card.Tiger }} , Card.Cobra));
        state.printBoard();
        System.out.println();
        System.out.println(state.scoreExplanation());
    }
}
