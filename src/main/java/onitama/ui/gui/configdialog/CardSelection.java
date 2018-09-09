package onitama.ui.gui.configdialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import onitama.model.Card;
import onitama.model.Deck;

/** Contains the current selection and assignment of cards in the configuration dialog. */
class CardSelection {
    /** Menu constant for deselecting a currently selected card. */
    static final int DESELECT = 3;

    List<ConfigCard> cards = new ArrayList<>();
    int[] playerByCard = new int[Card.NR_CARDS];

    /** Counters used to keep track of the order that cards were added */
    int[] counterByCard = new int[Card.NR_CARDS];
    int counter = 0;

    CardSelection() {
        Arrays.fill(playerByCard, -1);
    }

    void select(int player, int card) {
        if (player == DESELECT) {
            deselect(card);
            return;
        }

        // selecting already selected card, for same player
        if (playerByCard[card] == player) {
            counterByCard[card] = counter++;
            return;
        }

        int[] matches = new int[2];
        for (int c = 0, count = 0; c < Card.NR_CARDS; ++c) {
            if (playerByCard[c] == player)
                matches[count++] = c;

            int deselect = -1;
            if (player == 2 && count == 1) {
                // if selecting a new extra card, deselect the old one
                deselect = c;
            } else if (player < 2 && count == 2) {
                // if trying to select a third player card, deselect the oldest one
                deselect = counterByCard[matches[0]] < counterByCard[matches[1]] ? matches[0] : matches[1];
            }

            if (deselect != -1) {
                deselect(deselect);
                break;
            }
        }

        playerByCard[card] = player;
        counterByCard[card] = counter++;
        cards.get(card).select(player);
    }

    void deselect(int card) {
        playerByCard[card] = -1;
        cards.get(card).deselect();
    }

    private void disable(int card, boolean disable) {
        cards.get(card).paintCard(Card.CARDS[card], false, !disable);
    }

    public void disableDeck(Deck deck, boolean disable) {
        for (int card = 0; card < Card.NR_CARDS; ++card) {
            if (Card.CARDS[card].deck == deck) {
                disable(card, disable);
                if (disable)
                    deselect(card);
            }
        }
    }
}