package onitama.ui.gui;

import static onitama.model.GameDefinition.N;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import onitama.model.Card;
import onitama.model.GameState.CellType;
import onitama.model.Move;

/** The main game board element in the GUI, consisting of 5x5 {@link BoardCell}s. */
class GameBoard extends GridPane {
    final Gui gui;

    BoardCell[][] boardCell = new BoardCell[N][N];

    int selectedCell = -1;

    GameBoard(Gui gui) {
        this.gui = gui;

        setAlignment(Pos.CENTER);
        setHgap(5);
        setVgap(5);
        setStyle("-fx-background-color: #" + GuiColor.BOARD_SPACING + ";");

        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++)
                add(boardCell[y][x] = new BoardCell(gui, x, y), x, y);

        VBox.setVgrow(this, Priority.ALWAYS);

        GuiUtils.setSize(this, GuiDimension.BOARD_SIZE);
    }

    void update(int x, int y) {
        boardCell[y][x].update();
    }

    void update() {
        for (int y = 0; y < N; ++y)
            for (int x = 0; x < N; ++x)
                update(x, y);
    }

    void mark(boolean[][] marked) {
        for (int y = 0; y < N; ++y)
            for (int x = 0; x < N; ++x)
                boardCell[y][x].mark(marked[y][x]);
    }

    void deselect() {
        for (int y = 0; y < N; ++y)
            for (int x = 0; x < N; ++x)
                boardCell[y][x].mark(false);
        selectedCell = -1;
    }

    void click(int x, int y) {
        if (gui.cards.selectedCard == null) {
            gui.statusMessage.setMessage("Select a card first!", Duration.millis(5 * 100));
            gui.cards.flash(gui.playerToMove);
            return;
        }

        CellType ct = gui.gameState.cellAt(x, y);
        Card card = gui.gameState.cardState.playerCards[gui.playerToMove][gui.cards.selectedCard.cardIndex];

        if (ct.player == gui.playerToMove) {
            // clicking one's own piece
            if (selectedCell == y * N + x) {
                // if clicking the piece that's already selected, deselect it
                deselect();
            } else {
                // if clicking a non-selected piece, mark valid moves on the board
                selectedCell = y * N + x;

                boolean[][] marked = new boolean[N][N];

                int cardMoves = card.moveBitmask[gui.playerToMove][y * N + x];

                for (int v = 0; v < N; ++v)
                    for (int u = 0; u < N; ++u)
                        marked[v][u] = gui.gameState.cellAt(u, v).player != gui.playerToMove && (cardMoves & (1 << v * N + u)) != 0;

                // mark selected cell
                marked[y][x] = true;

                mark(marked);
            }
        } else if (selectedCell != -1) {
            int px = selectedCell % N, py = selectedCell / N;

            if (isValidMove(px, py, x, y)) {
                // a cell had previously been selected, and a cell representing a valid move was just clicked
                gui.cards.deselect();
                gui.scene.setCursor(Cursor.DEFAULT);
                gui.move(gui.playerToMove, new Move(card, px, py, x, y));
            }

            deselect();
        }
    }

    boolean cellIsClickable(int x, int y) {
        if (gui.cards.selectedCard == null)
            return false;

        CellType ct = gui.gameState.cellAt(x, y);

        if (ct.player == gui.playerToMove)
            return true;

        return selectedCell != -1 && isValidMove(selectedCell % N, selectedCell / N, x, y);
    }

    boolean isValidMove(int x, int y, int u, int v) {
        int player = gui.gameState.cellAt(x, y).player;

        // onto oneself
        if (gui.gameState.cellAt(u, v).player == player)
            return false;

        Card card = gui.gameState.cardState.playerCards[player][gui.cards.selectedCard.cardIndex];
        int cardMoves = card.moveBitmask[player][y * N + x];

        return (cardMoves & (1 << v * N + u)) != 0;
    }
}