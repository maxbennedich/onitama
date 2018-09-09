package onitama.ui.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class CenteredLabel extends Label {
    public CenteredLabel(String text) {
        super(text);
        setAlignment(Pos.CENTER);
        setTextAlignment(TextAlignment.CENTER); // needed to center multi-line text within the label
    }

    public CenteredLabel(String text, double fontSize) {
        this(text);
        setFont(Font.font(fontSize));
    }
}