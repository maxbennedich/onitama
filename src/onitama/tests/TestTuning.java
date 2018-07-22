package onitama.tests;

import static onitama.model.GameDefinition.N;
import static onitama.model.GameDefinition.NN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import onitama.ai.evaluation.CenterPriorityEvaluator;
import onitama.ai.evaluation.FixedSquareScoreEvaluator;
import onitama.ai.evaluation.PstEvaluator;
import onitama.model.SearchParameters;
import onitama.tuning.MultiGameResult;
import onitama.tuning.TuningUtils;
import onitama.ui.console.Output;
import onitama.ui.console.Output.OutputLevel;

public class TestTuning {
    @Test
    public void testMultiGameResult() {
        MultiGameResult results = new MultiGameResult();
        results.wins[0] = 7;
        results.wins[1] = 4;
        results.draws = 9;
        assertEquals(11.5, results.points(0));
        assertEquals(8.5, results.points(1));
        assertEquals(11.5 / 20, results.winRate(0));
        assertEquals(8.5 / 20, results.winRate(1));
        assertEquals(52.5, results.eloDifference(0), 0.1);
        assertEquals(-52.5, results.eloDifference(1), 0.1);
    }

    @Test
    public void testAIVsAI() {
        Output.outputLevel = OutputLevel.NONE;

        double[] staticAITable = new double[NN];
        double[] badAITable = new double[NN];

        Arrays.fill(staticAITable, 8);
        Arrays.fill(badAITable, 8);

        // make the PST bad
        for (int y = 1; y < 4; ++y)
            for (int x = 1; x < 4; ++x)
                badAITable[y*N+x] = -50;

        SearchParameters[] sp = {
            new SearchParameters(14, 2, 10000, state -> new FixedSquareScoreEvaluator(state, staticAITable)),
            new SearchParameters(14, 2, 10000, state -> new FixedSquareScoreEvaluator(state, badAITable)),
        };

        MultiGameResult result = TuningUtils.testAIs(sp, null, 50, 1, new Random(0), false);

        System.out.println(result);

        assertTrue(result.eloDifference(0) > 100);
    }

    @Test
    public void testMultipleThreads() {
        Output.outputLevel = OutputLevel.NONE;

        for (int k = 0; k < 10; ++k) {
            SearchParameters[] searchParameters = {
                    new SearchParameters(10, 2, 100000, state -> new PstEvaluator(state)),
                    new SearchParameters(10, 2, 100000, state -> new CenterPriorityEvaluator(state)),
                };

            TuningUtils.testAIs(searchParameters, null, 100, 12, new Random(), false);
        }
    }
}
