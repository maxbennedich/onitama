package onitama.model;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

public enum Card {
    Tiger(new int[] {0,-2, 0,1}),
    Crab(new int[] {0,-1, -2,0, 2,0}),
    Monkey(new int[] {-1,-1, -1,1, 1,-1, 1,1}),
    Crane(new int[] {0,-1, -1,1, 1,1}),
    Dragon(new int[] {-2,-1, 2,-1, -1,1, 1,1}),
    Elephant(new int[] {-1,-1, 1,-1, -1,0, 1,0}),
    Mantis(new int[] {-1,-1, 1,-1, 0,1}),
    Boar(new int[] {0,-1, -1,0, 1,0}),
    Frog(new int[] {-1,-1, -2,0, 1,1}),
    Goose(new int[] {-1,-1, -1,0, 1,0, 1,1}),
    Horse(new int[] {0,-1, -1,0, 0,1}),
    Eel(new int[] {-1,-1, 1,0, -1,1}),
    Rabbit(new int[] {1,-1, 2,0, -1,1}),
    Rooster(new int[] {1,-1, -1,0, 1,0, -1,1}),
    Ox(new int[] {0,-1, 1,0, 0,1}),
    Cobra(new int[] {-1,0, 1,1, 1,-1}),
    ;

    public static final int NR_CARDS;

    public static final Card[] CARDS;

    public static final int MAX_CARD_NAME_LENGTH;

    static {
        NR_CARDS = Card.values().length;
        CARDS = Card.values();

        int maxLength = -1;
        for (Card card : CARDS)
            maxLength = Math.max(maxLength, card.name.length());
        MAX_CARD_NAME_LENGTH = maxLength;
    }

    public final String name;
    public final int[] moves;
    public final int id;

    public final int[][] moveBitmask = new int[2][NN];

    private Card(int[] moves) {
        this.name = name();
        this.moves = moves;
        this.id = ordinal();

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

    public String getFixedWidthName() {
        return String.format("%-" + MAX_CARD_NAME_LENGTH + "s", name);
    }

    /** @return The card with the given name (case insensitive), or null if none is found. Warning: inefficient since it loops over all cards. */
    public static Card getByName(String name) {
        for (Card card : CARDS)
            if (card.name.equalsIgnoreCase(name))
                return card;
        return null;
    }
}
