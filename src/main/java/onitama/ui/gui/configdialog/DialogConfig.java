package onitama.ui.gui.configdialog;

import java.util.Arrays;

import onitama.ai.Searcher;
import onitama.ai.TranspositionTable;
import onitama.model.Card;
import onitama.model.CardState;
import onitama.model.Deck;
import onitama.model.GameDefinition;
import onitama.model.GameState;
import onitama.model.SearchParameters;
import onitama.ui.gui.GuiUtils;

import static onitama.model.GameDefinition.NR_PLAYERS;
import static onitama.model.GameDefinition.CARDS_PER_PLAYER;

/**
 * Represents the raw configuration from the game configuration dialog, without references to GUI elements.
 * Can be used for persisting the configuration.
 * The reason that this class exists in addition to {@link GameConfig}, is that {@link GameConfig} only
 * contains the config needed for the game (e.g. not the contents of disabled text fields), whereas this
 * class contains every setting represented in the dialog.
 */
class DialogConfig {
    boolean[] isAI = new boolean[NR_PLAYERS];
    boolean[] timeBox = new boolean[NR_PLAYERS];
    boolean[] depthBox = new boolean[NR_PLAYERS];
    boolean[] ponderBox = new boolean[NR_PLAYERS];
    String[] timeField = new String[NR_PLAYERS];
    String[] depthField = new String[NR_PLAYERS];

    public int startingPlayer;

    boolean[] decks = new boolean[Deck.NR_DECKS];
    int[] playerByCard = new int[Card.NR_CARDS];

    static final DialogConfig DEFAULT_CONFIG = new DialogConfig();

    /** Creates an instance with default settings. */
    private DialogConfig() {
        isAI[0] = false;
        isAI[1] = true;

        for (int p = 0; p < 2; ++p) {
            timeBox[p] = true;
            timeField[p] = "1000";
            depthBox[p] = false;
            depthField[p] = "12";
            ponderBox[p] = false;
        }

        startingPlayer = 0;

        for (int d = 0; d < decks.length; ++d)
            decks[d] = d == 0; // select first deck (original cards)

        Arrays.fill(playerByCard, -1); // no card selection
    }

    /** Creates an instance from the settings in the supplied {@link GameConfigDialog}. No validation takes place at this point. */
    DialogConfig(GameConfigDialog dialog) {
        for (int p = 0; p < NR_PLAYERS; ++p) {
            isAI[p] = dialog.playerConfig[p].ai.isSelected();
            timeBox[p] = dialog.playerConfig[p].timeBox.isSelected();
            timeField[p] = dialog.playerConfig[p].timeField.getText();
            depthBox[p] = dialog.playerConfig[p].depthBox.isSelected();
            depthField[p] = dialog.playerConfig[p].depthField.getText();
            ponderBox[p] = dialog.playerConfig[p].ponderBox.isSelected();
        }

        startingPlayer = dialog.startingPlayer[0].isSelected() ? 0 : 1;

        for (int d = 0; d < decks.length; ++d)
            decks[d] = dialog.deckButton[d].isSelected();

        System.arraycopy(dialog.cardSelection.playerByCard, 0, playerByCard, 0, playerByCard.length);
    }

    /** Populates the supplied {@link GameConfigDialog} with the settings in this instance. */
    void populateDialog(GameConfigDialog dialog) {
        for (int p = 0; p < NR_PLAYERS; ++p) {
            dialog.playerConfig[p].ai.setSelected(isAI[p]);
            dialog.playerConfig[p].timeBox.setSelected(timeBox[p]);
            dialog.playerConfig[p].timeField.setText(timeField[p]);
            dialog.playerConfig[p].depthBox.setSelected(depthBox[p]);
            dialog.playerConfig[p].depthField.setText(depthField[p]);
            dialog.playerConfig[p].ponderBox.setSelected(ponderBox[p]);

            (isAI[p] ? dialog.playerConfig[p].ai : dialog.playerConfig[p].human).setSelected(true);
            dialog.playerConfig[p].disableAIConfig(!isAI[p]);

            dialog.startingPlayer[p].setSelected(startingPlayer == p);
        }

        for (int d = 0; d < decks.length; ++d) {
            dialog.deckButton[d].setSelected(decks[d]);
            dialog.cardSelection.disableDeck(Deck.values()[d], !decks[d]);
        }

        for (int card = 0; card < playerByCard.length; ++card)
            if (playerByCard[card] >= 0)
                dialog.cardSelection.select(playerByCard[card], card);
    }

    /** Parses and validates the settings in this instance, and returns a valid {@link GameConfig}. */
    GameConfig getGameConfig() {
        GameConfig gameConfig = new GameConfig();

        try {
            for (int p = 0; p < NR_PLAYERS; ++p) {
                if (gameConfig.isAI[p] = isAI[p]) {
                    gameConfig.searchParameters[p] = getSearchParameters(p);
                    gameConfig.ponder[p] = ponderBox[p];
                }
            }

            gameConfig.startingPlayer = startingPlayer;

            gameConfig.gameState = new GameState(getCardState());
        } catch (InvalidConfigException ice) {
            GuiUtils.errorAlert(ice.getMessage());
            return null;
        }

        return gameConfig;
    }

    private SearchParameters getSearchParameters(int player) throws InvalidConfigException {
        if (!timeBox[player] && !depthBox[player])
            throw new InvalidConfigException("Either search time or search depth must be selected");

        int maxSearchTimeMs = timeBox[player] ? validate(timeField[player], 0, Integer.MAX_VALUE, "Invalid search time") : Integer.MAX_VALUE;
        int maxDepth = depthBox[player] ? validate(depthField[player], 1, Searcher.MAX_NOMINAL_DEPTH, "Invalid search depth") : Searcher.MAX_NOMINAL_DEPTH;
        int ttSize = TranspositionTable.getSuggestedSize(maxDepth, maxSearchTimeMs);

        return new SearchParameters(ttSize, maxDepth, maxSearchTimeMs);
    }

    private int validate(String text, int min, int max, String failMsg) throws InvalidConfigException {
        Integer v = null;
        try {
            v = Integer.parseInt(text);
        } catch (NumberFormatException nfe) { }

        if (v == null || v < min || v > max)
            throw new InvalidConfigException(failMsg + ": " + text);

        return v;
    }

    private CardState getCardState() throws InvalidConfigException {
        Card[][] playerCards = new Card[NR_PLAYERS][CARDS_PER_PLAYER];
        int[] cardCount = { 0, 0 };
        Card nextCard = null;

        for (int c = 0; c < playerByCard.length; ++c) {
            int player = playerByCard[c];
            if (player == 2)
                nextCard = Card.CARDS[c];
            else if (player >= 0)
                playerCards[player][cardCount[player]++] = Card.CARDS[c];
        }

        // if no cards selected, return random cards
        if (nextCard == null && cardCount[0] == 0 && cardCount[1] == 0)
            return CardState.random(decks);

        for (int p = 0; p < NR_PLAYERS; ++p)
            if (cardCount[p] != 2)
                throw new InvalidConfigException("Select 2 cards for " + GameDefinition.PLAYER_COLOR[p].toLowerCase() + " player");

        if (nextCard == null)
            throw new InvalidConfigException("Select an extra card");

        return new CardState(playerCards, nextCard);
    }
}