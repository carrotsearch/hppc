package com.carrotsearch.tests.benchmarks;

import org.junit.Rule;
import org.junit.rules.MethodRule;


/**
 * A superclass for tests that should be executed as benchmarks (several rounds, GC and
 * time accounting). Provides JUnit rules that run the tests repeatedly, logging the
 * intermediate results (memory usage, times).
 */
public class AbstractBenchmark
{
    /**
     * Enables the benchmark rule. Add {@link BenchmarkOptions} to your test
     * class or methods to override the defaults.
     */
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();
}
