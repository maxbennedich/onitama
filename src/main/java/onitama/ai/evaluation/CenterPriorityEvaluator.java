package onitama.ai.evaluation;

import onitama.ai.SearchState;

/**
 * Simple and highly efficient evaluator which assigns higher scores to pieces close to the center of the board.
 * This evaluator was used before automated tuning was able to come up with superior evaluators.
 * It's also used as a benchmark evaluator, to compare the performance of other evaluators.
 */
public class CenterPriorityEvaluator extends Evaluator {
    public CenterPriorityEvaluator(SearchState state) {
        super(state);
    }

    // Score for each position on the board. (Larger score is better.)
    private static final int SCORE_1 = 0b01010_10001_00000_10001_01010;
    private static final int SCORE_2 = 0b00100_01010_10001_01010_00100;
    private static final int SCORE_3 = 0b00000_00100_01010_00100_00000;
    private static final int SCORE_4 = 0b00000_00000_00100_00000_00000;

    @Override public int score(int playerToEvaluate) {
        int pieceScore0 =
                Integer.bitCount(state.bitboardPlayer[0] & SCORE_1) +
                2 * Integer.bitCount(state.bitboardPlayer[0] & SCORE_2) +
                3 * Integer.bitCount(state.bitboardPlayer[0] & SCORE_3) +
                4 * Integer.bitCount(state.bitboardPlayer[0] & SCORE_4);

        int pieceScore1 =
                Integer.bitCount(state.bitboardPlayer[1] & SCORE_1) +
                2 * Integer.bitCount(state.bitboardPlayer[1] & SCORE_2) +
                3 * Integer.bitCount(state.bitboardPlayer[1] & SCORE_3) +
                4 * Integer.bitCount(state.bitboardPlayer[1] & SCORE_4);

        int materialDifference = Integer.bitCount(state.bitboardPlayer[0]) - Integer.bitCount(state.bitboardPlayer[1]);

        int score = materialDifference * SCORE_PER_PIECE + (pieceScore0 - pieceScore1)*1;

        return playerToEvaluate == 0 ? score : -score;
    }
}