package com.carrotsearch.hppc;

import org.junit.Assert;
import org.junit.Test;

public class NaNCornerCaseTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsDoubleKey()
    {
        DoubleObjectMap<String> map = new DoubleObjectHashMap<>();
        map.put(Double.NaN, "a");
        map.put(Double.NaN, "b");

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("b", map.get(Double.NaN));

        map.put(Double.longBitsToDouble(0xfff8000000000000L), "c");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("c", map.get(Double.NaN));
        Assert.assertEquals(
            (Double) map.keys().iterator().next().value, 
            (Double) Double.NaN);
        
        DoubleHashSet set = new DoubleHashSet();
        set.add(Double.NaN);
        set.add(Double.NaN);
        set.add(Double.longBitsToDouble(0xfff8000000000000L));
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(
            (Double) set.iterator().next().value, 
            (Double) Double.NaN);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsFloatKey()
    {
        FloatObjectMap<String> map = new FloatObjectHashMap<>();
        map.put(Float.NaN, "a");
        map.put(Float.NaN, "b");

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("b", map.get(Float.NaN));

        map.put(Float.intBitsToFloat(0xfff80000), "c");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("c", map.get(Float.NaN));
        Assert.assertEquals(
            (Float) map.keys().iterator().next().value, 
            (Float) Float.NaN);
        
        FloatHashSet set = new FloatHashSet();
        set.add(Float.NaN);
        set.add(Float.NaN);
        set.add(Float.intBitsToFloat(0xfff80000));
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(
            (Float) set.iterator().next().value, 
            (Float) Float.NaN);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsValue()
    {
      {
        IntDoubleMap m1 = new IntDoubleHashMap();
        m1.put(1, Double.NaN);
        IntDoubleMap m2 = new IntDoubleHashMap();
        m2.put(1, Double.NaN);
        Assert.assertEquals(m1, m2);
      }
      
      {
        IntFloatMap m1 = new IntFloatHashMap();
        m1.put(1, Float.NaN);
        IntFloatMap m2 = new IntFloatHashMap();
        m2.put(1, Float.NaN);
        Assert.assertEquals(m1, m2);
      }
      
      {
        FloatArrayList list = new FloatArrayList();
        Assert.assertFalse(list.contains(Float.NaN));
        list.add(0, Float.NaN, 1);
        Assert.assertTrue(list.contains(Float.NaN));
      }
      
      {
        DoubleArrayList list = new DoubleArrayList();
        Assert.assertFalse(list.contains(Double.NaN));
        list.add(0, Double.NaN, 1);
        Assert.assertTrue(list.contains(Double.NaN));
      }
      
      {
        
        DoubleArrayList l1 = new DoubleArrayList();
        l1.add(0, Double.NaN, 1);
        DoubleArrayList l2 = new DoubleArrayList();
        l2.add(0, Double.NaN, 1);
        Assert.assertEquals(l1, l2);
      }                  
    }    
}
