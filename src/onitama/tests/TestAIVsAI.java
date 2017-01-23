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

import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Pair;
import onitama.model.SearchParameters;
import onitama.ui.AIPlayer;
import onitama.ui.GameSimulator;
import onitama.ui.Output;
import onitama.ui.Output.OutputLevel;
import onitama.ui.Player;

public class TestAIVsAI {
    static final int THREADS = 2;

    static String EMPTY_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    static int depth[] = new int[2];
    static Player[] players = new Player[2];

    public static void main(String ... args) throws Exception {
        Output.outputLevel = OutputLevel.NONE;

//        for (int d = 1; d <= 15; ++d) {
//            for (int n = 1; n <= d; ++n) {
//                for (int p = 0; p < 2; ++p) {
//                    if (p == 1 && n == d) continue;
//                    depth[p] = d;
//                    depth[1-p] = n;
//                    for (int i = 0; i < 2; ++i)
//                        players[i] = new AIPlayer(i, new SearchParameters(22, depth[i], Integer.MAX_VALUE));
//                    runTest(200);
//                }
//            }
//        }

        for (int i = 0; i < 2; ++i)
            players[i] = new AIPlayer(i, new SearchParameters(16, 50, 30+i), false);

        runTest(100);

        for (int i = 0; i < 2; ++i)
            players[i] = new AIPlayer(i, new SearchParameters(16, 50, 31-i), false);

        runTest(100);
    }

    private static Pair<Integer, Integer> playCards(CardState cards) {
        return new GameSimulator(players, new GameState(EMPTY_BOARD, cards)).play();
    }

    private static void runTest(final int nrOfHandsToTest) throws Exception {
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
            // add the same cards with the two player cards swapped -- don't do this since the extra card creates a bias anyway
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

                    Pair<Integer, Integer> gameResult = playCards(new CardState(new Card[][] {{Card.CARDS[c0], Card.CARDS[c1]}, {Card.CARDS[c2], Card.CARDS[c3]}}, Card.CARDS[c4]));

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

//        System.out.println();
        int c0 = winCount[0].get(), c1 = winCount[1].get();
//        System.out.printf("Total combinations won for p1/p2: %d / %d (%.0f%% / %.0f%%)%n", c0, c1, 100.0 * c0 / (c0 + c1), 100.0 * c1 / (c0 + c1));
//        System.out.printf("Stuck games: %d%n", drawCount.get());
//
        List<Integer> plies = getPliesList(gamesByPlies);
//        System.out.printf("Med/max plies: %d / %d%n", getMedian(plies), Collections.max(plies));

        System.out.printf("%d/%d: %.1f%% (%d / %d) -- %.0f s%n", depth[0], depth[1], 100.0 * c0 / (c0 + c1), getMedian(plies), Collections.max(plies), totalTime / 1000.0);

//        System.out.println();
//        gamesByPlies.keySet().stream().sorted().forEach(plies -> {
//            System.out.printf("%d plies: %d games%n", plies, gamesByPlies.get(plies));
//        });
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
