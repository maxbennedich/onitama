package onitama.ui.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/** Game status message displayed in the GUI, such as "Red player to move". */
class StatusMessage {
    final Label label;
    private String message;
    private Timeline temporaryMessageTimeline = new Timeline();

    StatusMessage() {
        label = new CenteredLabel("", 24);
        label.setMaxWidth(Double.MAX_VALUE);
    }

    void setMessage(String message) {
        temporaryMessageTimeline.stop();
        label.setText(this.message = message);
    }

    void setMessage(String temporaryMessage, Duration duration) {
        temporaryMessageTimeline.stop();
        label.setText(temporaryMessage);

        temporaryMessageTimeline = new Timeline(new KeyFrame(duration, ae -> setMessage(message)));
        temporaryMessageTimeline.play();
    }
}