package onitama.ui;

import onitama.ai.Searcher;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.SearchParameters;

public class AIPlayer extends Player {

    private final SearchParameters searchParameters;

    public AIPlayer(int player, SearchParameters searchParameters) {
        super(player);
        this.searchParameters = searchParameters;
    }

    @Override public String getName() { return "AI (" + (player+1) + ")"; }

    @Override public Move getMove(int turn, GameState gameState) {
        Searcher searcher = new Searcher(searchParameters.maxDepth, searchParameters.ttBits);

        searcher.setState(player, gameState.board, gameState.cardState);

        searcher.start(searchParameters.maxSearchTimeMs);

        Move move = searcher.bestMove;

        System.out.printf("%nTurn %d: %s plays %s %c%c%c%c%n%n", turn + 1, getName(), move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny);

        return move;
    }
}
