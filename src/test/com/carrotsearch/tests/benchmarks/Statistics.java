package com.carrotsearch.tests.benchmarks;

import java.util.List;

/**
 * Statistics from multiple {@link Result}s.
 */
class Statistics
{
    public Average gc;
    public Average evaluation;

    public static Statistics from(List<Result> results)
    {
        final Statistics stats = new Statistics();
        long [] times = new long [results.size()];

        // GC-times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).gcTime();
        stats.gc = Average.from(times);

        // Evaluation-only times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).evaluationTime();
        stats.evaluation = Average.from(times);

        return stats;
    }
}