package onitama.model;

import java.util.function.Function;

import onitama.ai.SearchState;
import onitama.ai.Searcher;
import onitama.ai.evaluation.Evaluator;
import onitama.ai.evaluation.PstEvaluator;

public class SearchParameters {
    public final int ttBits;
    public final int maxDepth;
    public final int maxSearchTimeMs;
    public final Function<SearchState, Evaluator> evaluator;

    public static final SearchParameters DUMMY_SEARCHER = new SearchParameters(1, Searcher.MAX_NOMINAL_DEPTH, 0);

    public SearchParameters(int ttBits, int maxDepth, int maxSearchTimeMs) {
        this(ttBits, maxDepth, maxSearchTimeMs, state -> new PstEvaluator(state));
    }

    public SearchParameters(int ttBits, int maxDepth, int maxSearchTimeMs, Function<SearchState, Evaluator> evaluator) {
        this.ttBits = ttBits;
        this.maxDepth = maxDepth;
        this.maxSearchTimeMs = maxSearchTimeMs;
        this.evaluator = evaluator;
    }

    @Override public String toString() {
        return String.format("TT = %d bits, Depth = %d, Time = %d ms", ttBits, maxDepth, maxSearchTimeMs);
    }
}
