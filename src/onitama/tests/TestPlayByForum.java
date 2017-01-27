package onitama.tests;

import onitama.ai.Searcher;

public class TestPlayByForum {
    static final int TT_BITS = 28; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 14;

    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    static String PBF0 =
            "ooQoo" +
            "....." +
            "....." +
            "....." +
            "xx#xx";

    static String PBF1 =
            "ooQoo" +
            "....." +
            "....." +
            "...x." +
            "xx#.x";

    static String PBF2 =
            ".oQoo" +
            ".o..." +
            "....." +
            "...x." +
            "xx#.x";

    static String PBF3 =
            ".oQoo" +
            ".o..." +
            "....." +
            ".x.x." +
            ".x#.x";

    static String PBF4 =
            "..Qoo" +
            ".oo.." +
            "....." +
            ".x.x." +
            ".x#.x";

    static String PBF5 =
            "..Qoo" +
            ".oo.." +
            "....." +
            "xx.x." +
            "..#.x";

    static String PBF6 =
            "..Qo." +
            ".ooo." +
            "....." +
            "xx.x." +
            "..#.x";

    static String PBF7 =
            "..Qo." +
            "xooo." +
            "....." +
            ".x.x." +
            "..#.x";

    static String PBF8 =
            "..Qo." +
            "xo.o." +
            "..o.." +
            ".x.x." +
            "..#.x";

    static String PBF9 =
            "..Qo." +
            "xo.o." +
            "..x.." +
            ".x..." +
            "..#.x";

    public static void main(String ... args) throws Exception {
        Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS, Integer.MAX_VALUE, true);

        // new cards:
//        public static Card Tiger = new Card("Tiger", new int[] {0,-2});
//        public static Card Dragon = new Card("Dragon", new int[] {-1,0, 1,0, -1,-1, 1,-1});
//        public static Card Boar = new Card("Boar", new int[] {0,-1, 0,1});
//        public static Card Rabbit = new Card("Rabbit", new int[] {0,1, -1,-1, 1,-1});
//        public static Card Sheep = new Card("Sheep", new int[] {-1,1, 1,1, -1,-1, 1,-1});

        // From https://boardgamegeek.com/thread/1454897/play-forum-vs-ai :
        // Best moves:
        // 0: 0,4 -> 1,3 (Dragon) (score 0 after 12 moves)
        // 1: 0,0 -> 1,1 (Sheep) (score 1 after 13 moves)
        // 2: 0,4 -> 1,3 (Dragon) (score -1 after 12 moves)
        // 3: 1,0 -> 2,1 (Sheep) (score 1 after 13 moves)
        // 4: 3,3 -> 3,4 (Rabbit) (score -1 after 12 moves)
        // 5: 3,0 -> 3,1 (Boar) (score 9 after 13 moves)
        // 6: 0,3 -> 1,4 (Sheep) (score -2 after 12 moves)
        // 7: 2,1 -> 2,2 (Boar) (win after 15 moves)
//        searcher.setState(PLAYER_0, PBF0, new CardState(new Card[][] {{Card.Boar, Card.Dragon}, {Card.Rabbit, Card.Sheep}}, Card.Tiger));
//        searcher.setState(PLAYER_1, PBF1, new Card[][] {{Card.Tiger, Card.Dragon}, {Card.Rabbit, Card.Sheep}}, Card.Boar);
//        searcher.setState(PLAYER_0, PBF2, new Card[][] {{Card.Tiger, Card.Dragon}, {Card.Boar, Card.Sheep}}, Card.Rabbit);
//        searcher.setState(PLAYER_1, PBF3, new Card[][] {{Card.Tiger, Card.Rabbit}, {Card.Boar, Card.Sheep}}, Card.Dragon);
//        searcher.setState(PLAYER_0, PBF4, new Card[][] {{Card.Tiger, Card.Rabbit}, {Card.Boar, Card.Dragon}}, Card.Sheep);
//        searcher.setState(PLAYER_1, PBF5, new Card[][] {{Card.Tiger, Card.Sheep}, {Card.Boar, Card.Dragon}}, Card.Rabbit);
//        searcher.setState(PLAYER_0, PBF6, new Card[][] {{Card.Tiger, Card.Sheep}, {Card.Boar, Card.Rabbit}}, Card.Dragon);
//        searcher.setState(PLAYER_1, PBF7, new Card[][] {{Card.Dragon, Card.Sheep}, {Card.Boar, Card.Rabbit}}, Card.Tiger);
//        searcher.setState(PLAYER_0, PBF8, new Card[][] {{Card.Dragon, Card.Sheep}, {Card.Tiger, Card.Rabbit}}, Card.Boar);
//        searcher.setState(PLAYER_1, PBF9, new Card[][] {{Card.Dragon, Card.Boar}, {Card.Tiger, Card.Rabbit}}, Card.Sheep);

        System.out.printf("Transposition table size: %d entries (%.0f MB)%n", searcher.tt.sizeEntries(), searcher.tt.sizeBytes() / 1024.0 / 1024.0);

        searcher.printBoard();

        long time = System.currentTimeMillis();

        searcher.start();

        time = System.currentTimeMillis() - time;

        System.out.println();
        searcher.stats.print();

        System.out.printf("%nElapsed time: %d ms%n", time);
    }
}
