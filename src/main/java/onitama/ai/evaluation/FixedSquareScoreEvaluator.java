package onitama.ai.evaluation;

import static onitama.model.GameDefinition.NN;
import static onitama.model.GameDefinition.NR_PLAYERS;

import onitama.ai.SearchState;

/**
 * Evaluator which assigns a fixed score to each square on the board.
 * Experiments were carried out optimizing the scores for this evaluator with SPSA,
 * but ultimately using a more complicated evaluation method with separate tables
 * per phase / card / piece yielded much better results.
 */
public class FixedSquareScoreEvaluator extends Evaluator {
    private final double[] scoreByPosition;

    public FixedSquareScoreEvaluator(SearchState state, double[] scoreByPosition) {
        super(state);
        this.scoreByPosition = scoreByPosition;

        if (scoreByPosition.length != NN)
            throw new AssertionError(scoreByPosition.length);
    }

    @Override public int score(int playerToEvaluate) {
        double score = 0;

        for (int player = 0; player < NR_PLAYERS; ++player) {
            for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
                playerBitmask >>= (pz+1);
                if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
                p += pz + 1;

                if (player == 0)
                    score += SCORE_PER_PIECE + scoreByPosition[p];
                else
                    score -= SCORE_PER_PIECE + scoreByPosition[NN-1-p];
            }
        }

        int scoreDifference = (int)Math.round(score);
        return playerToEvaluate == 0 ? scoreDifference : -scoreDifference;
    }
}
