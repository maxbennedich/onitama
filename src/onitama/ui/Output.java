package onitama.ui;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;
import onitama.model.GameState;

public class Output {
    static char[] markers = new char[] {' ', 'r', 'b', 'R', 'B'};

    public enum OutputLevel {
        NONE, VERBOSE
    }

    public static OutputLevel outputLevel = OutputLevel.VERBOSE;

    public static void println() {
        print("\n");
    }

    public static void println(String string) {
        print(string + "\n");
    }

    public static void printf(String format, Object... args) {
        print(String.format(format, args));
    }

    public static void print(String string) {
        if (outputLevel == OutputLevel.VERBOSE)
            System.out.print(string);
    }

    public static void printBoard(int boardOccupied, long boardPieces) {
        println("  +---+---+---+---+---+");
        for (int y = 0, bit = 1, piece = 0; y < Searcher.N; ++y) {
            printf("%d |", Searcher.N-y);
            for (int x = 0; x < Searcher.N; ++x, bit *= 2, piece += 2) {
                int c = (boardOccupied & bit) == 0 ? 0 : 1 + ((int)(boardPieces >> piece) & 3);
                printf(" %c |", markers[c]);
            }
            println("\n  +---+---+---+---+---+");
        }

        print(" ");
        for (int x = 0; x < Searcher.N; ++x)
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

    public static void printCards(CardState cardState) {
        for (int player = 0; player < 2; ++player) {
            printf("Player %d cards:", player + 1); // players[player].getName()
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                printf(" %s", cardState.playerCards[player][c].name);
            println();
        }
        printf("%s card: %s%n", Onitama.EXTRA_CARD_NAME, cardState.nextCard.name);
    }

    public static void printGameState(GameState gameState) {
        Searcher searcher = new Searcher(1, 0);
        searcher.setState(0, gameState.board, gameState.cardState);
        searcher.printBoard();
        println();
        Output.printCards(gameState.cardState);
        println();
    }

}
