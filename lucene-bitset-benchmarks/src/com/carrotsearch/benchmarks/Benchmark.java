
package com.carrotsearch.benchmarks;

import java.util.Arrays;
import java.util.Random;

import org.junit.BeforeClass;

import com.carrotsearch.tests.benchmarks.AbstractBenchmark;
import com.carrotsearch.tests.benchmarks.BenchmarkOptions;

/**
 * Microbenchmarks various implementations of ntz/pop in BitUtils.
 */
@BenchmarkOptions(warmupRounds = 5, benchmarkRounds = 15, callgc = false)
public abstract class Benchmark extends AbstractBenchmark
{
    /** Memory chunk for testing. */
    protected static int TEST_MEMORY = (/* MB */ 200 * 1024 * 1024) / 8;

    /** Repeat each test look this many times. */
    protected static int LOOPS = 6;

    /* */
    protected static long[] memblock1 = new long[TEST_MEMORY];
    
    /* */
    protected static long[] memblock2 = new long[TEST_MEMORY];

    /* */
    public static volatile long guard;

    /* */
    @BeforeClass
    public static void init()
    {
        // Initialize with random data.
        final Random rnd = new Random(0x11223344);
        for (int i = TEST_MEMORY; --i >= 0;)
        {
            memblock1[i] = rnd.nextLong();
            memblock2[i] = rnd.nextLong();
        }

        System.out.print("# ");
        for (String key : Arrays.asList(
            "java.version",
            "java.vm.name",
            "java.vm.version",
            "java.vm.vendor"
        ))
        {
            System.out.print(System.getProperty(key) + ", ");
        }
        System.out.println();
    }

    public static void main(String [] args) throws Throwable
    {
        org.junit.runner.JUnitCore.runClasses(
            Benchmark_BitUtil_trunk.class,
            Benchmark_BitUtil_pop3264.class,
            Benchmark_BitUtil_popNtzJRE.class,
            Benchmark_BitUtil_popNtzJRE_simple.class
            );
    }
}
