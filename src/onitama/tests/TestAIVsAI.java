package onitama.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import onitama.ai.Searcher;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Pair;
import onitama.model.SearchParameters;
import onitama.ui.AIPlayer;
import onitama.ui.Player;
import onitama.ui.console.GameSimulator;
import onitama.ui.console.Output;
import onitama.ui.console.Output.OutputLevel;

public class TestAIVsAI {
    static final int THREADS = 4;

    static String EMPTY_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    public static void main(String ... args) throws Exception {
        Output.outputLevel = OutputLevel.NONE;

        SearchParameters[] aiParams = new SearchParameters[2];
        for (int i = 0; i < 2; ++i)
            aiParams[i] = new SearchParameters(16, Searcher.MAX_DEPTH, 30+i);

        runTest(aiParams, 30);

        for (int i = 0; i < 2; ++i)
            aiParams[i] = new SearchParameters(16, Searcher.MAX_DEPTH, 31-i);

        runTest(aiParams, 30);
    }

    /** Test playing AI against itself but with another search depth. */
    @SuppressWarnings("unused")
    private static void testAIsWithDifferentDepths() throws Exception {
        for (int d = 1; d <= 15; ++d) {
            for (int n = 1; n <= d; ++n) {
                for (int p = 0; p < 2; ++p) {
                    if (p == 1 && n == d) continue;
                    SearchParameters[] aiParams = new SearchParameters[2];
                    for (int i = 0; i < 2; ++i)
                        aiParams[i] = new SearchParameters(22, i == p ? d : n, Integer.MAX_VALUE);
                    runTest(aiParams, 200);
                }
            }
        }
    }

    private static Pair<Integer, Integer> playCards(Player[] players, CardState cards) {
        return new GameSimulator(players, new GameState(EMPTY_BOARD, cards)).play();
    }

    private static void runTest(final SearchParameters[] aiParams, final int nrOfHandsToTest) throws Exception {
        int cardsToTest = 16;

        List<List<Integer>> combos = new ArrayList<>();

        for (int c0 = 0; c0 < cardsToTest-1; ++c0) {
            for (int c1 = c0+1; c1 < cardsToTest; ++c1) {
                for (int c2 = 0; c2 < cardsToTest-1; ++c2) {
                    if (c2 == c0 || c2 == c1) continue;
                    for (int c3 = c2+1; c3 < cardsToTest; ++c3) {
                        if (c3 == c0 || c3 == c1) continue;
                        for (int c4 = 0; c4 < cardsToTest; ++c4) {
                            if (c4 == c0 || c4 == c1 || c4 == c2 || c4 == c3) continue;
                            combos.add(Arrays.asList(c0, c1, c2, c3, c4));
                        }
                    }
                }
            }
        }

        Collections.shuffle(combos, new Random(1));
        final List<List<Integer>> combosToTest = new ArrayList<List<Integer>>();
        for (int h = 0; h < nrOfHandsToTest; ++h) {
            combosToTest.add(combos.get(h));
            // Don't add the same cards with the two player cards swapped -- the extra card creates a bias anyway
//            combosToTest.add(Arrays.asList(combos.get(h).get(2), combos.get(h).get(3), combos.get(h).get(0), combos.get(h).get(1), combos.get(h).get(4)));
        }

        AtomicInteger comboIdx = new AtomicInteger(0);
        AtomicInteger[] winCount = new AtomicInteger[] { new AtomicInteger(0), new AtomicInteger(0)};
        AtomicInteger drawCount = new AtomicInteger(0);
        Map<Integer, Integer> gamesByPlies = new HashMap<>();

        long totalTime = System.currentTimeMillis();

        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; ++t) {
            threads[t] = new Thread(() -> {
                while (true) {
                    int idx = comboIdx.getAndIncrement();
                    if (idx >= combosToTest.size()) break;
                    List<Integer> combo = combosToTest.get(idx);
                    int c0 = combo.get(0), c1 = combo.get(1), c2 = combo.get(2), c3 = combo.get(3), c4 = combo.get(4);

                    long time = System.currentTimeMillis();

                    Player[] players = new Player[2];
                    for (int i = 0; i < 2; ++i)
                        players[i] = new AIPlayer(i, aiParams[i], false, Utils.NO_LOGGER);

                    Pair<Integer, Integer> gameResult = playCards(players, new CardState(new Card[][] {{Card.CARDS[c0], Card.CARDS[c1]}, {Card.CARDS[c2], Card.CARDS[c3]}}, Card.CARDS[c4]));

                    time = System.currentTimeMillis() - time;

                    if (gameResult.p == -1) {
                        drawCount.incrementAndGet();
                    } else {
                        winCount[gameResult.p].incrementAndGet();
                        synchronized (gamesByPlies) {
                            Integer count = gamesByPlies.get(gameResult.q);
                            if (count == null) count = 0;
                            gamesByPlies.put(gameResult.q, ++count);
                        }
                    }
                    int wc1 = winCount[0].get(), wc2 = winCount[1].get();
                    System.out.printf("Hand %d: winner = %d (total %d/%d = %.1f%%), plies = %d, %d ms, %s/%s, %s/%s, %s%n", idx, gameResult.p, wc1, wc2, 100.0*wc1/(wc1+wc2), gameResult.q, time, Card.CARDS[c0].name, Card.CARDS[c1].name, Card.CARDS[c2].name, Card.CARDS[c3].name, Card.CARDS[c4].name);
                }
            });
            threads[t].start();
        }

        for (int t = 0; t < THREADS; ++t)
            threads[t].join();

        totalTime = System.currentTimeMillis() - totalTime;

        int c0 = winCount[0].get(), c1 = winCount[1].get();

        List<Integer> plies = getPliesList(gamesByPlies);

        System.out.printf("%nAI 1: %s%nAI 2: %s%n", aiParams[0], aiParams[1]);
        System.out.printf("%nAI #1 won %.1f%% games (avg plies = %d / max plies = %d) -- %.0f s%n", 100.0 * c0 / (c0 + c1), getMedian(plies), Collections.max(plies), totalTime / 1000.0);

        System.out.println();
        gamesByPlies.keySet().stream().sorted().forEach(p -> System.out.printf("%d plies: %d games%n", p, gamesByPlies.get(p)));
        System.out.printf("Stuck games: %d%n%n", drawCount.get());
    }

    static List<Integer> getPliesList(Map<Integer, Integer> gamesByPlies) {
        List<Integer> plies = new ArrayList<>();
        for (Entry<Integer, Integer> entry : gamesByPlies.entrySet()) {
            for (int n = 0; n < entry.getValue(); ++n)
                plies.add(entry.getKey());
        }
        return plies;
    }

    static int getMedian(List<Integer> list) {
        Collections.sort(list);
        if (list.size() % 2 == 0)
            return (list.get(list.size()/2) + list.get(list.size()/2 - 1))/2;
        return list.get(list.size()/2);
    }
}
