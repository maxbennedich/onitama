package onitama.ui.console;

import onitama.model.GameState;
import onitama.model.Move;
import onitama.ui.Player;

public class GameSimulator {
    private final Player[] players;
    private final GameState gameState;

    public GameSimulator(Player[] players, GameState gameState) {
        this.players = players;
        this.gameState = gameState;
    }

    public static class GameResult {
        /** 0 for player 1, 1 for player 2, -1 for draw. */
        public final int playerWon;

        /** Number of plies the game lasted. */
        public final int plies;

        /** Total number of nodes evaluated. */
        public final long nodesEvaluated;

        GameResult(int playerWon, int plies, long nodesEvaluated) {
            this.playerWon = playerWon;
            this.plies = plies;
            this.nodesEvaluated = nodesEvaluated;
        }
    }

    public GameResult play() {
        Move prevMove = null;
        int playerWon = -1, ply;
        long nodesEvaluated = 0;

        for (ply = 0; (playerWon = gameState.playerWon()) == -1 && !gameState.isDraw();) {
            Output.printGameState(gameState);

            int player = ply & 1;
            players[1-player].opponentToMove(gameState);
            Move move = players[player].getMove(ply / 2, gameState, prevMove);
            gameState.move(player, move);

            prevMove = move;
            nodesEvaluated += move.nodesEvaluated;
            ++ply;
        }

        for (int p = 0; p < 2; ++p)
            players[p].gameOver();

        Output.printGameState(gameState);
        if (playerWon == -1)
            Output.printf("Game aborted after move %d (%d plies)!%n", ply/2, ply);
        else
            Output.printf("Player %d won at move %d (%d plies)!%n", playerWon + 1, ply/2, ply);

        return new GameResult(playerWon, ply, nodesEvaluated);
    }
}
