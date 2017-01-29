package onitama.model;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

public class Card {
    private static int cardId = 0;

    public static final int NR_CARDS = 16;

    public static final Card[] CARDS = new Card[NR_CARDS];

    public static final Card Tiger = new Card("Tiger", new int[] {0,-2, 0,1});
    public static final Card Crab = new Card("Crab", new int[] {0,-1, -2,0, 2,0});
    public static final Card Monkey = new Card("Monkey", new int[] {-1,-1, -1,1, 1,-1, 1,1});
    public static final Card Crane = new Card("Crane", new int[] {0,-1, -1,1, 1,1});
    public static final Card Dragon = new Card("Dragon", new int[] {-2,-1, 2,-1, -1,1, 1,1});
    public static final Card Elephant = new Card("Elephant", new int[] {-1,-1, 1,-1, -1,0, 1,0});
    public static final Card Mantis = new Card("Mantis", new int[] {-1,-1, 1,-1, 0,1});
    public static final Card Boar = new Card("Boar", new int[] {0,-1, -1,0, 1,0});
    public static final Card Frog = new Card("Frog", new int[] {-1,-1, -2,0, 1,1});
    public static final Card Goose = new Card("Goose", new int[] {-1,-1, -1,0, 1,0, 1,1});
    public static final Card Horse = new Card("Horse", new int[] {0,-1, -1,0, 0,1});
    public static final Card Eel = new Card("Eel", new int[] {-1,-1, 1,0, -1,1});
    public static final Card Rabbit = new Card("Rabbit", new int[] {1,-1, 2,0, -1,1});
    public static final Card Rooster = new Card("Rooster", new int[] {1,-1, -1,0, 1,0, -1,1});
    public static final Card Ox = new Card("Ox", new int[] {0,-1, 1,0, 0,1});
    public static final Card Cobra = new Card("Cobra", new int[] {-1,0, 1,1, 1,-1});

    public final String name;
    public final int[] moves;
    public final int id;

    public final int[][] moveBitmask = new int[2][NN];

    private Card(String name, int[] moves) {
        this.name = name;
        this.moves = moves;
        this.id = cardId++;

        CARDS[id] = this;

        /** For each combination of player, and board square, create a bitmask of valid moves. */
        for (int player = 0; player < 2; ++player) {
            for (int p = 0; p < NN; ++p) {
                int bitmask = 0;

                int px = p % N, py = p / N;
                for (int move = 0; move < moves.length; move += 2) {
                    int mx = moves[move], my = moves[move+1];
                    if (player == 1) { mx *= -1; my *= -1; }
                    int nx = px + mx, ny = py + my;
                    if (nx >= 0 && nx < N && ny >= 0 && ny < N)
                        bitmask |= 1 << nx + ny * N;
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
