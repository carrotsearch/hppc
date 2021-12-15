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

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.benchmarks.IntIntMapOps;

public class HppcIntIntMapOps implements IntIntMapOps {
  private final IntIntHashMap delegate;

  public HppcIntIntMapOps(int expectedElements, double loadFactor) {
    this.delegate = new IntIntHashMap(expectedElements, loadFactor);
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
