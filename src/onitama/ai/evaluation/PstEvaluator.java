package onitama.ai.evaluation;

import static onitama.model.GameDefinition.NN;
import static onitama.model.GameDefinition.NR_PIECE_TYPES;
import static onitama.model.GameDefinition.NR_PLAYERS;

import onitama.ai.SearchState;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.GameDefinition;

/**
 * This evaluator has one score per combination of board position, card, piece type (pawn / king), and game phase (middle / end game).
 * This type of table is commonly referred to as a Piece Square Table, or PST for short.
 * <p>
 * A significant amount of the total search time (~15 %) is spent in this method, and some effort has gone into optimizing it:
 * <ul>
 * <li>Board positions are looped over in a bitwise way (just like in the {@link MoveGenerator}).</li>
 * <li>The PST scores have been converted to fixed point integers to avoid floating point math.</li>
 * <li>Tables are pre-calculated for each of the 7 possible phases, instead of only storing one middle game table and one
 * end game table, and interpolating between the two.</li>
 * <li>The multidimensional PST table has been converted to a single array for more efficient indexing.</li>
 * <li>The PST scores are stored as 16 bit shorts, to better fit the table in cache.</li>
 * <li>The PST table has been rearranged for better cache locality (phase / card / piece / position).</li>
 * </ul>
 * Since the player cards change each turn, and the scores depend on the cards assigned, it's not particularly efficient to update
 * the score during the move / unmove.
 */
public class PstEvaluator extends Evaluator {
    private static final short[] PST = optimizePst(PieceSquareTables.CARD_PHASE_PIECE_POSITION_RAW_SCORES);

    private static final int NR_PHASES = 7;

    private static final int FIXED_POINT_FRACTIONAL_BITS = 9;
    private static final int FIXED_POINT_MULTIPLIER = 1 << FIXED_POINT_FRACTIONAL_BITS;
    private static final int FIXED_POINT_HALF = FIXED_POINT_MULTIPLIER / 2;

    public PstEvaluator(SearchState state) {
        super(state);
    }

    @Override public int score(int playerToEvaluate) {
        int score = FIXED_POINT_HALF; // for rounding

        // phase ranges from 0 (end game) to 6 (middle game)
        int totalMaterial = Integer.bitCount(state.bitboardPlayer[0]) + Integer.bitCount(state.bitboardPlayer[1]);
        int phase = totalMaterial - 4;
        if (phase < 0) phase = 0;
        phase *= Card.NR_CARDS * NR_PIECE_TYPES * NN;

        for (int player = 0; player < NR_PLAYERS; ++player) {
            int card0 = state.cardMapping[(state.cardBits >> 3 + 0 + player*6)&7] * NR_PIECE_TYPES * NN;
            int card1 = state.cardMapping[(state.cardBits >> 3 + 3 + player*6)&7] * NR_PIECE_TYPES * NN;

            for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
                playerBitmask >>= (pz+1);
                if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
                p += pz + 1;

                int piece = (state.bitboardKing[player] >> p) & 1;
                int pos = player == 0 ? p : NN - 1 - p;
                int idx = phase + piece * NN + pos;
                score += (1 - player*2) * ((SCORE_PER_PIECE << FIXED_POINT_FRACTIONAL_BITS) + (int)PST[idx + card0] + (int)PST[idx + card1]);
            }
        }

        int scoreDifference = score >> FIXED_POINT_FRACTIONAL_BITS;

        return playerToEvaluate == 0 ? scoreDifference : -scoreDifference;
    }

    @Override public String explain() {
        StringBuilder sb = new StringBuilder();

        // phase ranges from 0 (end game) to 6 (middle game)
        int totalMaterial = Integer.bitCount(state.bitboardPlayer[0]) + Integer.bitCount(state.bitboardPlayer[1]);
        int phase = totalMaterial - 4;
        if (phase < 0) phase = 0;

        sb.append(String.format("Total material: %d%nGame phase: %d (0 = End game, 6 = Middle game)%n", totalMaterial, phase));
        phase *= Card.NR_CARDS * NR_PIECE_TYPES * NN;

        int totalScore = 0;
        int material = 0;

        for (int player = 0; player < NR_PLAYERS; ++player) {
            int card0 = state.cardMapping[(state.cardBits >> 3 + 0 + player*6)&7];
            int card1 = state.cardMapping[(state.cardBits >> 3 + 3 + player*6)&7];

            sb.append(String.format("%n---+%s+-------%n", Utils.centerString(" " + GameDefinition.PLAYER_COLOR[player] + " ", 31, '-')));
            sb.append(String.format("   |  %s|  %s|%n",
                    Utils.centerString(Card.CARDS[card0].name, 13),
                    Utils.centerString(Card.CARDS[card1].name, 13)));
            sb.append("Pos|    MG     EG  |    MG     EG  |  Score\n");
            sb.append("---+---------------+---------------+-------\n");

            card0 *= NR_PIECE_TYPES * NN;
            card1 *= NR_PIECE_TYPES * NN;

            int playerScore = 0;

            for (int playerBitmask = state.bitboardPlayer[player], p = -1, pz = -1; ; ) {
                playerBitmask >>= (pz+1);
                if ((pz = Integer.numberOfTrailingZeros(playerBitmask)) == 32) break;
                p += pz + 1;

                int piece = (state.bitboardKing[player] >> p) & 1;
                int pos = player == 0 ? p : NN - 1 - p;
                int idx = phase + piece * NN + pos;

                int mg0 = PST[card0 + piece * NN + pos + (NR_PHASES - 1) * Card.NR_CARDS * NR_PIECE_TYPES * NN];
                int eg0 = PST[card0 + piece * NN + pos];
                int mg1 = PST[card1 + piece * NN + pos + (NR_PHASES - 1) * Card.NR_CARDS * NR_PIECE_TYPES * NN];
                int eg1 = PST[card1 + piece * NN + pos];

                int score = (int)PST[idx + card0] + (int)PST[idx + card1];
                totalScore += (1 - player*2) * ((SCORE_PER_PIECE << FIXED_POINT_FRACTIONAL_BITS) + score);
                playerScore += score;
                material += (1 - player*2) * SCORE_PER_PIECE;

                sb.append(String.format("%s |%7.2f%7.2f |%7.2f%7.2f |%7.2f%n",
                        GameDefinition.getPosition(p),
                        mg0 / (double)FIXED_POINT_MULTIPLIER,
                        eg0 / (double)FIXED_POINT_MULTIPLIER,
                        mg1 / (double)FIXED_POINT_MULTIPLIER,
                        eg1 / (double)FIXED_POINT_MULTIPLIER,
                        score / (double)FIXED_POINT_MULTIPLIER));
            }

            sb.append("---+---------------+---------------+-------\n");
            sb.append(String.format("%s total score: %.2f%n", GameDefinition.PLAYER_COLOR[player], playerScore / (double)FIXED_POINT_MULTIPLIER));
        }

        String player0 = GameDefinition.PLAYER_COLOR[0];
        int finalScore = totalScore >> FIXED_POINT_FRACTIONAL_BITS;
        sb.append(String.format("%nMaterial bonus: %d (%s)%n", material, player0));
        sb.append(String.format("Final score: %d (%s)%n", finalScore, player0));

        // ensure that the actual score method returns the same answer as this
        int actualScore = score(0);
        if (finalScore != actualScore)
            throw new AssertionError("Got " + finalScore + ", expected " + actualScore);

        return sb.toString();
    }

    /**
     * Convert a [card][phase][piece][position] double PST (output from tuner) to an optimized PST for use with this evaluator.
     * See {@link PstEvaluator} for details on the optimizations done.
     */
    private static short[] optimizePst(double[][][][] pst) {
        short[] optimized = new short[NR_PHASES * Card.NR_CARDS * NR_PIECE_TYPES * NN];

        for (int a = 0; a < Card.NR_CARDS; ++a) {
            for (int phase = 0; phase < NR_PHASES; ++phase) {
                for (int c = 0; c < NR_PIECE_TYPES; ++c) {
                    for (int d = 0; d < NN; ++d) {
                        double mg = pst[a][0][c][d];
                        double eg = pst[a][1][c][d];
                        double p = phase / 6.0;

                        int v = (int)Math.round(((p * mg + (1 - p) * eg) * FIXED_POINT_MULTIPLIER));
                        if (v < Short.MIN_VALUE || v > Short.MAX_VALUE)
                            throw new IllegalArgumentException("Value out of range: " + v);

                        optimized[phase * Card.NR_CARDS * NR_PIECE_TYPES * NN + a * NR_PIECE_TYPES * NN + c * NN + d] = (short)v;
                    }
                }
            }
        }

        return optimized;
    }
}