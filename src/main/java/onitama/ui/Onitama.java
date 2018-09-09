package onitama.ui;

import onitama.ui.console.ConsoleGame;
import onitama.ui.gui.Gui;

public class Onitama {
    public static void main(String ... args) {
        if (args.length > 0 && "console".equals(args[0]))
            ConsoleGame.launch();
        else
            Gui.launch(Gui.class, args);
    }
}