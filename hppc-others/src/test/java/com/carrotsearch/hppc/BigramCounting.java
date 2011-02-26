package com.carrotsearch.hppc;


import java.io.File;

import org.junit.*;
import org.junit.rules.MethodRule;


import com.carrotsearch.junitbenchmarks.*;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.h2.*;

@BenchmarkHistoryChart(filePrefix = "CLASSNAME-history", maxRuns = 10)
@BenchmarkMethodChart(filePrefix = "CLASSNAME-methods")
@BenchmarkOptions(warmupRounds = 2, benchmarkRounds = 5)
public class BigramCounting extends BigramCountingBase
{
    private static H2Consumer consumer = new H2Consumer(new File(".mapadditions"));

    @Rule
    public MethodRule runBenchmarks = new BenchmarkRule(consumer, new WriterConsumer());

    @AfterClass
    public static void cleanup()
    {
        consumer.close();
    }
}
