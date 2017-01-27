package onitama.ui;

import onitama.model.GameState;
import onitama.model.Move;

public abstract class Player {

    final protected int player;

    Player(int player) {
        this.player = player;
    }

    public abstract void gameOver();

    public abstract String getName();

    public abstract void opponentToMove(GameState gameState);

    public abstract Move getMove(int turn, GameState gameState, Move opponentMove);

}
