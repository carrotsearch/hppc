package com.carrotsearch.tests.benchmarks;

import java.lang.annotation.*;

/**
 * Benchmark options applicable to methods annotated as tests.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BenchmarkOptions
{
    /**
     * @return Call {@link System#gc()} before each test. This may slow down the tests
     *         in a significant way, so disabling it is sensible in most cases.
     */
    boolean callgc() default false;

    /**
     * Sets the number of warmup rounds for the test. If negative, the default is taken.
     */
    int warmupRounds() default -1;

    /**
     * Sets the number of benchmark rounds for the test. If negative, the default is taken.
     */
    int benchmarkRounds() default -1;
}