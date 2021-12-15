/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import org.junit.Assert;
import org.junit.Test;

public class NaNCornerCaseTest {
  /** @see "http://issues.carrot2.org/browse/HPPC-93" */
  @Test
  public void testNaNAsValue() {
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
