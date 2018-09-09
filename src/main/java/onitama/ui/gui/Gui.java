package onitama.ui.gui;

import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import onitama.ai.Searcher;
import onitama.ai.pondering.PonderSearchStats;
import onitama.common.ILogger;
import onitama.common.Utils;
import onitama.model.GameDefinition;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.SearchParameters;
import onitama.ui.AIPlayer;
import onitama.ui.gui.configdialog.GameConfig;
import onitama.ui.gui.configdialog.GameConfigDialog;

/* Main class for the GUI. Builds the GUI and contains the game simulation logic. */
public class Gui extends Application {
    // GUI elements
    Scene scene;

    GameBoard gameBoard;
    GuiCards cards;

    StatusMessage statusMessage = new StatusMessage();
    EvaluationScore evaluationScore;
    TextArea moveLog;

    TextArea log = new LogArea();
    TextArea ponderStats = new LogArea();
    HBox logAndPonder = new HBox(0, log, ponderStats);

    // Game state fields
    GameState gameState;

    int playerToMove;
    GuiAIPlayer[] aiPlayer = { new GuiAIPlayer(), new GuiAIPlayer() };

    private ILogger logger = new ILogger() {
        @Override public void logSearch(String text) {
            Platform.runLater(() -> {
                log.appendText(text + "\n");
            });
        }

        @Override public void logPonder(List<PonderSearchStats> threadStats) {
            Platform.runLater(() -> {
                StringBuilder sb = new StringBuilder("Score Move           Ply  States\n");
                threadStats.forEach(stats -> sb.append(stats.stats + "\n"));
                ponderStats.setText(sb.toString());
            });
        }

        @Override public void logMove(String move) {
            Platform.runLater(() -> {
                log.appendText("\n");
            });
        }
    };

    @Override
    public void start(Stage stage) {
        // game board
        gameBoard = new GameBoard(this);
        Pane labeledBoard = new LabeledBoard(gameBoard);

        // cards
        cards = new GuiCards(this);
        VBox cardBox = new VBox(20, cards.playerCardsPane[1], cards.nextCard, cards.playerCardsPane[0]);
        cardBox.setPadding(new Insets(5, 0, 0, 0));

        // stats region
        evaluationScore = new EvaluationScore(this);

        moveLog = new LogArea();
        GuiUtils.setWidth(moveLog, GuiDimension.MOVE_LOG_WIDTH);
        VBox.setVgrow(moveLog, Priority.ALWAYS);

        Button newGame = new Button("New game");
        newGame.setOnAction(event -> newGame());

        VBox stats = new VBox(10, evaluationScore.text, moveLog, newGame);
        stats.setAlignment(Pos.CENTER);

        // put everything together
        HBox boardCardsAndStats = new HBox(20, labeledBoard, cardBox, stats);

        log.setVisible(false); // temporarily hide log, to prevent it showing with scroll bars during scene construction
        showPonderStats(false);

        VBox layout = new VBox(10, statusMessage.label, boardCardsAndStats, logAndPonder);
        layout.setPadding(new Insets(0, 10, 10, 10));

        layout.setOnMouseClicked(event -> {
            gameBoard.deselect();
            cards.deselect();
        });

        scene = new Scene(layout);

        stage.setTitle("Onitama");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene(); // work-around for https://bugs.openjdk.java.net/browse/JDK-8096889
        stage.show();

        // now show log, making it appear correctly from the start
        HBox.setHgrow(log, Priority.ALWAYS);
        log.setVisible(true);

        // bring up dialog for starting a new game
        if (!newGame()) {
            // shut down if no new game was requested
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            return;
        }
    }

    /** Show or hide the ponder stats area. */
    void showPonderStats(boolean show) {
        if (show) {
            logAndPonder.setSpacing(5);
            GuiUtils.setSize(ponderStats, GuiDimension.PONDER_STATS_WIDTH, GuiDimension.LOG_HEIGHT);
        } else {
            logAndPonder.setSpacing(0);
            GuiUtils.setSize(ponderStats, 0, GuiDimension.LOG_HEIGHT);
        }

        ponderStats.setVisible(show);
    }

    /**
     * Starts a new game. Brings up the New Game dialog, then prepares the game state and GUI, and starts the game.
     * @return False if the New Game dialog is canceled, otherwise true.
     */
    boolean newGame() {
        GameConfigDialog gameConfigDialog = new GameConfigDialog();
        Optional<GameConfig> optionalConfig = gameConfigDialog.showAndWait();

        if (!optionalConfig.isPresent())
            return false;

        // prepare game state
        GameConfig gameConfig = optionalConfig.get();

        playerToMove = 0;

        for (int p = 0; p < 2; ++p) {
            if (aiPlayer[p].enabled())
                aiPlayer[p].stopSearch();

            aiPlayer[p].player = gameConfig.isAI[p] ? new AIPlayer(p, gameConfig.searchParameters[p], gameConfig.ponder[p], logger) : null;
        }

        gameState = gameConfig.gameState;

        // prepare gui
        resetGui(gameConfig.ponder[0] | gameConfig.ponder[1]);

        // start game
        nextMove(null);

        return true;
    }

    void resetGui(boolean showPonderStats) {
        evaluationScore.clear();
        gameBoard.update();
        cards.update();

        log.setText("");
        moveLog.setText("");
        ponderStats.setText("");

        showPonderStats(showPonderStats);
    }

    /** Called for each move, player as well as AI, to update the GUI, and then request the next move. */
    void move(int player, Move move) {
        boolean capture = gameState.move(player, move);
        playerToMove = 1 - playerToMove;

        evaluationScore.update(move, false);
        gameBoard.update(move.px, move.py);
        gameBoard.update(move.nx, move.ny);
        cards.update();

        String moveString = move.toFixedWidthString(capture);
        if (player == 0)
            moveLog.appendText(String.format("%2d. %s", gameState.nrMovesPlayed(), moveString));
        else
            moveLog.appendText("  " + moveString + "\n");

        int playerWon = gameState.playerWon();
        if (playerWon == -1) {
            if (gameState.isDraw())
                statusMessage.setMessage("Game drawn after " + gameState.nrMovesPlayed() + " moves");
        } else {
            statusMessage.setMessage(GameDefinition.PLAYER_COLOR[playerWon] + " player won in " + gameState.nrMovesPlayed() + " moves");
        }

        nextMove(move);
    }

    /** Prepare for doing the next move. This method will return immediately both in the case of an AI or human player.
     * For an AI player, a search is started in a separate task. For a human player, the move is handled by GUI events. */
    void nextMove(Move lastMove) {
        if (gameState.gameOver())
            return;

        String message = GameDefinition.PLAYER_COLOR[playerToMove] + " player to move";
        if (gameState.nrPliesPlayed <= 1 && !aiPlayer[playerToMove].enabled())
            message += ". First select one of your two cards.";
        statusMessage.setMessage(message);

        if (aiPlayer[playerToMove].enabled())
            startAISearchTask(lastMove);

        // if the opponent is an AI player, it might for example want to ponder
        if (aiPlayer[1 - playerToMove].enabled())
            aiPlayer[1 - playerToMove].player.opponentToMove(gameState);

        // if two human players, start a searcher task to evaluate the score after each move
        if (!aiPlayer[0].enabled() && !aiPlayer[1].enabled())
            startScoreEvaluationTask();
    }

    void startAISearchTask(Move lastMove) {
        AISearchTask moveTask = aiPlayer[playerToMove].moveTask = new AISearchTask(lastMove);

        moveTask.setOnFailed(event -> moveTask.getException().printStackTrace());

        moveTask.setOnSucceeded(event -> {
            if (!moveTask.stopped)
                move(playerToMove, moveTask.getValue());
        });

        new Thread(aiPlayer[playerToMove].moveTask).start();
    }

    class AISearchTask extends Task<Move> {
        private final Move lastMove;
        boolean stopped = false;

        AISearchTask(Move lastMove) {
            this.lastMove = lastMove;
        }

        void stop() {
            stopped = true;
        }

        @Override protected Move call() throws Exception {
            return aiPlayer[playerToMove].player.getMove(0, gameState, lastMove);
        }
    }

    void startScoreEvaluationTask() {
        Task<Move> scoreEvaluationTask = new Task<Move>() {
            @Override protected Move call() throws Exception {
                Searcher searcher = new Searcher(new SearchParameters(20, Searcher.MAX_NOMINAL_DEPTH, 1000), Utils.NO_LOGGER, false);
                searcher.setState(playerToMove, gameState.board, gameState.cardState);
                searcher.start();
                return searcher.getBestMove();
            }
        };

        scoreEvaluationTask.setOnFailed(event -> scoreEvaluationTask.getException().printStackTrace());

        scoreEvaluationTask.setOnSucceeded(event -> {
            evaluationScore.update(scoreEvaluationTask.getValue(), true);
        });

        new Thread(scoreEvaluationTask).start();
    }
}