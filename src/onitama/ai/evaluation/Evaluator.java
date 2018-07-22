package onitama.ai.evaluation;

import onitama.ai.SearchState;
import onitama.model.GameDefinition;

public abstract class Evaluator {
    protected static int SCORE_PER_PIECE = 100;

    protected final SearchState state;

    protected Evaluator(SearchState state) {
        this.state = state;
    }

    public abstract int score(int playerToEvaluate);

    public String explain() {
        return score(0) + " (" + GameDefinition.PLAYER_COLOR[0] + ")";
    }
}

