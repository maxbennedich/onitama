package onitama.ai;

import java.util.List;

import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;
import onitama.model.SearchParameters;

public class AIUtils {

    public static List<Pair<Move, GameState>> getPossibleMoves(int player, GameState gameState) {
        // create a dummy searcher to extract the list of possible moves
        Searcher searcher = new Searcher(50, 1, 0, false);
        searcher.setState(player, gameState.board, gameState.cardState);
        return searcher.getAllMoves();
    }

    public static Searcher startNewSearcher(int player, GameState gameState, SearchParameters searchParameters) {
        Searcher searcher = new Searcher(searchParameters.maxDepth, searchParameters.ttBits, searchParameters.maxSearchTimeMs, true);
        searcher.setState(player, gameState.board, gameState.cardState);

        searcher.start();

        return searcher;
    }

}
