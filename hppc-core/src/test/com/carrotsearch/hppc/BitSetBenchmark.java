package com.carrotsearch.hppc;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkMethodChart;

/**
 * Simple benchmarks against <code>java.util.BitSet</code>.
 */
@BenchmarkHistoryChart(filePrefix="CLASSNAME.history", maxRuns=50)
@BenchmarkMethodChart(filePrefix="CLASSNAME.methods")
public class BitSetBenchmark extends AbstractBenchmark
{
    private static BitSet hppc;
    private static java.util.BitSet jre;

    /** Pseudo-random with initial seed (repeatability). */
    private static Random rnd = new Random(0x11223344);

    /** Escape analysis guard. */
    @SuppressWarnings("unused")
    private static volatile int guard;

    /* */
    @BeforeClass
    public static void before()
    {
        final int MB = 1024 * 1024;
        final int bits = 128 * MB * 4;

        hppc = new BitSet(bits);
        jre = new java.util.BitSet(bits);

        // Randomly fill every bits (this is fairly dense distribution).
        for (int i = 0; i < bits; i += 1 + rnd.nextInt(10))
        {
            if (rnd.nextBoolean())
            {
                hppc.set(i);
                jre.set(i);
            }
        }
    }

    @Test
    public void testCardinalityHPPC() throws Exception
    {
        hppc.cardinality();
    }

    @Test
    public void testCardinalityJRE() throws Exception
    {
        jre.cardinality();
    }

    @Test
    public void testBitSetIteratorHPPC() throws Exception
    {
        final BitSetIterator bi = hppc.iterator();
        int sum = 0;
        for (int i = bi.nextSetBit(); i >= 0; i = bi.nextSetBit())
        {
            sum += i;
        }
        guard = sum;
    }

    @Test
    public void testIntCursorIteratorHPPC() throws Exception
    {
        int sum = 0;
        for (IntCursor c : hppc.asIntLookupContainer())
        {
            sum += c.value;
        }
        guard = sum;
    }

    @Test
    public void testIteratorHPPC() throws Exception
    {
        int sum = 0;
        for (int i = hppc.nextSetBit(0); i >= 0; i = hppc.nextSetBit(i + 1))
        {
            sum += i;
        }
        guard = sum;
    }

    @Test
    public void testIteratorJRE() throws Exception
    {
        int sum = 0;
        for (int i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            sum += i;
        }
        guard = sum;
    }
}
