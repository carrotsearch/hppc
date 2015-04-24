package com.carrotsearch.hppc;

import static org.junit.Assert.*;

import org.junit.Test;

public class ObjectIntHashMapRegressionTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-32" */
    @Test
    public void testEqualsOnObjectKeys()
    {
        ObjectIntHashMap<String> map = new ObjectIntHashMap<String>();
        String key1 = "key1";
        String key2 = new String("key1");

        map.put(key1, 1);
        assertEquals(1, map.get(key2));
        assertEquals(1, map.put(key2, 2));
        assertEquals(1, map.size());
    }
}
