/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.benchmarks.IntIntMapOps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class FastutilIntIntMapOps implements IntIntMapOps {
  private final Int2IntOpenHashMap delegate;

  public FastutilIntIntMapOps(int expectedElements, double loadFactor) {
    this.delegate = new Int2IntOpenHashMap(expectedElements, (float) loadFactor);
  }

  @Override
  public void put(int key, int value) {
    delegate.put(key, value);
  }

  @Override
  public int get(int key) {
    return delegate.get(key);
  }
}
