package onitama.ui;

public class UIUtils {

    static void silentSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
