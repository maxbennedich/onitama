package onitama.tuning;

import static onitama.model.GameDefinition.NR_PIECE_TYPES;
import static onitama.tuning.TuningUtils.NR_PHASES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import onitama.ai.SearchState;
import onitama.ai.evaluation.CenterPriorityEvaluator;
import onitama.ai.evaluation.Evaluator;
import onitama.model.Card;
import onitama.model.GameState;
import onitama.model.SearchParameters;
import onitama.ui.console.Output;
import onitama.ui.console.Output.OutputLevel;

/**
 * Implementation of SPSA (Simultaneous Perturbation Algorithm for Stochastic Optimization).
 * For details, see this paper: http://www.jhuapl.edu/spsa/PDF-SPSA/Spall_Implementation_of_the_Simultaneous.PDF
 * <p/>
 * There is one score for each combination of board position, card, piece type (pawn / king), and game phase (middle / end game).
 * For a horizontally self-symmetrical card, we need 15 scores for the board. For a pair of symmetrical cards, we need 25 scores
 * (both cards use mirrored versions of the same scores).
 * <p/>
 * For the original set of 16 cards, with 2 game phases, 2 piece types, 8 self-symmetrical cards, and 4 symmetrical card pairs,
 * there are thus 2 * 2 * (8 * 15 + 4 * 25) = 880 scores.
 * <p/>
 * A few implementation details:
 * <ul>
 * <li>The difference in loss function (yplus - yminus in Spall's paper) is calculated by playing the two perturbed score sets
 * against each other many times and using the average Elo difference, as opposed to calculating the loss for each score set
 * individually. This method is also used in Stockfish's tuning method, as detailed here:
 * https://chessprogramming.wikispaces.com/Stockfish%27s+Tuning+Method</li>
 * <li>To reduce the search space, cards take turns to be tuned individually, and all games are ensured to include the currently
 * tuned card during evaluation. I.e., first we tune card 1 for 20 iterations, during which all games played have to
 * include card 1, then we tune card 2 for 20 iterations, etc, and once all cards have been tuned, we start over with card 1.
 * The reason that this makes sense is that during any game, only the scores for the 5 cards used will matter, so if
 * all cards are tuned at the same time, the signal-to-noise ratio will be much lower. Experiments showed that this method
 * led to a faster convergence rate.</li>
 * <li>Parallelization is implemented during the evaluation phase of a given set of scores, by starting separate threads that
 * all play with random cards. Experiments were done by parallelizing the perturbation itself, by starting one
 * thread for each card to be tuned. While that led to faster throughput for a large number of threads (>12) due to the
 * overhead of starting new threads too frequently, the overall convergence was a lot slower, suggesting that it's better
 * to tune cards one at a time, than together in "random" order. This is somewhat supported by Spall's paper (and by
 * references therein) where it's suggested that reducing the amount of randomness can increase the convergence rate.</li>
 * </ul>
 */
public class SpsaTuner {
    /** Number of parallel threads to use in the tuning (can be overridden by argument to the program). */
    private static int nrThreads = 4;

    private static final int SEARCH_DEPTH = 4; // Should ideally be higher, but the search time grows exponentially with the depth. Make it even for the same number of moves per player.
    private static final int GAMES_PER_PARAMETER_CHANGE = 1000; // enough games to reduce the noise in measuring AI performance
    private static final int ITERATIONS_PER_CARD = 20;

    // SPSA constants (names from Spall's paper)
    private static final int A = 100; // roughly 10% (or less) of total number of iterations
    private static final double a = 2.0; // aggressiveness of theta updates
    private static final int c = 4; // initial score perturbation
    private static final double ALPHA = 0.602; // lowest possible value to guarantee convergence (from Spall's paper)
    private static final double GAMMA = 0.101; // lowest possible value to guarantee convergence (from Spall's paper)

    public static void main(String ... args) throws Exception {
        Output.outputLevel = OutputLevel.NONE;
        GameState.MAX_PLIES_BEFORE_DRAW = 120; // to speed up evaluation; games reaching this many plies are most likely draws

        if (args.length > 0)
            nrThreads = Integer.parseInt(args[0]);
        System.out.println("Threads = " + nrThreads);

        List<Integer> cardThetaOffset = new ArrayList<>();
        List<Integer> cardParameterCount = new ArrayList<>();
        int thetaOffset = 0;

        for (Card card : Card.CARDS) {
            int count = NR_PHASES * NR_PIECE_TYPES * (card.isSelfSymmetrical() ? 15 : card.isFirstMirroredCard() ? 25 : 0);

            if (count > 0) {
                cardThetaOffset.add(thetaOffset);
                cardParameterCount.add(count);
                thetaOffset += count;
            }
        }

        // total number of parameters to tune
        final int P = thetaOffset;

        // initial values
        double[] theta = new double[P];
        Arrays.fill(theta, 0);

        long turnsPlayed = 0;
        long nodesEvaluated = 0;
        Random rnd = new Random(0);

        for (int kOuter = 0, iterations = 1; ; ++kOuter) {
            for (int card = 0; card < cardThetaOffset.size(); ++card) {
                for (int kInner = 0; kInner < ITERATIONS_PER_CARD; ++kInner, ++iterations) {
                    if (kInner % 10 == 0)
                        System.out.printf("Current Elo gain: %.2f%n%n", evaluateParameters(theta, 0).eloDifference(0));

                    int k = kOuter * ITERATIONS_PER_CARD + kInner + 1;

                    double ak = a / Math.pow(k + A, ALPHA);
                    double ck = c / Math.pow(k, GAMMA);

                    int p = cardParameterCount.get(card);
                    int ofs = cardThetaOffset.get(card);

                    double[] delta = TuningUtils.bernoulliRandom(rnd, p);

                    double[][] thetaPerturbed = new double[2][P];
                    for (int sign = 0; sign < 2; ++sign) {
                        System.arraycopy(theta, 0, thetaPerturbed[sign], 0, P);
                        for (int i = 0; i < p; ++i)
                            thetaPerturbed[sign][i+ofs] += ck * delta[i] * (sign == 0 ? 1 : -1);
                    }

                    MultiGameResult result = compareAIs(thetaPerturbed, Card.CARDS[card], rnd);
                    double elo = result.eloDifference(0);
                    turnsPlayed += result.plies;
                    nodesEvaluated += result.nodesEvaluated;

                    for (int i = 0; i < p; ++i)
                        theta[i+ofs] += ak * elo / (2 * ck * delta[i]);

                    TuningUtils.printPst(TuningUtils.getCardPhasePiecePst(theta));
                    System.out.printf("Elo = %.1f, iterations = %d, card = %d, k = %d (%d / %d), ak = %.4f, ck = %.2f, delta factor = %.2f%n", elo, iterations, card, k, kOuter, kInner, ak, ck, ak * elo / (2 * ck));
                    System.out.println("Games played = " + iterations * 2 * GAMES_PER_PARAMETER_CHANGE);
                    System.out.println("Turns played = " + turnsPlayed);
                    System.out.println("Nodes evaluated = " + nodesEvaluated + "\n");
                }
            }
        }
    }

    private static MultiGameResult evaluateParameters(double[] theta, long cardSelectionRandomSeed) {
        SearchParameters[] searchParameters = { getSearchParameters(theta), getSearchParameters(state -> new CenterPriorityEvaluator(state)) };
        return TuningUtils.testAIs(searchParameters, null, GAMES_PER_PARAMETER_CHANGE, nrThreads, new Random(cardSelectionRandomSeed), false);
    }

    private static MultiGameResult compareAIs(double[][] theta, Card cardThatMustBePresent, Random rnd) {
        SearchParameters[] searchParameters = { getSearchParameters(theta[0]), getSearchParameters(theta[1]) };
        return TuningUtils.testAIs(searchParameters, cardThatMustBePresent, GAMES_PER_PARAMETER_CHANGE, nrThreads, rnd, false);
    }

    private static SearchParameters getSearchParameters(double[] theta) {
        return getSearchParameters(state -> new TuningEvaluator(state, TuningUtils.getCardPhasePiecePst(theta)));
    }

    private static SearchParameters getSearchParameters(Function<SearchState, Evaluator> evaluator) {
        return new SearchParameters(10, SEARCH_DEPTH, 100000, evaluator);
    }
}
