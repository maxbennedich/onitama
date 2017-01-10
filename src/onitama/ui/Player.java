package onitama.ui;

import onitama.model.GameState;
import onitama.model.Move;

public abstract class Player {

    final protected int player;

    Player(int player) {
        this.player = player;
    }

    public abstract String getName();

    public abstract Move getMove(int turn, GameState gameState);

}
