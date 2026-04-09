/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NaNCornerCaseTest {
  /**
   * @see "https://github.com/carrotsearch/hppc/issues/136"
   */
  @Test
  public void testNaNAsValue() {
    {
      IntDoubleMap m1 = new IntDoubleHashMap();
      m1.put(1, Double.NaN);
      IntDoubleMap m2 = new IntDoubleHashMap();
      m2.put(1, Double.NaN);
      assertEquals(m1, m2);
    }

    {
      IntFloatMap m1 = new IntFloatHashMap();
      m1.put(1, Float.NaN);
      IntFloatMap m2 = new IntFloatHashMap();
      m2.put(1, Float.NaN);
      assertEquals(m1, m2);
    }

    {
      FloatArrayList list = new FloatArrayList();
      assertFalse(list.contains(Float.NaN));
      list.add(0, Float.NaN, 1);
      assertTrue(list.contains(Float.NaN));
    }

    {
      DoubleArrayList list = new DoubleArrayList();
      assertFalse(list.contains(Double.NaN));
      list.add(0, Double.NaN, 1);
      assertTrue(list.contains(Double.NaN));
    }

    {
      DoubleArrayList l1 = new DoubleArrayList();
      l1.add(0, Double.NaN, 1);
      DoubleArrayList l2 = new DoubleArrayList();
      l2.add(0, Double.NaN, 1);
      assertEquals(l1, l2);
    }
  }
}
