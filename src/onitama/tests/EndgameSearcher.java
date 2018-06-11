package onitama.tests;

import static onitama.model.GameDefinition.NN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import onitama.ai.Searcher;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;

/**
 * For testing wins only, use an alpha/beta window of 1!
 */
public class EndgameSearcher {
    static final int THREADS = 2;

    static final int TT_BITS = 16; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 60;
    static final int MAX_TIME_MS = 100;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String BOARD =
            "....." +
            "....." +
            "B...." +
            "...W." +
            ".....";

    public static void main(String ... args) throws Exception {
        long totalTime = System.currentTimeMillis();

        new EndgameSearcher().searchAllBoards();
//        new EndgameSearcher().searchAllCards();

        totalTime = System.currentTimeMillis() - totalTime;
        System.out.printf("Total time: %d ms%n", totalTime);
    }

    private void searchAllBoards() throws Exception {
        CardState cards = new CardState(new Card[][] {{Card.Dragon, Card.Crab}, {Card.Monkey, Card.Eel}}, Card.Frog);

        AtomicInteger drawCount = new AtomicInteger(0);
        AtomicInteger win0Count = new AtomicInteger(0);
        AtomicInteger win1Count = new AtomicInteger(0);

        List<String> allBoards = new ArrayList<>();
        char[] board = new char[NN];
        Arrays.fill(board, '.');

        // king + king (600 combos)
        for (int wk = 0; wk < NN; ++wk) {
            for (int bk = 0; bk < NN; ++bk) {
                if (bk == wk) continue;
                board[wk] = 'W';
                board[bk] = 'B';

                allBoards.add(new String(board));

                board[bk] = '.';
                board[wk] = '.';
            }
        }

        // king + king + pawn (13800 + 13800 combos)
        for (int wk = 0; wk < NN; ++wk) {
            for (int bk = 0; bk < NN; ++bk) {
                if (bk == wk) continue;
                for (int wp = 0; wp < NN; ++wp) {
                    if (wp == wk || wp == bk) continue;
                    board[wk] = 'W';
                    board[bk] = 'B';

                    board[wp] = 'w';
                    allBoards.add(new String(board));
                    board[wp] = 'b';
                    allBoards.add(new String(board));
                    board[wp] = '.';

                    board[bk] = '.';
                    board[wk] = '.';
                }
            }
        }

        AtomicInteger boardIdx = new AtomicInteger(0);

        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; ++t) {
            threads[t] = new Thread(() -> {
                while (true) {
                    int idx = boardIdx.getAndIncrement();
                    if (idx >= allBoards.size()) break;
                    String boardStr = allBoards.get(idx);

                    Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS, MAX_TIME_MS, true, Utils.NO_LOGGER, false);
                    searcher.setState(PLAYER_0, boardStr, cards);

                    long time = System.currentTimeMillis();

                    int score = searcher.start();
                    String move = searcher.getPrincipalVariationMoveString(true, score);

                    time = System.currentTimeMillis() - time;

                    if (score == Searcher.WIN_SCORE)
                        win0Count.incrementAndGet();
                    else if (score == -Searcher.WIN_SCORE)
                        win1Count.incrementAndGet();
                    else
                        drawCount.incrementAndGet();
        //            if (Math.abs(score) == Searcher.WIN_SCORE && searcher.currentDepthSearched >= 20)
                        System.out.printf("%s: %d ms %s%n", boardStr, time, move);
        //            int wins = winCount.incrementAndGet();
                }
            });
            threads[t].start();
        }

        for (int t = 0; t < THREADS; ++t)
            threads[t].join();

        System.out.println();
        System.out.printf("Boards=%d, Draws=%d, P1 wins=%d, P2 wins=%d%n", drawCount.get()+win0Count.get()+win1Count.get(), drawCount.get(), win0Count.get(), win1Count.get());
    }

    @SuppressWarnings("unused")
    private void searchAllCards() throws Exception {
        int cardsToTest = Card.NR_CARDS;

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

        AtomicInteger comboIdx = new AtomicInteger(0);
        AtomicInteger drawCount = new AtomicInteger(0);
        AtomicInteger win0Count = new AtomicInteger(0);
        AtomicInteger win1Count = new AtomicInteger(0);

        Collections.shuffle(combos, new Random(0));

        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; ++t) {
            threads[t] = new Thread(() -> {
                while (true) {
                    int idx = comboIdx.getAndIncrement();
                    if (idx >= 1+0*combos.size()) break;
                    List<Integer> combo = combos.get(idx);
                    int c0 = combo.get(0), c1 = combo.get(1), c2 = combo.get(2), c3 = combo.get(3), c4 = combo.get(4);

                    Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS, Integer.MAX_VALUE, true, Utils.NO_LOGGER, false);
                    searcher.setState(PLAYER_0, BOARD, new CardState(new Card[][] {{Card.CARDS[c0], Card.CARDS[c1]}, {Card.CARDS[c2], Card.CARDS[c3]}}, Card.CARDS[c4]));

                    long time = System.currentTimeMillis();

                    int score = searcher.start();
                    String move = searcher.getPrincipalVariationMoveString(true, score);

                    time = System.currentTimeMillis() - time;

                    if (score == Searcher.WIN_SCORE)
                        win0Count.incrementAndGet();
                    else if (score == -Searcher.WIN_SCORE)
                        win1Count.incrementAndGet();
                    else
                        drawCount.incrementAndGet();
//                    if (Math.abs(score) == Searcher.WIN_SCORE && searcher.currentDepthSearched >= 20)
                        System.out.printf("%d. %d ms, %s/%s, %s/%s, %s (%.0f%%)   %s%n", idx, time, Card.CARDS[c0].name, Card.CARDS[c1].name, Card.CARDS[c2].name, Card.CARDS[c3].name, Card.CARDS[c4].name, idx*100.0/combos.size(), move);
//                    int wins = winCount.incrementAndGet();
                }
            });
            threads[t].start();
        }

        for (int t = 0; t < THREADS; ++t)
            threads[t].join();

        System.out.println();
        System.out.printf("d=%d 0=%d 1=%d%n", drawCount.get(), win0Count.get(), win1Count.get());
    }
}
