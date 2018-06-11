package onitama.common;

import java.util.List;

import onitama.ai.pondering.PonderSearchStats;

public interface ILogger {
    void logSearch(String text);

    void logPonder(List<PonderSearchStats> threadStats);

    void logMove(String move);
}
