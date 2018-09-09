package onitama.ui.gui;

import static onitama.model.GameDefinition.N;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/** Player card element in the GUI. There will be 5 instances of this class (2 cards per player, plus 1 extra card). */
class GuiCard extends BaseGuiCard {
    private final Gui gui;
    final int player;
    final int cardIndex;

    GridPane board;
    Label name;
    Label flippedName;
    Pane[][] cell = new Pane[N][N];
    boolean selected;

    GuiCard(Gui gui, int player, int cardIndex) {
        this.gui = gui;
        this.player = player;
        this.cardIndex = cardIndex;

        board = new GridPane();

        board.setAlignment(Pos.CENTER);
        board.setHgap(GuiDimension.CARD_CELL_SPACING);
        board.setVgap(GuiDimension.CARD_CELL_SPACING);
        board.setStyle("-fx-background-color: #" + GuiColor.BOARD_SPACING + ";");

        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++)
                board.add(cell[y][x] = GuiUtils.createSquare(GuiDimension.CARD_CELL_WIDTH), x, y);

        int size = GuiDimension.CARD_CELL_WIDTH * N + GuiDimension.CARD_CELL_SPACING * (N + 1);
        GuiUtils.setSize(board, size);

        name = new Label("");
        name.setRotate(0);
        flippedName = new Label("");
        flippedName.setRotate(180);

        if (player >= 1) getChildren().add(flippedName);
        getChildren().add(board);
        if (player != 1) getChildren().add(name);

        setAlignment(Pos.CENTER);

        if (player != 2) {
            setOnMouseEntered(event -> { if (isClickable()) gui.scene.setCursor(Cursor.HAND); });
            setOnMouseExited(event -> { if (isClickable()) gui.scene.setCursor(Cursor.DEFAULT); });
            setOnMouseClicked(event -> {
                if (!isClickable())
                    return;

                if (this == gui.cards.selectedCard)
                {
                    select(false);
                    gui.cards.selectedCard = null;
                } else {
                    if (gui.cards.selectedCard != null)
                        gui.cards.selectedCard.select(false);
                    select(true);
                    gui.cards.selectedCard = this;
                }
                gui.gameBoard.deselect();
                event.consume();
            });
        }
    }

    private boolean isClickable() {
        return !gui.gameState.gameOver() && gui.playerToMove == player && !gui.aiPlayer[player].enabled();
    }

    void select(boolean selected) {
        boolean change = this.selected != selected;
        this.selected = selected;
        if (change) {
            board.setStyle("-fx-background-color: #" + (selected ? GuiColor.CARD_SELECTED_BACKGROUND : GuiColor.BOARD_SPACING) + ";");
            setStyle(selected ? "-fx-background-color: #" + GuiColor.CARD_SELECTED_BACKGROUND + ";" : "");
            name.setStyle(selected ? "-fx-font-weight: bold" : "");
        }
    }

    @Override public void setCellColor(int x, int y, String color) {
        cell[y][x].setStyle("-fx-background-color: #" + color + ";");
    }

    @Override public void setName(String cardName, boolean flipped) {
        (flipped ? flippedName : name).setText(cardName);
        (flipped ? name : flippedName).setText("");
    }
}