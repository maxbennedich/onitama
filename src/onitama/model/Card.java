package onitama.model;

import onitama.ai.Searcher;

public class Card {
    private static int cardId = 0;

    public static int NR_CARDS = 16;

    public static final Card[] CARDS = new Card[NR_CARDS];

    public static Card Tiger = new Card("Tiger", new int[] {0,-2, 0,1});
    public static Card Crab = new Card("Crab", new int[] {0,-1, -2,0, 2,0});
    public static Card Monkey = new Card("Monkey", new int[] {-1,-1, -1,1, 1,-1, 1,1});
    public static Card Crane = new Card("Crane", new int[] {0,-1, -1,1, 1,1});
    public static Card Dragon = new Card("Dragon", new int[] {-2,-1, 2,-1, -1,1, 1,1});
    public static Card Elephant = new Card("Elephant", new int[] {-1,-1, 1,-1, -1,0, 1,0});
    public static Card Mantis = new Card("Mantis", new int[] {-1,-1, 1,-1, 0,1});
    public static Card Boar = new Card("Boar", new int[] {0,-1, -1,0, 1,0});
    public static Card Frog = new Card("Frog", new int[] {-1,-1, -2,0, 1,1});
    public static Card Goose = new Card("Goose", new int[] {-1,-1, -1,0, 1,0, 1,1});
    public static Card Horse = new Card("Horse", new int[] {0,-1, -1,0, 0,1});
    public static Card Eel = new Card("Eel", new int[] {-1,-1, 1,0, -1,1});
    public static Card Rabbit = new Card("Rabbit", new int[] {1,-1, 2,0, -1,1});
    public static Card Rooster = new Card("Rooster", new int[] {1,-1, -1,0, 1,0, -1,1});
    public static Card Ox = new Card("Ox", new int[] {0,-1, 1,0, 0,1});
    public static Card Cobra = new Card("Cobra", new int[] {-1,0, 1,1, 1,-1});

    public String name;
    public int[] moves;
    public int id;

    public final int[][] moveBitmask = new int[2][Searcher.NN];

    private Card(String name, int[] moves) {
        this.name = name;
        this.moves = moves;
        this.id = cardId++;

        CARDS[id] = this;

        /** For each combination of player, and board square, create a bitmask of valid moves. */
        for (int player = 0; player < 2; ++player) {
            for (int p = 0; p < Searcher.NN; ++p) {
                int bitmask = 0;

                int px = p % Searcher.N, py = p / Searcher.N;
                for (int move = 0; move < moves.length; move += 2) {
                    int mx = moves[move], my = moves[move+1];
                    if (player == 1) { mx *= -1; my *= -1; }
                    int nx = px + mx, ny = py + my;
                    if (nx >= 0 && nx < Searcher.N && ny >= 0 && ny < Searcher.N)
                        bitmask |= 1 << nx + ny * Searcher.N;
                }

                moveBitmask[player][p] = bitmask;
            }
        }
    }

    /** @return The card with the given name (case insensitive), or null if none is found. Warning: inefficient since it loops over all cards. */
    public static Card getByName(String name) {
        for (Card card : CARDS)
            if (card.name.equalsIgnoreCase(name))
                return card;
        return null;
    }
}
