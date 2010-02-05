package com.carrotsearch.hppc;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Regression tests against <code>java.util.BitSet</code>.
 */
public class BitSetTest
{
    private BitSet hppc;
    private java.util.BitSet jre;
    
    /** Pseudo-random with initial seed (repeatability). */
    private Random rnd = new Random(0x11223344);

    /* */
    @Before
    public void before()
    {
        hppc = new BitSet();
        jre = new java.util.BitSet();
    }

    /**
     * Test random insertions into the bitset.
     */
    @Test
    public void testAgainstJREBitSet() throws Exception
    {
        final int rounds = 100;
        final int bits = 1000;
        final int bitSpace = bits * 10;

        for (int i = 0; i < rounds; i++)
        {
            for (int bit = 0; bit < bits; bit++)
            {
                int index = rnd.nextInt(bitSpace);
                jre.set(index);
                hppc.set(index);

                assertEquals(jre.length(), hppc.length());
            }
            
            assertSame(jre, hppc);
        }
    }
    
    /** */
    @Test
    public void testHashCodeEquals()
    {
        BitSet bs1 = new BitSet(200);
        BitSet bs2 = new BitSet(64);
        bs1.set(3);
        bs2.set(3);
        
        assertEquals(bs1, bs2);
        assertEquals(bs1.hashCode(), bs2.hashCode());
    }

    /**
     * Assert that the two bitsets are identical. 
     */
    private void assertSame(java.util.BitSet jre, BitSet hppc)
    {
        // Cardinality and emptiness status.
        assertEquals(jre.cardinality(), hppc.cardinality());
        assertEquals(jre.isEmpty(), hppc.isEmpty());

        // Check bit-by-bit.
        for (int i = 0; i < jre.size() - 1; i++)
            assertEquals(jre.get(i), hppc.get(i));

        // Check iterator indices.
        int i = jre.nextSetBit(0);
        int j = hppc.nextSetBit(0);
        BitSetIterator k = hppc.iterator();
        while (i >= 0) 
        {
            assertEquals(i, j);
            assertEquals(i, k.nextSetBit());

            i = jre.nextSetBit(i + 1);
            j = hppc.nextSetBit(j + 1);
        }

        assertEquals(-1, k.nextSetBit());
        assertEquals(-1, j);
    }
}
