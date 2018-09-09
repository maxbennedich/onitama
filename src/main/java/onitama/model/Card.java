package onitama.model;

import static onitama.model.Deck.Original;
import static onitama.model.Deck.Promo;
import static onitama.model.Deck.SenseisPath;
import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

public enum Card {
    Tiger(Original, new int[] {0,-2, 0,1}),
    Crab(Original, new int[] {0,-1, -2,0, 2,0}),
    Monkey(Original, new int[] {-1,-1, -1,1, 1,-1, 1,1}),
    Crane(Original, new int[] {0,-1, -1,1, 1,1}),
    Dragon(Original, new int[] {-2,-1, 2,-1, -1,1, 1,1}),
    Elephant(Original, new int[] {-1,-1, 1,-1, -1,0, 1,0}),
    Mantis(Original, new int[] {-1,-1, 1,-1, 0,1}),
    Boar(Original, new int[] {0,-1, -1,0, 1,0}),
    Frog(Original, new int[] {-1,-1, -2,0, 1,1}),
    Goose(Original, new int[] {-1,-1, -1,0, 1,0, 1,1}),
    Horse(Original, new int[] {0,-1, -1,0, 0,1}),
    Eel(Original, new int[] {-1,-1, 1,0, -1,1}),
    Rabbit(Original, new int[] {1,-1, 2,0, -1,1}, Frog),
    Rooster(Original, new int[] {1,-1, -1,0, 1,0, -1,1}, Goose),
    Ox(Original, new int[] {0,-1, 1,0, 0,1}, Horse),
    Cobra(Original, new int[] {-1,0, 1,1, 1,-1}, Eel),

    Giraffe(SenseisPath, new int[] {-2,-1, 2,-1, 0,1}),
    Kirin(SenseisPath, new int[] {-1,-2, 1,-2, 0,2}),
    Phoenix(SenseisPath, new int[] {-1,-1, 1,-1, -2,0, 2,0}),
    Turtle(SenseisPath, new int[] {-2,0, 2,0, -1,-1, 1,-1}),
    Dog(SenseisPath, new int[] {-1,-1, -1,0, -1,1}),
    Bear(SenseisPath, new int[] {0,-1, -1,-1, 1,1}),
    Viper(SenseisPath, new int[] {0,-1, -2,0, 1,1}),
    Rat(SenseisPath, new int[] {0,-1, -1,0, 1,1}),
    Iguana(SenseisPath, new int[] {0,-1, -2,-1, 1,1}),
    Otter(SenseisPath, new int[] {-1,-1, 2,0, 1,1}),
    Fox(SenseisPath, new int[] {1,-1, 1,0, 1,1}, Dog),
    Panda(SenseisPath, new int[] {0,-1, 1,-1, -1,1}, Bear),
    Sea_Snake(SenseisPath, new int[] {0,-1, 2,0, -1,1}, Viper),
    Mouse(SenseisPath, new int[] {0,-1, 1,0, -1,1}, Rat),
    Tanuki(SenseisPath, new int[] {0,-1, 2,-1, -1,1}, Iguana),
    Sable(SenseisPath, new int[] {1,-1, -2,0, -1,1}, Otter),

    Sheep(Promo, new int[] {-1,-1, 1,0, 0,1}),
    Goat(Promo, new int[] {1,-1, -1,0, 0,1}, Sheep),
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
    public final Deck deck;
    public final int[] moves;
    public final int id;

    public final int[][] moveBitmask = new int[2][NN];

    private Card mirror = null;

    private Card(Deck deck, int[] moves, Card horizontallyMirroredCard) {
        this(deck, moves);

        mirror = horizontallyMirroredCard;
        mirror.mirror = this;
    }

    private Card(Deck deck, int[] moves) {
        this.name = name().replaceAll("_", " ");
        this.deck = deck;
        this.moves = moves;
        this.id = ordinal();

        deck.cards.add(this);

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

    public boolean isSelfSymmetrical() {
        return mirror == null;
    }

    public boolean isFirstMirroredCard() {
        return mirror != null && mirror.id > id;
    }

    public Card getMirroredCard() {
        return mirror;
    }

    public boolean isSameOrMirrored(Card card) {
        return this == card || mirror == card;
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
