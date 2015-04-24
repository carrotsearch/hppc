package com.carrotsearch.hppc;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdentityMapsTest
{
    @Test
    public void testSanity()
    {
        ObjectCharIdentityHashMap<Integer> m1 = new ObjectCharIdentityHashMap<>(); 

        Integer a, b;
        m1.put(a = new Integer(1), 'a');
        m1.put(b = new Integer(1), 'b');

        assertEquals('a', m1.get(a));
        assertEquals('b', m1.get(b));
        assertEquals(2, m1.size());
        
        ObjectCharIdentityHashMap<Integer> m2 = new ObjectCharIdentityHashMap<>();
        m2.put(b, 'b');
        m2.put(a, 'a');
        
        assertEquals(m1, m2);
        assertEquals(m2, m1);
        
        m2.remove(a);
        m2.put(new Integer(1), 'a');
        assertNotEquals(m1,  m2);
        assertNotEquals(m2,  m1);
    }

    @Test
    public void testEqualsComparesValuesByReference()
    {
        ObjectObjectIdentityHashMap<String, String> m1 = new ObjectObjectIdentityHashMap<>();
        ObjectObjectIdentityHashMap<String, String> m2 = new ObjectObjectIdentityHashMap<>();

        String a = "a";
        String av = "av";
        String b = "b";
        String bv = "bv";

        m1.put(a, av);
        m1.put(b, bv);

        m2.put(b, bv);
        m2.put(a, av);

        assertEquals(m1, m2);
        assertEquals(m2, m1);
        
        m2.put(a, new String(av));
        assertNotEquals(m1,  m2);
        assertNotEquals(m2,  m1);
    }

    @Test
    public void testNaNsInValues()
    {
        ObjectDoubleIdentityHashMap<String> m1 = new ObjectDoubleIdentityHashMap<>();
        ObjectDoubleIdentityHashMap<String> m2 = new ObjectDoubleIdentityHashMap<>();

        String a = "a";
        Double av = Double.NaN;

        m1.put(a, av);
        m2.put(a, av);

        assertEquals(m1, m2);
        assertEquals(m2, m1);
        
        // value storage is an array of primitives, so NaNs should be equal, even though the object
        // was different.
        m2.put(a, new Double(Double.NaN));
        assertEquals(m1,  m2);
        assertEquals(m2,  m1);

        DoubleContainer values = m1.values();
        assertTrue(values.contains(Double.NaN));
    }   
}
