package com.carrotsearch.hppc;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.tests.benchmarks.AbstractBenchmark;

/**
 * Simple benchmarks against <code>java.util.BitSet</code>.
 */
public class BitSetBenchmark extends AbstractBenchmark
{
    private static BitSet hppc;
    private static java.util.BitSet jre;

    /** Pseudo-random with initial seed (repeatability). */
    private static Random rnd = new Random(0x11223344);

    /* */
    @BeforeClass
    public static void before()
    {
        final int bits = 1024 * 1024 * 128;

        hppc = new BitSet(bits);
        jre = new java.util.BitSet(bits);

        // Randomly fill every 17-th bit to speed up the test.
        for (int i = 0; i < bits; i += 17)
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
    public void testIteratorHPPC() throws Exception
    {
        for (int i = hppc.nextSetBit(0); i >= 0; i = hppc.nextSetBit(i + 1))
        {
            // do nothing.
        }
    }

    @Test
    public void testIteratorJRE() throws Exception
    {
        for (int i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            // do nothing.
        }
    }
}
