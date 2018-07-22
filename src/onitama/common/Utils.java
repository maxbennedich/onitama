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

    public static String centerString(String s, int size) {
        return centerString(s, size, ' ');
    }

    public static String centerString(String s, int size, char pad) {
        if (s == null || size <= s.length())
            return s;

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }

    public static void sleepAndLogException(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static void joinAndLogException(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
