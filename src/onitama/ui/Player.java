package onitama.ui;

import onitama.common.ILogger;
import onitama.model.GameState;
import onitama.model.Move;

public abstract class Player {

    protected final int player;
    protected final ILogger logger;

    protected Player(int player, ILogger logger) {
        this.player = player;
        this.logger = logger;
    }

    public abstract void gameOver();

    public abstract String getName();

    public abstract void opponentToMove(GameState gameState);

    public abstract Move getMove(int turn, GameState gameState, Move opponentMove);

}
