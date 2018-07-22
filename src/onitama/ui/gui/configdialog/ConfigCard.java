package onitama.ui.gui.configdialog;

import static onitama.model.GameDefinition.N;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import onitama.model.GameDefinition;
import onitama.ui.gui.BaseGuiCard;
import onitama.ui.gui.GuiColor;
import onitama.ui.gui.GuiDimension;
import onitama.ui.gui.GuiUtils;

/** Represents a single card in the configuration dialog. */
class ConfigCard extends BaseGuiCard {
    private GridPane board;
    private Pane[][] cell = new Pane[N][N];
    private Label name;

    ConfigCard(int card, Dialog<?> dialog, CardSelection configCards) {
        board = new GridPane();

        board.setAlignment(Pos.CENTER);
        board.setHgap(GuiDimension.CONFIG_CARD_CELL_SPACING);
        board.setVgap(GuiDimension.CONFIG_CARD_CELL_SPACING);
        board.setStyle("-fx-background-color: #" + GuiColor.BOARD_SPACING + ";");

        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++)
                board.add(cell[y][x] = GuiUtils.createSquare(GuiDimension.CONFIG_CARD_CELL_WIDTH), x, y);

        int size = GuiDimension.CONFIG_CARD_CELL_WIDTH * N + GuiDimension.CONFIG_CARD_CELL_SPACING * (N + 1);
        GuiUtils.setSize(board, size);

        setSpacing(2);
        getChildren().add(board);
        getChildren().add(name = new Label(""));
        name.setFont(new Font(11));
        setAlignment(Pos.CENTER);

        ContextMenu playerSelection = getPlayerSelection(card, configCards, false);
        ContextMenu playerSelectionWithDeselect = getPlayerSelection(card, configCards, true);

        setOnMouseEntered(event -> dialog.getDialogPane().setCursor(Cursor.HAND));
        setOnMouseExited(event -> dialog.getDialogPane().setCursor(Cursor.DEFAULT));
        setOnMouseClicked(event -> (configCards.playerByCard[card] == -1 ? playerSelection : playerSelectionWithDeselect).show(this, event.getScreenX(), event.getScreenY()));
    }

    private ContextMenu getPlayerSelection(int card, CardSelection configCards, boolean includeDeselect) {
        MenuItem[] players = new MenuItem[includeDeselect ? 4 : 3];
        for (int p = 0; p < players.length; ++p) {
            players[p] = new MenuItem(p == CardSelection.DESELECT ? "Deselect" : p == 2 ? "Extra card" : GameDefinition.PLAYER_COLOR[p] + " player");
            final int player = p;
            players[p].setOnAction(event -> configCards.select(player, card));
        }
        return new ContextMenu(players);
    }

    void select(int player) {
        select(player, true);
    }

    void deselect() {
        select(-1, false);
    }

    private void select(int player, boolean selected) {
        board.setStyle("-fx-background-color: #" + (selected ? GuiColor.CONFIG_CARD_SELECTED_BACKGROUND[player] : GuiColor.BOARD_SPACING) + ";");
        setStyle(selected ? "-fx-background-color: #" + GuiColor.CONFIG_CARD_SELECTED_BACKGROUND[player] + ";" : "");
        name.setStyle(selected ? "-fx-font-weight: bold" : "");
    }

    @Override public void setCellColor(int x, int y, String color) {
        cell[y][x].setStyle("-fx-background-color: #" + color + ";");
    }

    @Override public void setName(String cardName, boolean flipped) {
        this.name.setText(cardName);
    }
}