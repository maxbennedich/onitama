package onitama.ui.gui;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

/** A non-editable, fixed-width font, {@link TextArea}. */
public class LogArea extends TextArea {
    public LogArea() {
        setEditable(false);
        setFont(Font.font("Monospaced", 11));
    }
}