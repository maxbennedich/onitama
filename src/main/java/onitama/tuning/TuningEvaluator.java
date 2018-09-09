package onitama.tuning;

import static onitama.model.GameDefinition.NN;
import static onitama.model.GameDefinition.NR_PLAYERS;

import onitama.ai.SearchState;
import onitama.ai.evaluation.Evaluator;

/** Evaluator used during experimentation and parameter tuning. */
public class TuningEvaluator extends Evaluator {
    private final double[][][][] pst;

    public TuningEvaluator(SearchState state, double[][][][] pst) {
        super(state);
        this.pst = pst;
    }

    @Override public int score(int playerToEvaluate) {
        double score = 0;

        // phase ranges from 0 (end game) to 1 (middle game)
        int totalMaterial = Integer.bitCount(state.bitboardPlayer[0]) + Integer.bitCount(state.bitboardPlayer[1]);
        double phase = (totalMaterial - 4) / 6.0;
        if (phase < 0) phase = 0;

        for (int player = 0; player < NR_PLAYERS; ++player) {
            int card0 = state.cardMapping[(state.cardBits >> 3 + 0 + player*6)&7];
            int card1 = state.cardMapping[(state.cardBits >> 3 + 3 + player*6)&7];

            for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
                playerBitmask >>= (pz+1);
                if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
                p += pz + 1;

                int piece = (state.bitboardKing[player] >> p) & 1;
                int pos = player == 0 ? p : NN - 1 - p;
                double mg = pst[card0][0][piece][pos] + pst[card1][0][piece][pos];
                double eg = pst[card0][1][piece][pos] + pst[card1][1][piece][pos];
                score += (1 - player*2) * (SCORE_PER_PIECE + phase * mg + (1 - phase) * eg);
            }
        }

        int scoreDifference = (int)Math.round(score);

        return playerToEvaluate == 0 ? scoreDifference : -scoreDifference;
    }
}