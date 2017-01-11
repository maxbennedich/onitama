package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameState;
import onitama.model.Pair;
import onitama.model.SearchParameters;
import onitama.ui.AIPlayer;
import onitama.ui.GameSimulator;
import onitama.ui.Player;

public class TestAIVsAI {
    static final int THREADS = 4;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String EMPTY_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    static Player[] players = new Player[] {
            new AIPlayer(0, new SearchParameters(26, 15, 5000)),
            new AIPlayer(1, new SearchParameters(26, 11, 1000)) };

    public static void main(String ... args) throws Exception {
        Searcher.LOGGING = false;

        long totalTime = System.currentTimeMillis();

        playCards(new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Eel, Card.Crab}}, Card.Dragon));

        totalTime = System.currentTimeMillis() - totalTime;
        System.out.printf("Total time: %d ms%n", totalTime);
    }

    private static Pair<Integer, Integer> playCards(CardState cards) {
        return new GameSimulator(players, new GameState(EMPTY_BOARD, cards), true).play();
    }

/*    private static void testAllCards() throws Exception {
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

        Collections.shuffle(combos, new Random(0));

        AtomicInteger comboIdx = new AtomicInteger(0);
        AtomicInteger winCount = new AtomicInteger(0);

        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; ++t) {
            threads[t] = new Thread(() -> {
                while (true) {
                    int idx = comboIdx.getAndIncrement();
                    if (idx >= combos.size()) break;
                    List<Integer> combo = combos.get(idx);
                    int c0 = combo.get(0), c1 = combo.get(1), c2 = combo.get(2), c3 = combo.get(3), c4 = combo.get(4);

                    Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS);
                    searcher.setState(PLAYER_0, EMPTY_BOARD, new CardState(new Card[][] {{Card.CARDS[c0], Card.CARDS[c1]}, {Card.CARDS[c2], Card.CARDS[c3]}}, Card.CARDS[c4]));

                    long time = System.currentTimeMillis();

                    int score = searcher.start(Integer.MAX_VALUE);

                    time = System.currentTimeMillis() - time;

                    if (score == 100) {
                        int wins = winCount.incrementAndGet();
                        System.out.printf("%d. %d ms, %s/%s, %s/%s, %s (%d/%d - %.0f%%)%n", wins, time, Card.CARDS[c0].name, Card.CARDS[c1].name, Card.CARDS[c2].name, Card.CARDS[c3].name, Card.CARDS[c4].name, idx, combos.size(), idx*100.0/combos.size());
                    }
                }
            });
            threads[t].start();
        }

        for (int t = 0; t < THREADS; ++t)
            threads[t].join();

        System.out.println();
        System.out.printf("Total combinations won: %d%n", winCount.get());
    }*/

}
