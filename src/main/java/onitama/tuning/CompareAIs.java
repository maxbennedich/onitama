package onitama.tuning;

import java.util.Random;

import onitama.ai.evaluation.CenterPriorityEvaluator;
import onitama.ai.evaluation.PstEvaluator;
import onitama.model.SearchParameters;
import onitama.ui.console.Output;
import onitama.ui.console.Output.OutputLevel;

public class CompareAIs {
    static final int THREADS = 4;

    static final int SEARCH_DEPTH = 4;
    static final int GAMES = 1000;

    public static void main(String ... args) throws Exception {
        Output.outputLevel = OutputLevel.NONE;

        int randomSeed = 0;

        SearchParameters[] searchParameters = {
                new SearchParameters(10, SEARCH_DEPTH, 100000, state -> new PstEvaluator(state)),
                new SearchParameters(10, SEARCH_DEPTH, 100000, state -> new CenterPriorityEvaluator(state)),
            };

        MultiGameResult result = TuningUtils.testAIs(searchParameters, null, GAMES, THREADS, new Random(randomSeed), true);

        System.out.printf("Depth = %d -- Games = 2 * %d%n", SEARCH_DEPTH, GAMES);
        System.out.println(result);
    }
}
