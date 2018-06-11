package onitama.ui.gui.configdialog;

@SuppressWarnings("serial")
class InvalidConfigException extends Exception {
    InvalidConfigException(String message) {
        super(message);
    }
}