package onitama.ui;

import static onitama.model.GameDefinition.N;

import java.util.Scanner;

import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;
import onitama.model.Move;
import onitama.model.SearchParameters;

public class Input {
    static Player[] queryPlayers() {
        Player[] players = new Player[2];
        for (int p = 0; p < 2; ++p)
            players[p] = queryPlayer(p);
        return players;
    }

    private static Player queryPlayer(int player) {
        while (true) {
            System.out.printf("Use AI for player %d (y/n): ", player + 1);
            try {
                @SuppressWarnings("resource") // don't close System.in
                String response = new Scanner(System.in).next();
                return response.equals("y") ? new AIPlayer(player, new SearchParameters(26, 50, 10000), true) : new HumanPlayer(player);
            } catch (Exception e) {
                System.out.println("Invalid input, try again");
            }
        }
    }

    static CardState queryStartCards() {
        while (true) {
            System.out.print("Enter start card numbers (");
            for (int player = 0; player < 2; ++player)
                for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                    System.out.printf("P%d, ", player + 1);
            System.out.print(Onitama.EXTRA_CARD_NAME + "): ");

            @SuppressWarnings("resource") // don't close System.in
            String cardsStr = new Scanner(System.in).nextLine();
            String[] cardsStrs = cardsStr.split("\\s+");

            if (cardsStrs.length == GameDefinition.CARDS_PER_PLAYER * 2 + 1) {
                try {
                    Card[][] playerCards = new Card[2][GameDefinition.CARDS_PER_PLAYER];
                    for (int player = 0, i = 0; player < 2; ++player)
                        for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c, ++i)
                            playerCards[player][c] = Card.CARDS[Integer.parseInt(cardsStrs[i]) - 1];

                    Card extraCard = Card.CARDS[Integer.parseInt(cardsStrs[2 * GameDefinition.CARDS_PER_PLAYER]) - 1];

                    return new CardState(playerCards, extraCard);
                } catch (Exception e) {
                    // report error below
                }
            }
            System.out.printf("Invalid input '%s', try again%n", cardsStr);
        }
    }

    @SuppressWarnings("resource") // don't close System.in
    static Move queryMove(int turn) {
        while (true) {
            System.out.printf("Enter player move %d: ", turn + 1);

            String move = new Scanner(System.in).nextLine();
            String[] moveParts = move.split("\\s+");
            if (moveParts.length == 2) {
                Card card = Card.getByName(moveParts[0]);
                if (card == null) {
                    System.out.printf("Invalid card name '%s', try again%n", moveParts[0]);
                    continue;
                }
                if (card != null) {
                    String pos = moveParts[1].toLowerCase();
                    if (pos.length() == 4) {
                        int px = pos.charAt(0) - 'a';
                        int py = '5' - pos.charAt(1);
                        int nx = pos.charAt(2) - 'a';
                        int ny = '5' - pos.charAt(3);
                        if (verifyRange(px) && verifyRange(py) && verifyRange(nx) && verifyRange(ny))
                            return new Move(card, px, py, nx, ny);
                    }
                }
            }
            System.out.printf("Invalid input '%s', try again%n", move);
        }
    }

    private static boolean verifyRange(int n) {
        return n >= 0 && n < N;
    }
}
