package onitama.ui.gui;

import static onitama.model.GameDefinition.N;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/** Wrapper around a game board that adds cell labels. */
class LabeledBoard extends HBox {
    LabeledBoard(Pane board) {
        super(0);
        VBox withHorizontalLabels = new VBox(0, board, new HorizontalBoardLabels());
        getChildren().addAll(new VerticalBoardLabels(), withHorizontalLabels);
    }

    static class HorizontalBoardLabels extends GridPane {
        HorizontalBoardLabels() {
            for (int n = 0; n < N; ++n) {
                Label label = getBoardLabel((char)('a'+n) + "");
                GuiUtils.setWidth(label, GuiDimension.BOARD_CELL_WIDTH + GuiDimension.BOARD_CELL_SPACING);
                label.setPadding(new Insets(0, 0, 0, GuiDimension.BOARD_CELL_SPACING));
                add(label, n, 0);
            }
        }
    }

    static class VerticalBoardLabels extends GridPane {
        VerticalBoardLabels() {
            for (int n = 0; n < N; ++n) {
                Label label = getBoardLabel((N-n) + "");
                GuiUtils.setHeight(label, GuiDimension.BOARD_CELL_WIDTH + GuiDimension.BOARD_CELL_SPACING);
                label.setPadding(new Insets(GuiDimension.BOARD_CELL_SPACING, 5, 0, 0));
                add(label, 0, n);
            }
        }
    }

    private static Label getBoardLabel(String text) {
        return new CenteredLabel(text, 16);
    }
}