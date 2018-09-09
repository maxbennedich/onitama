package onitama.ui.gui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import onitama.ai.Searcher;
import onitama.model.Move;

/** The score for the current board, calculated and updated every move, and displayed in the GUI. */
class EvaluationScore {
    private final Gui gui;

    final TextFlow text = new TextFlow();

    final Text scoreText = new Text();
    final Text movesText = new Text();

    public EvaluationScore(Gui gui) {
        this.gui = gui;

        scoreText.setFont(new Font(24));
        scoreText.setStyle("-fx-font-weight: bold");

        movesText.setFont(new Font(14));
        movesText.setTranslateY(-1);

        clear();

        text.getChildren().addAll(scoreText, movesText);
        text.setTextAlignment(TextAlignment.CENTER);
    }

    void clear() {
        scoreText.setText("0.00");
        scoreText.setFill(Color.web(GuiColor.EVALUATION_SCORE[2]));
        movesText.setText("");
    }

    void update(Move move, boolean negateScore) {
        int playerWon = gui.gameState.playerWon();

        if (move.score != Searcher.NO_SCORE || playerWon != -1) {
            int score, moves;
            if (playerWon != -1) {
                score = Searcher.WIN_SCORE * (1 - playerWon * 2);
                moves = 0;
            } else {
                score = move.score * (gui.playerToMove * 2 - 1) * (negateScore ? -1 : 1);
                moves = move.scoreSearchDepth / 2;
            }

            scoreText.setFill(Color.web(GuiColor.EVALUATION_SCORE[score > 0 ? 0 : score < 0 ? 1 : 2]));

            if (Math.abs(score) == Searcher.WIN_SCORE) {
                scoreText.setText("Mate" + (moves == 0 ? "" : " in " + moves));
                movesText.setText("");
            } else {
                scoreText.setText(score == 0 ? "0.00 " : String.format("+%.2f ", Math.abs(score) / 100.0));
                movesText.setText("(in " + moves + " moves)");
            }
        }
    }
}