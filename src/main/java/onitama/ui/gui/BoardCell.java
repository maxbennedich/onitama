package onitama.ui.gui;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import onitama.model.GameState.CellType;

/** An individual cell element displayed in the GUI. Part of a {@link GameBoard}. */
class BoardCell extends StackPane {
    /** Used to indicate the main pawn. */
    static Image STAR = new Image(Gui.class.getResourceAsStream("star.png"));

    final Gui gui;
    final int x;
    final int y;

    /** Used to indicate selected cells and valid moves. */
    boolean marked = false;

    BoardCell(Gui gui, int x, int y) {
        this.gui = gui;
        this.x = x;
        this.y = y;

        GuiUtils.setSize(this, GuiDimension.BOARD_CELL_WIDTH);
        GuiUtils.setColor(this, "fff");

        setOnMouseEntered(event -> { gui.scene.setCursor(gui.gameBoard.cellIsClickable(x, y) ? Cursor.HAND : Cursor.DEFAULT); });
        setOnMouseExited(event -> { if (gui.gameBoard.cellIsClickable(x, y)) gui.scene.setCursor(Cursor.DEFAULT); });
        setOnMouseClicked(event -> {
            if (!gui.aiPlayer[gui.playerToMove].enabled() && !gui.gameState.gameOver())
                gui.gameBoard.click(x, y);
            event.consume();
        });
    }

    void mark(boolean marked) {
        if (this.marked != marked) {
            this.marked = marked;
            update();
        }
    }

    void update() {
        CellType cellType = gui.gameState.cellAt(x, y);

        String color = GuiColor.BOARD_PIECE[cellType.isEmpty() ? 2 : cellType.player][marked ? 1 : 0];
        setStyle("-fx-background-color: #" + color + ";");

        if (cellType.king)
            getChildren().add(new ImageView(STAR));
        else
            getChildren().clear();
    }
}