package onitama.ui;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;
import onitama.model.GameState;

public class Output {
    static char[] markers = new char[] {' ', 'r', 'b', 'R', 'B'};

    public static void printBoard(int boardOccupied, long boardPieces) {
        System.out.println("  +---+---+---+---+---+");
        for (int y = 0, bit = 1, piece = 0; y < Searcher.N; ++y) {
            System.out.printf("%d |", Searcher.N-y);
            for (int x = 0; x < Searcher.N; ++x, bit *= 2, piece += 2) {
                int c = (boardOccupied & bit) == 0 ? 0 : 1 + ((int)(boardPieces >> piece) & 3);
                System.out.printf(" %c |", markers[c]);
            }
            System.out.println("\n  +---+---+---+---+---+");
        }

        System.out.print(" ");
        for (int x = 0; x < Searcher.N; ++x)
            System.out.printf("   %c", 'a'+x);

        System.out.println();
    }

    static void printWelcomeMessage() {
        System.out.print("Available cards: ");
        for (Card card : Card.CARDS)
            System.out.printf("%d. %s  ", card.id + 1, card.name);
        System.out.println();

        System.out.println("Enter moves like 'Dragon a1b2'\n");
    }

    public static void printCards(CardState cardState) {
        for (int player = 0; player < 2; ++player) {
            System.out.printf("Player %d cards:", player + 1); // players[player].getName()
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                System.out.printf(" %s", cardState.playerCards[player][c].name);
            System.out.println();
        }
        System.out.printf("%s card: %s%n", Onitama.EXTRA_CARD_NAME, cardState.nextCard.name);
    }

    public static void printGameState(GameState gameState) {
        Searcher searcher = new Searcher(1, 0);
        searcher.setState(0, gameState.board, gameState.cardState);
        searcher.printBoard();
        System.out.println();
        Output.printCards(gameState.cardState);
        System.out.println();
    }

}
