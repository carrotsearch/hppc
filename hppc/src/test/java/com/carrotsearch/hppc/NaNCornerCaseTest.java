package com.carrotsearch.hppc;

import org.junit.Assert;
import org.junit.Test;

public class NaNCornerCaseTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-93" */
    @Test
    public void testNaNAsDoubleKey()
    {
        DoubleObjectMap<String> map = DoubleObjectOpenHashMap.newInstance();
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
        
        DoubleOpenHashSet set = DoubleOpenHashSet.newInstance();
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
        FloatObjectMap<String> map = FloatObjectOpenHashMap.newInstance();
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
        
        FloatOpenHashSet set = FloatOpenHashSet.newInstance();
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
        IntDoubleMap m1 = IntDoubleOpenHashMap.newInstance();
        m1.put(1, Double.NaN);
        IntDoubleMap m2 = IntDoubleOpenHashMap.newInstance();
        m2.put(1, Double.NaN);
        Assert.assertEquals(m1, m2);
      }
      
      {
        IntFloatMap m1 = IntFloatOpenHashMap.newInstance();
        m1.put(1, Float.NaN);
        IntFloatMap m2 = IntFloatOpenHashMap.newInstance();
        m2.put(1, Float.NaN);
        Assert.assertEquals(m1, m2);
      }
      
      {
        FloatArrayList list = FloatArrayList.newInstance();
        Assert.assertFalse(list.contains(Float.NaN));
        list.add(0, Float.NaN, 1);
        Assert.assertTrue(list.contains(Float.NaN));
      }
      
      {
        DoubleArrayList list = DoubleArrayList.newInstance();
        Assert.assertFalse(list.contains(Double.NaN));
        list.add(0, Double.NaN, 1);
        Assert.assertTrue(list.contains(Double.NaN));
      }
      
      {
        
        DoubleArrayList l1 = DoubleArrayList.newInstance();
        l1.add(0, Double.NaN, 1);
        DoubleArrayList l2 = DoubleArrayList.newInstance();
        l2.add(0, Double.NaN, 1);
        Assert.assertEquals(l1, l2);
      }                  
    }    
}
