package com.carrotsearch.hppc;

import org.junit.Test;
import static org.junit.Assert.*;

public class XorShiftRandomTest
{
    /** */
    @Test
    public void testApproxEqualBucketFill()
    {
        int [] buckets = new int [(1 << 8)];
        int mask = buckets.length - 1;

        int hits = 1000000;
        XorShiftRandom rnd = new XorShiftRandom(0xdeadbeef);
        for (int count = hits * buckets.length; --count >= 0;)
        {
            buckets[rnd.nextInt() & mask]++;
        }

        // every bucket should get +- 1% * hits
        int limit = hits / 100;
        for (int bucketCount : buckets)
            assertTrue(
                Math.abs(bucketCount - hits) + " > " + limit + "?", 
                Math.abs(bucketCount - hits) <= limit);
    }
    
    @Test
    public void testNext() {
        XorShiftRandom random = new XorShiftRandom();
        for (int bits = 1; bits <= 32; bits++) {
            final long max = (1L << bits) - 1;
            long mask = 0;
            for (int i = 0; i < 10000; i++) {
                final long val = ((long) random.next(bits)) & 0xffffffffL;
                mask |= val;
                assertTrue(val + " >= " + max + "?", val <= max);
            }
            assertEquals(max, mask);
        }
    }    
}
