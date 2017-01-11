package onitama.ui;

import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class GameSimulator {
    Player[] players;
    GameState gameState;

    private final boolean verboseOutput;

    public GameSimulator(Player[] players, GameState gameState, boolean verboseOutput) {
        this.players = players;
        this.gameState = gameState;
        this.verboseOutput = verboseOutput;
    }

    /** @return Player that won the game, 0 for player 1 and 1 for player 2, and number of plies the game lasted. */
    public Pair<Integer, Integer> play() {
        int playerWon, ply;
        for (ply = 0; (playerWon = gameState.playerWon()) == -1; ++ply) {
            Output.printGameState(gameState);

            int player = ply & 1;
            Move move = players[player].getMove(ply / 2, gameState);
            gameState.move(player, move);
        }

        Output.printGameState(gameState);

        System.out.printf("Player %d won at move %d (%d plies)!%n", playerWon + 1, ply/2, ply);

        return new Pair<>(playerWon, ply);
    }
}
