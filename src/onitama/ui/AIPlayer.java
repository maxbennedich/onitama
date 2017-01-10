package onitama.ui;

import onitama.ai.Searcher;
import onitama.model.GameState;
import onitama.model.Move;

public class AIPlayer extends Player {

    static final int TT_BITS = 26; // log of nr of entries; 24 => 192 MB, 26 => 768 MB, 28 => 3 GB
    static final int MAX_DEPTH = 11;

    AIPlayer(int player) { super(player); }

    @Override public String getName() { return "AI (" + (player+1) + ")"; }

    @Override public Move getMove(int turn, GameState gameState) {
        Searcher searcher = new Searcher(MAX_DEPTH, TT_BITS);

        searcher.setState(player, gameState.board, gameState.cardState);

//        System.out.printf("Transposition table size: %d entries (%.0f MB)%n", searcher.tt.sizeEntries(), searcher.tt.sizeBytes() / 1024.0 / 1024.0);

//        searcher.printBoard();

//        long time = System.currentTimeMillis();

        searcher.start(5000);

//        time = System.currentTimeMillis() - time;

//        System.out.println();
//        searcher.printStats();
//
//        System.out.printf("%nElapsed time: %d ms%n", time);

        Move move = searcher.bestMove;

        System.out.printf("%nTurn %d: %s plays %s %c%c%c%c%n%n", turn + 1, getName(), move.card.name, 'a'+move.px, '5'-move.py, 'a'+move.nx, '5'-move.ny);

        return move;
    }
}
