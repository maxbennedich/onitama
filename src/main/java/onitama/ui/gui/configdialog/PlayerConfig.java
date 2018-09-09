package onitama.ui.gui.configdialog;

import java.util.Arrays;
import java.util.List;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import onitama.ai.Searcher;
import onitama.model.GameDefinition;
import onitama.ui.gui.GuiUtils;

/** Configuration for an individual player. Two instances of this class will be created. */
class PlayerConfig extends GridPane {
    ToggleButton human = new ToggleButton("Human");
    ToggleButton ai = new ToggleButton("AI");

    CheckBox timeBox = new CheckBox("Max search time (ms)");
    TextField timeField = GuiUtils.setWidth(new TextField(), 60);
    CheckBox depthBox = new CheckBox("Max search depth (1 - " + Searcher.MAX_NOMINAL_DEPTH + ")");
    TextField depthField = GuiUtils.setWidth(new TextField(), 35);
    CheckBox ponderBox = new CheckBox("Ponder (search while opponent plays)");

    List<Region> playerOptions = Arrays.asList(timeBox, timeField, depthBox, depthField, ponderBox);

    PlayerConfig(int player) {
        setHgap(10);
        setVgap(5);

        add(new Label(GameDefinition.PLAYER_COLOR[player] + " player:"), 0, 0);

        ToggleGroup group = new ToggleGroup();
        human.setToggleGroup(group);
        ai.setToggleGroup(group);

        timeBox.selectedProperty().addListener((observable, oldValue, newValue) -> timeField.setDisable(!newValue));
        depthBox.selectedProperty().addListener((observable, oldValue, newValue) -> depthField.setDisable(!newValue));

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                oldValue.setSelected(true); // disallow deselecting current option
            else
                disableAIConfig(newValue != ai);
        });
        add(new HBox(5, human, ai), 0, 1);

        add(timeBox, 0, 2);
        add(timeField, 1, 2);

        add(depthBox, 0, 3);
        add(depthField, 1, 3);

        add(ponderBox, 0, 4);
    }

    void disableAIConfig(boolean disabled) {
        if (disabled) {
            playerOptions.forEach(option -> option.setDisable(true));
        } else {
            timeBox.setDisable(false);
            timeField.setDisable(!timeBox.isSelected());
            depthBox.setDisable(false);
            depthField.setDisable(!depthBox.isSelected());
            ponderBox.setDisable(false);
        }
    }
}