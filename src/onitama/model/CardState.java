package onitama.model;

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
}
