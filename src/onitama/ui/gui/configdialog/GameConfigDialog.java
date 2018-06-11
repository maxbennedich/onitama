package onitama.ui.gui.configdialog;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import onitama.model.Card;
import onitama.ui.gui.CenteredLabel;

/** Main class for the confiugration dialog which is used to configure new games. */
public class GameConfigDialog extends Dialog<GameConfig> {
    /** Config that will be persisted between invocations of the New Game dialog. */
    private static DialogConfig currentConfig = DialogConfig.DEFAULT_CONFIG;

    PlayerConfig[] playerConfig = { new PlayerConfig(0), new PlayerConfig(1) };
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

        VBox playerConfigBox = new VBox(30, playerConfig[0], playerConfig[1]);

        Label cardSelectionLabel = new CenteredLabel("Select 2 cards per player, and the extra card.\nFor random cards, leave all unselected.");
        cardSelectionLabel.setMaxWidth(Double.MAX_VALUE);

        VBox cardConfig = new VBox(10, cardSelectionLabel);
        HBox cardRow = new HBox(20);

        for (int c = 0; c < Card.NR_CARDS; ++c) {
            ConfigCard cc = new ConfigCard(c, this, cardSelection);
            cardSelection.cards.add(cc);
            cc.paintCard(Card.CARDS[c], false);
            cardRow.getChildren().add(cc);

            if (((c+1) % 4) == 0 || c == Card.NR_CARDS - 1) {
                cardConfig.getChildren().add(cardRow);
                cardRow = new HBox(20);
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
}
