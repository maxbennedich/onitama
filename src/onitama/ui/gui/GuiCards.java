package onitama.ui.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/** This class contains the 5 player cards, and some associated logic. */
class GuiCards {
    final Gui gui;

    Pane[] playerCardsPane = new Pane[2];

    GuiCard[][] playerCards = new GuiCard[2][2];
    GuiCard nextCard;

    GuiCard selectedCard = null;

    GuiCards(Gui gui) {
        this.gui = gui;

        for (int p = 0; p < 2; ++p)
            playerCardsPane[p] = createCards(p);

        nextCard = createCard(2, 0);
    }

    private GuiCard createCard(int player, int cardIndex) {
        return new GuiCard(gui, player, cardIndex);
    }

    private Pane createCards(int player) {
        playerCards[player][0] = createCard(player, 0);
        playerCards[player][1] = createCard(player, 1);

        Label spacer = new Label("");
        GuiUtils.setWidth(spacer, GuiDimension.SPACE_BETWEEN_PLAYER_CARDS);

        HBox cards = new HBox(10, playerCards[player][0], spacer, playerCards[player][1]);
        cards.setAlignment(Pos.CENTER);

        return cards;
    }

    void flash(int player) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(50), event -> {
                for (int c = 0; c < 2; ++c)
                    playerCards[player][c].select(true);
            }),
            new KeyFrame(Duration.millis(100), event -> {
                for (int c = 0; c < 2; ++c)
                    playerCards[player][c].select(false);
            }));
        timeline.setCycleCount(5);
        timeline.play();
    }

    void update() {
        nextCard.paintCard(gui.gameState.cardState.nextCard, gui.playerToMove == 1, false);

        for (int player = 0; player < 2; ++player)
            for (int card = 0; card < 2; ++card)
                playerCards[player][card].paintCard(gui.gameState.cardState.playerCards[player][card], player == 1, player == gui.playerToMove);
    }

    void deselect() {
        if (selectedCard != null) {
            selectedCard.select(false);
            selectedCard = null;
        }
    }
}