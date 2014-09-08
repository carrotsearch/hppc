package com.carrotsearch.hppc;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.carrotsearch.hppc.predicates.IntPredicate;
import com.carrotsearch.hppc.predicates.LongPredicate;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Regression tests against <code>java.util.BitSet</code>.
 */
public class BitSetTest extends RandomizedTest
{
    private BitSet hppc;
    private java.util.BitSet jre;

    /* */
    @Before
    public void before()
    {
        hppc = new BitSet();
        jre = new java.util.BitSet();
    }

    /**
     * Test to string conversion.
     */
    @Test
    public void testToString()
    {
        assertEquals(jre.toString(), hppc.toString());

        for (int i : new int [] {1, 10, 20, 5000}) {
            hppc.set(i);
            jre.set(i);
        }
        assertEquals(jre.toString(), hppc.toString());
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
                int index = randomInt(bitSpace);
                jre.set(index);
                hppc.set(index);

                assertEquals(jre.length(), hppc.length());
            }
            
            assertSame(jre, hppc);
            assertIntLookupContainer(jre, hppc.asIntLookupContainer());
            assertLongLookupContainer(jre, hppc.asLongLookupContainer());
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
    private void assertSame(final java.util.BitSet jre, BitSet hppc)
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
    
    private void assertIntLookupContainer(final java.util.BitSet jre, IntLookupContainer ilc)
    {
        int i, j;

        // Check adapter to IntLookupContainer
        assertEquals(ilc.size(), jre.cardinality());

        i = jre.nextSetBit(0);
        Iterator<IntCursor> ilcCursor = ilc.iterator();
        while (i >= 0) 
        {
            assertTrue(ilcCursor.hasNext());
            IntCursor c = ilcCursor.next();
            assertEquals(i, c.index);
            assertEquals(i, c.value);

            i = jre.nextSetBit(i + 1);
        }
        assertEquals(-1, i);
        assertFalse(ilcCursor.hasNext());
        try
        {
            ilcCursor.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
            // expected.
        }
        
        // Check toArray()
        int [] setIndexes = ilc.toArray();
        int [] expected = new int [jre.cardinality()];
        for (j = 0, i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            expected[j++] = i;
        }
        assertArrayEquals(expected, setIndexes);

        // Test for-each predicates.
        ilc.forEach(new IntPredicate()
        {
            int i = jre.nextSetBit(0);
            public boolean apply(int setBit)
            {
                assertEquals(i, setBit);
                i = jre.nextSetBit(i + 1);
                return true;
            }
        });

        // Test contains.
        for (i = 0; i < jre.size() + 65; i++)
        {
            assertEquals(hppc.get(i), ilc.contains(i));
        }

        // IntLookupContainer must not throw exceptions on negative arguments. 
        ilc.contains(-1);
    }

    private void assertLongLookupContainer(final java.util.BitSet jre, LongLookupContainer llc)
    {
        int i, j;

        // Check adapter to IntLookupContainer
        assertEquals(llc.size(), jre.cardinality());

        i = jre.nextSetBit(0);
        Iterator<LongCursor> llcCursor = llc.iterator();
        while (i >= 0) 
        {
            assertTrue(llcCursor.hasNext());
            LongCursor c = llcCursor.next();
            assertEquals(i, c.index);
            assertEquals(i, c.value);

            i = jre.nextSetBit(i + 1);
        }
        assertEquals(-1, i);
        assertFalse(llcCursor.hasNext());
        try
        {
            llcCursor.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
            // expected.
        }
        
        // Check toArray()
        long [] setIndexes = llc.toArray();
        long [] expected = new long [jre.cardinality()];
        for (j = 0, i = jre.nextSetBit(0); i >= 0; i = jre.nextSetBit(i + 1))
        {
            expected[j++] = i;
        }
        assertArrayEquals(expected, setIndexes);

        // Test for-each predicates.
        llc.forEach(new LongPredicate()
        {
            int i = jre.nextSetBit(0);

            public boolean apply(long setBit)
            {
                assertEquals(i, setBit);
                i = jre.nextSetBit(i + 1);
                return true;
            }
        });

        // Test contains.
        for (i = 0; i < jre.size() + 65; i++)
        {
            assertEquals(hppc.get(i), llc.contains(i));
        }

        // IntLookupContainer must not throw exceptions on negative arguments. 
        llc.contains(-1);
    }
}
