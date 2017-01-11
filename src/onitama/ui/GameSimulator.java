package onitama.ui;

import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class GameSimulator {
    /** To prevent stuck games when AI plays itself. */
    public static final int MAX_PLIES_BEFORE_DRAW = 200;

    Player[] players;
    GameState gameState;

    public GameSimulator(Player[] players, GameState gameState) {
        this.players = players;
        this.gameState = gameState;
    }

    /** @return Player that won the game, 0 for player 1, 1 for player 2, -1 for draw, and number of plies the game lasted. */
    public Pair<Integer, Integer> play() {
        int playerWon = -1, ply;
        for (ply = 0; (playerWon = gameState.playerWon()) == -1 && ply < MAX_PLIES_BEFORE_DRAW;) {
            Output.printGameState(gameState);

            int player = ply & 1;
            Move move = players[player].getMove(ply / 2, gameState);
            gameState.move(player, move);
            ++ply;
        }

        Output.printGameState(gameState);
        if (playerWon == -1)
            Output.printf("Game aborted after move %d (%d plies)!%n", ply/2, ply);
        else
            Output.printf("Player %d won at move %d (%d plies)!%n", playerWon + 1, ply/2, ply);

        return new Pair<>(playerWon, ply);
    }
}
