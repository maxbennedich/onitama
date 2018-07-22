package onitama.tuning;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;
import static onitama.model.GameDefinition.NR_PIECE_TYPES;

import java.util.Random;

import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.SearchParameters;
import onitama.ui.AIPlayer;
import onitama.ui.Player;
import onitama.ui.console.GameSimulator;
import onitama.ui.console.GameSimulator.GameResult;

public class TuningUtils {
    /** Middle game, End game */
    static final int NR_PHASES = 2;

    public static MultiGameResult testAIs(SearchParameters[] searchParameters, Card cardThatMustBePresent, int games, int threadCount, Random cardSelectionRandom, boolean log) {
        MultiGameResult results = new MultiGameResult();

        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; ++t) {
            final int threadNr = t;

            threads[t] = new Thread(() ->
            {
                for (int n = games * threadNr / threadCount; n < games * (threadNr + 1) / threadCount; ++n) {
                    CardState cardSelection = getRandomCards(cardThatMustBePresent, cardSelectionRandom);

                    Player[] ais = new Player[2];
                    for (int ai0Player = 0; ai0Player < 2; ++ai0Player) {
                        for (int player = 0; player < 2; ++player)
                            ais[player] = new AIPlayer(player, searchParameters[player ^ ai0Player], false, Utils.NO_LOGGER);

                        GameResult gameResult = new GameSimulator(ais, new GameState(GameState.INITIAL_BOARD, cardSelection)).play();

                        synchronized (results) {
                            results.add(gameResult, ai0Player);
                            if (log)
                                System.out.println(results);
                        }
                    }
                }
            });
        }

        for (Thread thread : threads)
            thread.start();

        for (Thread thread : threads)
            Utils.joinAndLogException(thread);

        return results;
    }

    private static CardState getRandomCards(Card cardThatMustBePresent, Random cardSelectionRandom) {
        // synchronize so that the same random cards will be drawn every time the program is run
        synchronized (cardSelectionRandom) {
            // not the most efficient implementation, but good enough since this is not performance critical at all
            while (true) {
                CardState cardSelection = CardState.random(cardSelectionRandom, new boolean[] { true, false, false });
                if (cardThatMustBePresent == null)
                    return cardSelection;

                Card[] cards = cardSelection.cards();
                for (Card card : cards)
                    if (card.isSameOrMirrored(cardThatMustBePresent))
                        return cardSelection;
            }
        }
    }

    static void printPst(double[][][][] pst) {
        for (int card = 0; card < Card.NR_CARDS; ++card) {
            System.out.printf("Card %d (%s)%n", card, Card.CARDS[card]);
            for (int phase = 0; phase < NR_PHASES; ++phase) {
                System.out.println((phase == 0 ? "Middle" : "End") + " game");
                printPst(pst[card][phase]);
            }
        }
    }

    private static void printPst(double[][] pst) {
        for (int piece = 0; piece < NR_PIECE_TYPES; ++piece) {
            System.out.println(piece == 0 ? "Pawn:" : "King:");
            for (int y = 0; y < N; ++y) {
                for (int x = 0; x < N; ++x)
                    System.out.printf("%6.2f ", pst[piece][x+y*N]);
                System.out.println();
            }
            System.out.println();
        }
    }

    /** @return {@code n} random values that are either -1 or 1. */
    static double[] bernoulliRandom(Random rnd, int n) {
        double[] v = new double[n];

        for (int i = 0; i < n; ++i)
            v[i] = 2 * Math.round(rnd.nextDouble()) - 1;

        return v;
    }

    /**
     * @param parameters For each of the two phases (middle game and end game) and two pieces (pawn and king), there
     * should be 15 horizontally symmetrical scores for each self-symmetrical card, and 25 scores for each symmetrical
     * card pair. For the standard set of 16 cards, this means 2 * 2 * (15 * 8 + 25 * 4) = 880 parameters.
     * @return The input converted to a NR_CARDS x 2 x 2 x 25 parameter PST.
     */
    static double[][][][] getCardPhasePiecePst(double[] parameters) {
        double[][][][] pst = new double[Card.NR_CARDS][NR_PHASES][NR_PIECE_TYPES][NN];

        int k = 0;

        for (Card card : Card.CARDS) {
            for (int phase = 0; phase < NR_PHASES; ++phase) {
                for (int piece = 0; piece < NR_PIECE_TYPES; ++piece) {
                    for (int y = 0; y < N; ++y) {
                        if (card.isSelfSymmetrical()) {
                            for (int x = 0; x < 3; ++x) {
                                pst[card.id][phase][piece][x + y*N] = pst[card.id][phase][piece][(N-1-x) + y*N] = parameters[k++];
                            }
                        } else if (card.isFirstMirroredCard()) {
                            for (int x = 0; x < N; ++x) {
                                pst[card.id][phase][piece][x + y*N] = pst[card.getMirroredCard().id][phase][piece][(N-1-x) + y*N] = parameters[k++];
                            }
                        }
                    }
                }
            }
        }

        assert k == parameters.length : "Unexpected length; expected " + k + " got " + parameters.length;

        return pst;
    }
}
