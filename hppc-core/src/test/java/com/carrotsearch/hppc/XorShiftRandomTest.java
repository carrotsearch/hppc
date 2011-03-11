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
            assertTrue(Math.abs(bucketCount - hits) <= limit);
    }
}
