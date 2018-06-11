package onitama.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import onitama.ai.Searcher;
import onitama.common.Utils;
import onitama.model.Card;
import onitama.model.CardState;

/*
Cards: 1567 ms
Depth: 2227 ms
TT size: 520 ms
Board/Player: 620 ms
Total time: 4935 ms

Iterative deepening:

Cards: 2749 ms
Depth: 2597 ms
TT size: 545 ms
Board/Player: 716 ms
Total time: 6613 ms

Best moves:

Cards: 1896 ms
Depth: 525 ms
TT size: 212 ms
Board/Player: 572 ms
Total time: 3207 ms

Better static evaluation:

Cards: 2106 ms
Depth: 864 ms
TT size: 391 ms
Board/Player: 601 ms
Total time: 3964 ms

 */
public class TestSearchParameters {
    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String BOARD_WIN_AT_13 =
            "b.Bbb" +
            "....." +
            ".b..." +
            ".wwW." +
            "w...w";

    private static String TEST_FILE_NAME = "testdata.txt";

    public static void main(String ... args) throws Exception {
        new TestSearchParameters().runTests(true);
    }

    HashMap<String, Integer> scoreByParameters = new HashMap<>();

    private long reportTime(long watch, String test) {
        long t = System.currentTimeMillis();
        System.out.printf("%s: %d ms%n", test, t-watch);
        return System.currentTimeMillis();
    }

    private void loadTestData() {
        File file = new File(TEST_FILE_NAME);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] parts = line.split("\\|");
                scoreByParameters.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        System.out.printf("Loaded %d tests from %s%n", scoreByParameters.size(), file.getAbsolutePath());
    }

    private void storeTestData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEST_FILE_NAME))) {
            for (Entry<String, Integer> entry : scoreByParameters.entrySet())
                bw.write(String.format("%s|%d%n", entry.getKey(), entry.getValue()));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void runTests(boolean verify) {
        if (verify)
            loadTestData();

        long watch = System.currentTimeMillis(), w0 = watch;

        testCards(verify);
        watch = reportTime(watch, "Cards");

        testDepth(verify);
        watch = reportTime(watch, "Depth");

        testTTSize(verify);
        watch = reportTime(watch, "TT size");

        testStartBoardAndPlayer(verify);
        watch = reportTime(watch, "Board/Player");

        reportTime(w0, "Total time");

        if (!verify)
            storeTestData();
    }

    private void verify(String parameters, int score, boolean verify) {
        if (verify) {
            Integer storedScore = scoreByParameters.get(parameters);
            if (storedScore == null)
                System.out.printf("ERROR: No score stored for %s%n", parameters);
            else if (storedScore != score)
                System.out.printf("ERROR: Score mismatch for %s: expected %d, was %d%n", parameters, storedScore, score);
        } else {
            scoreByParameters.put(parameters, score);
        }
    }

    private void testCards(boolean verify) {
        int cardsToTest = 16;

        for (int c0 = 0; c0 < cardsToTest-4; ++c0) {
            for (int c1 = c0+1; c1 < cardsToTest-3; ++c1) {
                for (int c2 = c1+1; c2 < cardsToTest-2; ++c2) {
                    for (int c3 = c2+1; c3 < cardsToTest-1; ++c3) {
                        for (int c4 = c3+1; c4 < cardsToTest; ++c4) {
                            Searcher searcher = new Searcher(4, 16, Integer.MAX_VALUE, true, Utils.NO_LOGGER, false);

                            searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.CARDS[c0], Card.CARDS[c1]}, {Card.CARDS[c2], Card.CARDS[c3]}}, Card.CARDS[c4]));

                            int score = searcher.start();

                            String parameters = String.format("card_%s_%s_%s_%s_%s", Card.CARDS[c0].name, Card.CARDS[c1].name, Card.CARDS[c2].name, Card.CARDS[c3].name, Card.CARDS[c4].name);
                            verify(parameters, score, verify);
                        }
                    }
                }
            }
        }
    }

    private void testDepth(boolean verify) {
        for (int depth = 1; depth <= 9; ++depth) {
            Searcher searcher = new Searcher(depth, 20, Integer.MAX_VALUE, true, Utils.NO_LOGGER, false);

            searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon));

            int score = searcher.start();

            String parameters = String.format("depth_%d", depth);
            verify(parameters, score, verify);
        }
    }

    private void testTTSize(boolean verify) {
        for (int ttBits = 1; ttBits < 26; ++ttBits) {
            Searcher searcher = new Searcher(6, ttBits, Integer.MAX_VALUE, true, Utils.NO_LOGGER, false);

            searcher.setState(PLAYER_0, BOARD_WIN_AT_13, new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon));

            int score = searcher.start();

            String parameters = String.format("hash_size_%d", ttBits);
            verify(parameters, score, verify);
        }
    }

    private String getRandomBoard(Random rnd) {
        char[] board = ".........................".toCharArray();
        char[] pieces = "BWbwbwbwbw".toCharArray();

        int nrPieces = rnd.nextBoolean() ? pieces.length : 2 + rnd.nextInt(pieces.length-2);

        for (int p = 0; p < nrPieces; ++p) {
            int idx = 0;
            do idx = rnd.nextInt(board.length); while (board[idx] != '.');
            board[idx] = pieces[p];
        }

        return new String(board);
    }

    private String changeBoardPlayer(String board) {
        int N = board.length();
        char[] flipped = new char[N];
        for (int x = 0; x < N; ++x) {
            char c = board.charAt(x), f = c;
            if (c == 'b') f = 'w';
            else if (c == 'B') f = 'W';
            else if (c == 'w') f = 'b';
            else if (c == 'W') f = 'B';
            flipped[N-1-x] = f;
        }
        return new String(flipped);
    }

    private void testStartBoardAndPlayer(boolean verify) {
        Random rnd = new Random(0);

        for (int b = 0; b < 500; ++b) {
            String board = getRandomBoard(rnd);
            int[] scores = new int[2];

            for (int player = 0; player <= 1; ++player) {
                Searcher searcher = new Searcher(4, 18, Integer.MAX_VALUE, true, Utils.NO_LOGGER, false);

                if (player == 1)
                    board = changeBoardPlayer(board);

                searcher.setState(player, board, player == 0 ?
                        new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon) :
                        new CardState(new Card[][] {{Card.Tiger, Card.Crab}, {Card.Monkey, Card.Crane}}, Card.Dragon));

                int score = searcher.start();
                scores[player] = score;

                String parameters = String.format("player_%d_board_%s", player, board);
                verify(parameters, score, verify);
            }

            if (scores[0] != scores[1])
                System.out.printf("ERROR: score mismatch, %d vs %d for board %s%n", scores[0], scores[1], board);
        }
    }
}
