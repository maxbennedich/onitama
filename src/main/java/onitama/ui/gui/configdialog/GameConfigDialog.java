package onitama.ui.gui.configdialog;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import onitama.model.Card;
import onitama.model.Deck;
import onitama.model.GameDefinition;
import onitama.ui.gui.CenteredLabel;

/** Main class for the confiugration dialog which is used to configure new games. */
public class GameConfigDialog extends Dialog<GameConfig> {
    /** Config that will be persisted between invocations of the New Game dialog. */
    private static DialogConfig currentConfig = DialogConfig.DEFAULT_CONFIG;

    PlayerConfig[] playerConfig = { new PlayerConfig(0), new PlayerConfig(1) };

    ToggleButton[] startingPlayer = new ToggleButton[GameDefinition.NR_PLAYERS];

    ToggleButton[] deckButton = new ToggleButton[Deck.NR_DECKS];
    CardSelection cardSelection = new CardSelection();

    private static final String RULES =
            "Onitama is a two-player, perfect information abstract game with a random starting set-up. On a 5x5 board, both players start with five pawns on their side, with the main pawn in the middle.\n\n" +
            "Each player has two open cards that each display a possible move for any of his pieces. There is a fifth card that cannot be used by either player. On a player's turn, he chooses one of his cards, " +
            "moves one of his pieces according to the chosen card, then replaces the card he used with the fifth card. The other player then chooses one of his cards, moves accordingly, and exchanges that card " +
            "with this fifth card -- which is, of course, the card the first player just used.\n\n" +
            "Moving onto one of the opponent's pawns removes that pawn from the game. Taking the opponent's main pawn, or moving your main pawn into your opponent's main pawn's starting space, wins you the game.";

    public GameConfigDialog() {
        setTitle("New Game");

        ButtonType okButtonType = new ButtonType("Start game", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(okButtonType);

        ToggleGroup startingPlayerGroup = new ToggleGroup();
        for (int p = 0; p < GameDefinition.NR_PLAYERS; ++p) {
            startingPlayer[p] = new ToggleButton(GameDefinition.PLAYER_COLOR[p]);
            startingPlayer[p].setToggleGroup(startingPlayerGroup);
        }

        GridPane additionalPlayerConfig = new GridPane();
        additionalPlayerConfig.setHgap(10);
        additionalPlayerConfig.setVgap(5);
        additionalPlayerConfig.add(new Label("Starting player"), 0, 0);
        additionalPlayerConfig.add(new HBox(5, startingPlayer[0], startingPlayer[1]), 1, 0);

        VBox playerConfigBox = new VBox(30, playerConfig[0], playerConfig[1], additionalPlayerConfig);

        Label cardSelectionLabel = new CenteredLabel("Select 2 cards per player, and the extra card. For random cards, leave all unselected.");
        cardSelectionLabel.setMaxWidth(Double.MAX_VALUE);

        HBox deckRow = new HBox(20);
        deckRow.setAlignment(Pos.CENTER);
        for (Deck deck : Deck.values()) {
            int id = deck.ordinal();
            deckButton[id] = new ToggleButton(deck.name);
            deckButton[id].setOnAction(event -> clickDeckButton(deck));
            deckRow.getChildren().add(deckButton[id]);
        }

        VBox cardConfig = new VBox(10, cardSelectionLabel, deckRow);
        HBox cardRow = new HBox(15);

        for (int c = 0; c < Card.NR_CARDS; ++c) {
            ConfigCard cc = new ConfigCard(c, this, cardSelection);
            cardSelection.cards.add(cc);
            cc.paintCard(Card.CARDS[c], false, true);
            cardRow.getChildren().add(cc);

            if (((c+1) % 8) == 0 || c == Card.NR_CARDS - 1) {
                cardConfig.getChildren().add(cardRow);
                cardRow = new HBox(15);
            }
        }

        HBox config = new HBox(40, playerConfigBox, cardConfig);

        TextFlow rules = new TextFlow();
        Text rulesHeader = new Text("Rules");
        rulesHeader.setStyle("-fx-font-weight: bold");
        Text footer = new Text(" (from boardgamegeek.com)\n");
        footer.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, Font.getDefault().getSize()));
        rules.getChildren().addAll(rulesHeader, footer, new Text(RULES));
        rules.prefWidthProperty().bind(config.widthProperty());

        VBox configAndRules = new VBox(20, config, rules);

        if (currentConfig != null)
            currentConfig.populateDialog(this);

        getDialogPane().setContent(configAndRules);

        getDialogPane().lookupButton(okButtonType).addEventFilter(ActionEvent.ACTION, event -> {
            if (new DialogConfig(this).getGameConfig() == null)
                event.consume(); // prevent button from being clicked
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == null)
                return null;
            return (currentConfig = new DialogConfig(this)).getGameConfig();
        });
    }

    private void clickDeckButton(Deck deck) {
        boolean disable = !deckButton[deck.ordinal()].isSelected();

        if (disable) {
            // make sure we have at least 5 selectable cards, otherwise disallow disabling the deck
            int selectableCardCount = 0;
            for (int i = 0; i < Deck.NR_DECKS; ++i)
                if (deckButton[i].isSelected())
                    selectableCardCount += Deck.values()[i].cards.size();

            if (selectableCardCount < 5) {
                deckButton[deck.ordinal()].setSelected(true);
                return;
            }
        }

        cardSelection.disableDeck(deck, disable);
    }
}
