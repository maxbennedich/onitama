package onitama.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardState {
    public Card[][] playerCards = new Card[2][GameDefinition.CARDS_PER_PLAYER];
    public Card nextCard;

    private CardState(Card[] cards) {
        playerCards[0][0] = cards[0];
        playerCards[0][1] = cards[1];
        playerCards[1][0] = cards[2];
        playerCards[1][1] = cards[3];
        nextCard = cards[4];
    }

    public CardState(Card[][] playerCards, Card nextCard) {
        // create defensive copy
        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                this.playerCards[p][c] = playerCards[p][c];

        this.nextCard = nextCard;
    }

    public Card[] cards() {
        return new Card[] { playerCards[0][0], playerCards[0][1], playerCards[1][0], playerCards[1][1], nextCard };
    }

    /**
     * Draws 5 random cards.
     * @param decks True/false for each deck whether it should be included in the random draw.
     */
    public static CardState random(Random rnd, boolean[] decks) {
        List<Integer> cards = new ArrayList<>();
        for (int c = 0; c < Card.NR_CARDS; ++c)
            if (decks[Card.CARDS[c].deck.ordinal()])
                cards.add(c);

        Card[] c = new Card[5];
        for (int n = 0; n < 5; ++n)
            c[n] = Card.CARDS[cards.remove(rnd.nextInt(cards.size()))];

        return new CardState(c);
    }

    /** Calls {@link #random(Random, boolean[])} with a new random instance. */
    public static CardState random(boolean[] decks) {
        return random(new Random(), decks);
    }
}
