package com.carrotsearch.tests.benchmarks;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Benchmark evaluator statement.
 */
final class BenchmarkStatement extends Statement
{
    public final static String WARMUP_ROUNDS_PROPERTY = "rounds.warmup";
    public final static String BENCHMARK_ROUNDS_PROPERTY = "rounds.benchmark";

    public static final String IGNORE_ANNOTATION_OPTIONS_PROPERTY = "ignore.annotation.options";
    public static final String IGNORE_CALLGC_PROPERTY = "ignore.callgc";

    /**
     * How many warmup runs should we execute for each test method?
     */
    final static int DEFAULT_WARMUP_ROUNDS = 5;

    /**
     * How many actual benchmark runs should we execute for each test method?
     */
    final static int DEFAULT_BENCHMARK_ROUNDS = 10;

    /**
     * If <code>true</code>, the local overrides using {@link BenchmarkOptions}
     * are ignored and defaults (or globals passed via system properties) are used.
     */
    private boolean ignoreAnnotationOptions = Boolean.getBoolean(IGNORE_ANNOTATION_OPTIONS_PROPERTY);

    /**
     * Disable all forced garbage collector calls.
     */
    private boolean ignoreCallGC = Boolean.getBoolean(IGNORE_CALLGC_PROPERTY);

    private final Object target;
    private final FrameworkMethod method;
    private final Statement base;
    private final BenchmarkOptions options;

    @BenchmarkOptions
    @SuppressWarnings("unused")
    private void defaultOptions()
    {
    }

    /* */
    public BenchmarkStatement(Statement base, FrameworkMethod method, Object object)
    {
        this.base = base;
        this.method = method;
        this.target = object;

        this.options = resolveOptions(method, object);
    }
    
    private BenchmarkOptions resolveOptions(FrameworkMethod method, Object object)
    {
        // Method-level override.
        BenchmarkOptions options = method.getAnnotation(BenchmarkOptions.class);
        if (options != null)
            return options;

        // Class-level override. Look for annotations in this and superclasses.
        Class<?> clz = object.getClass();
        while (clz != null)
        {
            options = clz.getAnnotation(BenchmarkOptions.class);
            if (options != null)
                return options;
            
            clz = clz.getSuperclass();
        }

        // Defaults.
        try
        {
            return getClass().getDeclaredMethod("defaultOptions").getAnnotation(
                BenchmarkOptions.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void evaluate() throws Throwable
    {
        final int warmupRounds = 
            getIntOption(options.warmupRounds(), 
                WARMUP_ROUNDS_PROPERTY, DEFAULT_WARMUP_ROUNDS);

        final int benchmarkRounds = 
            getIntOption(options.benchmarkRounds(), 
                BENCHMARK_ROUNDS_PROPERTY, DEFAULT_BENCHMARK_ROUNDS);

        final int totalRounds = warmupRounds + benchmarkRounds;
            
        final ArrayList<Result> results = new ArrayList<Result>(totalRounds);

        GCSnapshot gcSnapshot = null;
        long warmupTime = System.currentTimeMillis();
        long benchmarkTime = 0;
        for (int i = 0; i < totalRounds; i++)
        {
            // We assume no reordering will take place here.
            final long startTime = System.currentTimeMillis();
            cleanupMemory();
            final long afterGC = System.currentTimeMillis();

            if (i == warmupRounds)
            {
                gcSnapshot = new GCSnapshot();
                benchmarkTime = System.currentTimeMillis();
                warmupTime = benchmarkTime - warmupTime;
            }

            base.evaluate();
            final long endTime = System.currentTimeMillis();

            results.add(new Result(startTime, afterGC, endTime));
        }
        benchmarkTime = System.currentTimeMillis() - benchmarkTime;

        final Statistics stats = Statistics.from(results.subList(warmupRounds, totalRounds));
        System.out.println(String.format(
                    Locale.ENGLISH,
                    "%-50s: %d/%d rounds, time.total: %.2f, time.warmup: %.2f, time.bench: %.2f, round: %s, round.gc: %s, GC.calls: %d, GC.time: %.2f",
                    target.getClass().getSimpleName() + "." + method.getName(),
                    benchmarkRounds, 
                    totalRounds,
                    (warmupTime + benchmarkTime) * 0.001, 
                    warmupTime * 0.001,
                    benchmarkTime * 0.001, 
                    stats.evaluation.toString(0.001), 
                    stats.gc.toString(0.001), 
                    gcSnapshot.accumulatedInvocations(), 
                    gcSnapshot.accumulatedTime() / 1000.0));
    }

    /**
     * Best effort attempt to clean up the memory if {@link BenchmarkOptions#callgc()} is
     * enabled.
     */
    private void cleanupMemory()
    {
        if (ignoreCallGC)
            return;

        if (!options.callgc())
            return;

        // Best-effort GC invocation. I really don't know of any other
        // way to ensure a GC pass.
        System.gc();
        System.gc();
        Thread.yield();
    }
    
    /**
     * Get an integer from system properties.
     */
    private int getIntOption(int localValue, String property, int defaultValue)
    {
        final String v = System.getProperty(property);
        if (v != null && v.trim().length() > 0)
        {
            defaultValue = Integer.parseInt(v);
        }

        if (ignoreAnnotationOptions || localValue < 0)
        {
            return defaultValue;
        }

        return localValue;
    }
}