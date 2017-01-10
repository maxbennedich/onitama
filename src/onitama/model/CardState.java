package onitama.model;

public class CardState {
    public Card[][] playerCards = new Card[2][GameDefinition.CARDS_PER_PLAYER];
    public Card nextCard;

    public CardState(Card[][] playerCards, Card nextCard) {
        this.playerCards = playerCards;
        this.nextCard = nextCard;
    }
}
