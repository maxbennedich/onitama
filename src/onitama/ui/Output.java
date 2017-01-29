package onitama.ui;

import static onitama.model.GameDefinition.N;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;
import onitama.model.GameState;

public class Output {
    private static final char[] MARKERS = new char[] {' ', 'r', 'b', 'R', 'B'};

    public static enum OutputLevel {
        NONE, VERBOSE
    }

    public static OutputLevel outputLevel = OutputLevel.VERBOSE;

    static void println() {
        print("\n");
    }

    static void println(String string) {
        print(string + "\n");
    }

    static void printf(String format, Object... args) {
        print(String.format(format, args));
    }

    static void print(String string) {
        if (outputLevel == OutputLevel.VERBOSE)
            System.out.print(string);
    }

    public static void printBoard(int[] bitboardPlayer, int[] bitboardKing) {
        println("  +---+---+---+---+---+");
        for (int y = 0, bit = 1; y < N; ++y) {
            printf("%d |", N-y);
            for (int x = 0; x < N; ++x, bit *= 2) {
                int c = 0;
                if ((bitboardKing[0] & bit) != 0) c = 3;
                else if ((bitboardKing[1] & bit) != 0) c = 4;
                else if ((bitboardPlayer[0] & bit) != 0) c = 1;
                else if ((bitboardPlayer[1] & bit) != 0) c = 2;
                printf(" %c |", MARKERS[c]);
            }
            println("\n  +---+---+---+---+---+");
        }

        print(" ");
        for (int x = 0; x < N; ++x)
            printf("   %c", 'a'+x);

        println();
    }

    static void printWelcomeMessage() {
        print("Available cards: ");
        for (Card card : Card.CARDS)
            printf("%d. %s  ", card.id + 1, card.name);
        println();

        println("Enter moves like 'Dragon a1b2'\n");
    }

    static void printCards(CardState cardState) {
        for (int player = 0; player < 2; ++player) {
            printf("Player %d cards:", player + 1); // players[player].getName()
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                printf(" %s", cardState.playerCards[player][c].name);
            println();
        }
        printf("%s card: %s%n", Onitama.EXTRA_CARD_NAME, cardState.nextCard.name);
    }

    static void printGameState(GameState gameState) {
        Searcher searcher = new Searcher(1, 0, 0, false);
        searcher.setState(0, gameState.board, gameState.cardState);
        searcher.printBoard();
        println();
        Output.printCards(gameState.cardState);
        println();
    }
}
