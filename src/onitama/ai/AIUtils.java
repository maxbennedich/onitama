package onitama.ai;

import static onitama.model.GameDefinition.NN;

import java.util.List;

import onitama.common.Utils;
import onitama.model.GameState;
import onitama.model.Move;
import onitama.model.Pair;

public class AIUtils {

    public static List<Pair<Move, GameState>> getPossibleMoves(int player, GameState gameState) {
        // create a dummy searcher to extract the list of possible moves
        Searcher searcher = new Searcher(Searcher.MAX_DEPTH, 1, 0, false, Utils.NO_LOGGER, false);
        searcher.setState(player, gameState.board, gameState.cardState);
        return searcher.getAllMoves();
    }

    public static GameState getGameState(SearchState state) {
        return new GameState(getBoard(state.bitboardPlayer, state.bitboardKing), state.getCardState());
    }

    private static char[] BOARD_MARKERS = new char[] {' ', 'w', 'b', 'W', 'B'};

    private static String getBoard(int[] bitboardPlayer, int[] bitboardKing) {
        char[] board = new char[NN];
        for (int p = 0, bit = 1; p < NN; ++p, bit *= 2) {
            int c = 0;
            if ((bitboardKing[0] & bit) != 0) c = 3;
            else if ((bitboardKing[1] & bit) != 0) c = 4;
            else if ((bitboardPlayer[0] & bit) != 0) c = 1;
            else if ((bitboardPlayer[1] & bit) != 0) c = 2;
            board[p] = BOARD_MARKERS[c];
        }
        return new String(board);
    }
}
