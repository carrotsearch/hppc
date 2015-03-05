package com.carrotsearch.hppc;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdentityMapsTest
{
    @Test
    public void testSanity()
    {
        ObjectCharOpenIdentityHashMap<Integer> m1 = ObjectCharOpenIdentityHashMap.newInstance(); 

        Integer a, b;
        m1.put(a = new Integer(1), 'a');
        m1.put(b = new Integer(1), 'b');

        assertEquals('a', m1.get(a));
        assertEquals('b', m1.get(b));
        assertEquals(2, m1.size());
        
        ObjectCharOpenIdentityHashMap<Integer> m2 = ObjectCharOpenIdentityHashMap.newInstance();
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
        ObjectObjectOpenIdentityHashMap<String, String> m1 = ObjectObjectOpenIdentityHashMap.newInstance();
        ObjectObjectOpenIdentityHashMap<String, String> m2 = ObjectObjectOpenIdentityHashMap.newInstance();

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
        ObjectDoubleOpenIdentityHashMap<String> m1 = ObjectDoubleOpenIdentityHashMap.newInstance();
        ObjectDoubleOpenIdentityHashMap<String> m2 = ObjectDoubleOpenIdentityHashMap.newInstance();

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
