package onitama.model;

import java.util.ArrayList;
import java.util.List;

public enum Deck {
    Original("Original"),
    SenseisPath("Sensei's Path"),
    Promo("Promo"),
    ;

    public static final int NR_DECKS;

    static {
        NR_DECKS = Deck.values().length;
    }

    public final String name;
    public final List<Card> cards = new ArrayList<>();

    private Deck(String name) {
        this.name = name;
    }
}