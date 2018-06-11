package onitama.common;

import java.util.List;

import onitama.ai.pondering.PonderSearchStats;

public class Utils {
    public static final ILogger NO_LOGGER = new ILogger() {
        @Override public void logSearch(String text) { }

        @Override public void logPonder(List<PonderSearchStats> threadStats) { }

        @Override public void logMove(String move) { }
    };

    public static String formatNumber(long n) {
        if (n < 1000) return n+"";
        if (n < 10000) return String.format("%.2f K", n/1000.0);
        if (n < 100000) return String.format("%.1f K", n/1000.0);
        if (n < 1000000) return String.format("%.0f K", n/1000.0);
        if (n < 10000000) return String.format("%.2f M", n/1000000.0);
        if (n < 100000000) return String.format("%.1f M", n/1000000.0);
        if (n < 1000000000) return String.format("%.0f M", n/1000000.0);
        if (n < 10000000000L) return String.format("%.2f G", n/1000000000.0);
        if (n < 100000000000L) return String.format("%.1f G", n/1000000000.0);
        return String.format("%.0f G", n/1000000000.0);
    }

    public static void silentSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
