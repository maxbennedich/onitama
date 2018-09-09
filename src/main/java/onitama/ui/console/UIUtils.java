package onitama.ui.console;

import java.util.List;

import onitama.ai.pondering.PonderSearchStats;
import onitama.common.ILogger;

public class UIUtils {
    public static final ILogger CONSOLE_LOGGER = new ILogger() {
        @Override public void logSearch(String text) {
            Output.println(text);
        }

        @Override public void logPonder(List<PonderSearchStats> threadStats) {
            threadStats.forEach(stats -> Output.println(stats.stats));
            Output.println("------------------------");
        }

        @Override public void logMove(String move) {
            Output.println("\n" + move + "\n");
        }
    };
}
