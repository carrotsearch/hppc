package com.carrotsearch.hppc;

import org.junit.Test;

public class HashCollisionsCornerCaseTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSets()
    {
        IntOpenHashSet a = new IntOpenHashSet();
        for (int i = 10000000; i-- != 0;) {
            a.add(i);
        }
        IntOpenHashSet b2 = new IntOpenHashSet();
        b2.addAll(a);
    }
    
    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMaps()
    {
        IntIntOpenHashMap a = new IntIntOpenHashMap();
        for (int i = 10000000; i-- != 0;) {
            a.put(i, 0);
        }
        IntIntOpenHashMap b2 = new IntIntOpenHashMap();
        b2.putAll(a);
    }
}
