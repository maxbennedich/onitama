package onitama.ai;

import static onitama.model.GameDefinition.CARDS_PER_PLAYER;
import static onitama.model.GameDefinition.N;

import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.GameDefinition;

class SearchState {
    private static final int PAWN = 0;
    private static final int KING = 1;

    /** Maps internally used card ids (0 - 4) to external ones (in {@link Card}). */
    private final int[] cardMapping = new int[CARDS_PER_PLAYER * 2 + 1];

    int[] bitboardPlayer = {0, 0};
    int[] bitboardKing = {0, 0};
    int cardBits;
    long zobrist;

    void initPlayer(int playerTurn) {
        if (playerTurn == 1)
            zobrist ^= Zobrist.SHIFT_PLAYER; // to make hash values deterministic regardless of initial player
    }

    void initCards(CardState cardState) {
        for (int p = 0; p < 2; ++p)
            for (int c = 0; c < CARDS_PER_PLAYER; ++c)
                zobrist ^= Zobrist.CARD[p][p*2 + c];

        // extra card, p0 c0, p0 c1, p1 c0, p1 c1
        cardBits = 4 + (0 << 3) + (1 << 6) + (2 << 9) + (3 << 12);

        for (int c = 0; c < CARDS_PER_PLAYER * 2; ++c)
            cardMapping[c] = cardState.playerCards[c/2][c&1].id;
        cardMapping[CARDS_PER_PLAYER * 2] = cardState.nextCard.id;
    }

    CardState getCardState() {
        return new CardState(new Card[][] {{getCard((cardBits>>3)&7), getCard((cardBits>>6)&7)}, {getCard((cardBits>>9)&7), getCard((cardBits>>12)&7)}}, getCard(cardBits&7));
    }

    /** @return {@link Card} for given internal card id (ranged 0 - 4). */
    Card getCard(int id) {
        return Card.CARDS[cardMapping[id]];
    }

    void initBoard(String board) {
        for (int y = 0, bit = 1; y < N; ++y) {
            for (int x = 0; x < N; ++x, bit *= 2) {
                if (board.charAt(y*5+x) != '.') {
                    if (board.charAt(y*5+x) == 'w') { bitboardPlayer[0] |= bit; zobrist ^= Zobrist.PIECE[0][0][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'b') { bitboardPlayer[1] |= bit; zobrist ^= Zobrist.PIECE[1][0][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'W') { bitboardPlayer[0] |= bit; bitboardKing[0] |= bit; zobrist ^= Zobrist.PIECE[0][1][y*5+x]; }
                    else if (board.charAt(y*5+x) == 'B') { bitboardPlayer[1] |= bit; bitboardKing[1] |= bit; zobrist ^= Zobrist.PIECE[1][1][y*5+x]; }
                }
            }
        }
    }

    /**
     * @return True if the first card in the hand has a lower card id than the second card. Since the zobrist hash doesn't
     * differentiate between card order for each player's two cards, this logic is used to store which of the two cards is
     * used in a single bit, by just storing whether the lower or higher card was used.
     */
    boolean firstCardLower(int player) {
        int card0 = ((cardBits >> 3 * (1 + player * 2)) & 7);
        int card1 = ((cardBits >> 3 * (1 + player * 2 + 1)) & 7);
        return card0 < card1;
    }

    void copyFrom(SearchState state) {
        zobrist = state.zobrist;
        cardBits = state.cardBits;
        bitboardPlayer[0] = state.bitboardPlayer[0];
        bitboardPlayer[1] = state.bitboardPlayer[1];
        bitboardKing[0] = state.bitboardKing[0];
        bitboardKing[1] = state.bitboardKing[1];
    }

    void move(int player, int oldPos, int newPos, int cardUsed) {
        int oldPosMask = 1 << oldPos;
        int newPosMask = 1 << newPos;

        if ((bitboardPlayer[1-player] & newPosMask) != 0) {
            // opponent player piece captured
            bitboardPlayer[1-player] &= ~newPosMask; // remove opponent piece

            int capturedPiece = (bitboardKing[1-player] & newPosMask) != 0 ? KING : PAWN;
            if (capturedPiece == KING)
                bitboardKing[1-player] &= ~newPosMask; // remove opponent king

            zobrist ^= Zobrist.PIECE[1 - player][capturedPiece][newPos];
        }

        bitboardPlayer[player] &= ~oldPosMask; // remove piece from current position
        bitboardPlayer[player] |= newPosMask; // add piece to new position

        int movedPiece = bitboardKing[player] == oldPosMask ? KING : PAWN;
        if (movedPiece == KING) {
            bitboardKing[player] &= ~oldPosMask; // remove king from current position
            bitboardKing[player] |= newPosMask; // add king to new position
        }

        zobrist ^= Zobrist.PIECE[player][movedPiece][oldPos];
        zobrist ^= Zobrist.PIECE[player][movedPiece][newPos];

        int cardUsedPos = 3 * (1 + player * 2 + cardUsed);
        int cardUsedId = ((cardBits >> cardUsedPos) & 7);
        int nextCardId = cardBits & 7;
        zobrist ^= Zobrist.CARD[player][cardUsedId];
        zobrist ^= Zobrist.CARD[player][nextCardId];

        zobrist ^= Zobrist.SHIFT_PLAYER;

        cardBits &= ~(7 + (7 << cardUsedPos));
        cardBits |= cardUsedId + (nextCardId << cardUsedPos);
    }

    /** Score for each position on the board. (Larger score is better.) */
    private static final int SCORE_1 = 0b01010_10001_00000_10001_01010;
    private static final int SCORE_2 = 0b00100_01010_10001_01010_00100;
    private static final int SCORE_3 = 0b00000_00100_01010_00100_00000;
    private static final int SCORE_4 = 0b00000_00000_00100_00000_00000;

    int score(int playerToEvaluate) {
        int pieceScore0 =
                Integer.bitCount(bitboardPlayer[0] & SCORE_1) +
                2 * Integer.bitCount(bitboardPlayer[0] & SCORE_2) +
                3 * Integer.bitCount(bitboardPlayer[0] & SCORE_3) +
                4 * Integer.bitCount(bitboardPlayer[0] & SCORE_4);

        int pieceScore1 =
                Integer.bitCount(bitboardPlayer[1] & SCORE_1) +
                2 * Integer.bitCount(bitboardPlayer[1] & SCORE_2) +
                3 * Integer.bitCount(bitboardPlayer[1] & SCORE_3) +
                4 * Integer.bitCount(bitboardPlayer[1] & SCORE_4);

        int materialDifference = Integer.bitCount(bitboardPlayer[0]) - Integer.bitCount(bitboardPlayer[1]);
        int score = materialDifference*100 + (pieceScore0 - pieceScore1)*1;

        return playerToEvaluate == 0 ? score : -score;
    }

    /** @return Whether the current board is a win for the given player. */
    boolean won(int player) {
        return bitboardKing[1-player] == 0 || bitboardKing[player] == GameDefinition.WIN_BITMASK[player];
    }
}