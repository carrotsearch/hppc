package com.carrotsearch.hppc;

import java.util.Random;

import org.junit.*;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.*;


/**
 * Repeated cardinality calculation, very sparse data sets.
 */
@BenchmarkHistoryChart(filePrefix="CLASSNAME.history", maxRuns=50)
@BenchmarkMethodChart(filePrefix="CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 2, benchmarkRounds = 10)
public class CardinalityBenchmark extends AbstractBenchmark
{
    private static final int WND = 100;
    private static int [] numbers = new int [50000];

    private static BitSet bitset = new BitSet();
    private static IntDoubleLinkedSet dlinked = new IntDoubleLinkedSet();
    private static IntOpenHashSet hset = new IntOpenHashSet();

    @SuppressWarnings("unused")
    private static volatile int guard;

    /* */
    @BeforeClass
    public static void prepare()
    {
        final int MAX_RANGE = 0xfffff;
        final Random rnd = new Random(0x11223344);
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = Math.abs(rnd.nextInt()) & MAX_RANGE;
    }
    
    @AfterClass
    public static void cleanup()
    {
        numbers = null;
        dlinked = null;
        bitset = null;
        hset = null;
    }

    @Before
    public void roundCleanup()
    {
        dlinked.clear();
        bitset.clear();
        hset.clear();
    }

    /**
     * Simple cardinality calculations, double-linked set (very sparse data).
     */
    @Test
    public void testCardinality_dlinked() 
    {
        int card = 0;
        for (int i = 0; i < numbers.length - WND; i++)
        {
            dlinked.clear();
            for (int j = 0; j < WND; j++)
            {
                dlinked.add(numbers[i + j]);
            }
            card += dlinked.size();
        }
        guard = card;
    }

    /**
     * Simple cardinality calculations, hash set (very sparse data).
     */
    @Test
    public void testCardinality_hset() 
    {
        int card = 0;
        for (int i = 0; i < numbers.length - WND; i++)
        {
            hset.clear();
            for (int j = 0; j < WND; j++)
            {
                hset.add(numbers[i + j]);
            }
            card += hset.size();
        }
        guard = card;
    }    
    
    /**
     * Simple cardinality calculations, bitset (very sparse data).
     */
    @Test
    @BenchmarkOptions(callgc = false, warmupRounds = 1, benchmarkRounds = 2)
    public void testCardinality_bset() 
    {
        int card = 0;
        for (int i = 0; i < numbers.length - WND; i++)
        {
            bitset.clear();
            for (int j = 0; j < WND; j++)
            {
                bitset.set(numbers[i + j]);
            }
            card += bitset.cardinality();
        }
        guard = card;
    }    
}
