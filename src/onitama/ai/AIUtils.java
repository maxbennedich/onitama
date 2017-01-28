package onitama.ai;

import java.util.List;

import onitama.model.Card;
import onitama.model.CardState;
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

    public static GameState getGameState(int[] bitboardPlayer, int[] bitboardKing, int cardBits) {
        return new GameState(getBoard(bitboardPlayer, bitboardKing), getCardState(cardBits));
    }

    private static char[] BOARD_MARKERS = new char[] {' ', 'w', 'b', 'W', 'B'};

    private static String getBoard(int[] bitboardPlayer, int[] bitboardKing) {
        char[] board = new char[Searcher.NN];
        for (int p = 0, bit = 1; p < Searcher.NN; ++p, bit *= 2) {
            int c = 0;
            if ((bitboardKing[0] & bit) != 0) c = 3;
            else if ((bitboardKing[1] & bit) != 0) c = 4;
            else if ((bitboardPlayer[0] & bit) != 0) c = 1;
            else if ((bitboardPlayer[1] & bit) != 0) c = 2;
            board[p] = BOARD_MARKERS[c];
        }
        return new String(board);
    }

    private static CardState getCardState(int cardBits) {
        return new CardState(new Card[][] {{Card.CARDS[(cardBits>>4)&15], Card.CARDS[(cardBits>>8)&15]}, {Card.CARDS[(cardBits>>12)&15], Card.CARDS[(cardBits>>16)&15]}}, Card.CARDS[cardBits&15]);
    }

}