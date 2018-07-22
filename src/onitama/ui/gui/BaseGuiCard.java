package onitama.ui.gui;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;

import javafx.scene.layout.VBox;
import onitama.model.Card;

/** Base class for a card element displayed in the GUI. */
public abstract class BaseGuiCard extends VBox {
    protected abstract void setName(String cardName, boolean flipped);

    protected abstract void setCellColor(int x, int y, String color);

    public void paintCard(Card card, boolean flipped, boolean enabled) {
        setDisable(!enabled);

        setName(card.name, flipped);

        int idx = enabled ? 0 : 1;
        for (int y = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x) {
                String color = x == N/2 && y == N/2 ? GuiColor.CARD_CENTER[idx] : GuiColor.CARD_NO_MOVE[idx];
                if ((card.moveBitmask[flipped ? 1 : 0][NN/2] & (1 << y * N + x)) != 0)
                    color = GuiColor.CARD_LEGAL_MOVE[idx];
                setCellColor(x, y, color);
            }
        }
    }
}