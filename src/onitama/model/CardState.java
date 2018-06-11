package onitama.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardState {
    public Card[][] playerCards = new Card[2][GameDefinition.CARDS_PER_PLAYER];
    public Card nextCard;

    public CardState(Card[][] playerCards, Card nextCard) {
        // create defensive copy
        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < GameDefinition.CARDS_PER_PLAYER; ++c)
                this.playerCards[p][c] = playerCards[p][c];

        this.nextCard = nextCard;
    }

    public static CardState Random() {
        List<Integer> cards = IntStream.range(0, Card.NR_CARDS).boxed().collect(Collectors.toList());

        Random rnd = new Random();

        Card[] c = new Card[5];
        for (int n = 0; n < c.length; ++n) {
            int idx = rnd.nextInt(cards.size());
            c[n] = Card.CARDS[cards.get(idx)];
            cards.remove(idx);
        }

        return new CardState(new Card[][] {{ c[0], c[1] }, { c[2], c[3] }}, c[4]);
    }
}
