package onitama;

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
    int[] moves;
    int id;

    Card(String name, int[] moves) {
        this.name = name;
        this.moves = moves;
        this.id = cardId++;

        CARDS[id] = this;
    }
}
