package onitama.ui.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Helper class with various GUI utilities. */
public class GuiUtils {
    public static <T extends Region> T setSize(T region, int s) {
        return setSize(region, s, s);
    }

    public static <T extends Region> T setSize(T region, int w, int h) {
        setWidth(region, w);
        setHeight(region, h);
        return region;
    }

    public static <T extends Region> T setWidth(T region, int w) {
        region.setMinWidth(w);
        region.setMaxWidth(w);
        return region;
    }

    public static <T extends Region> T setHeight(T region, int h) {
        region.setMinHeight(h);
        region.setMaxHeight(h);
        return region;
    }

    public static <T extends Region> T setColor(T region, String color) {
        region.setStyle("-fx-background-color: #" + color + ";");
        return region;
    }

    public static Pane createSquare(int size) {
        return GuiUtils.setColor(GuiUtils.setSize(new StackPane(), size), "fff");
    }

    public static void errorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Image loadImageOrDie(String file) {
        try {
            return new Image(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
