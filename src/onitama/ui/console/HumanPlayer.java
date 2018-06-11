package onitama.ui.console;

import onitama.common.ILogger;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.ui.Player;

public class HumanPlayer extends Player {

    HumanPlayer(int player, ILogger logger) { super(player, logger); }

    @Override public void gameOver() { }

    @Override public String getName() { return "Player (" + (player+1) + ")"; }

    @Override public void opponentToMove(GameState gameState) { }

    @Override public Move getMove(int turn, GameState gameState, Move opponentMove) {
        Move move = Input.queryMove(turn);
        System.out.println();
        return move;
    }
}
