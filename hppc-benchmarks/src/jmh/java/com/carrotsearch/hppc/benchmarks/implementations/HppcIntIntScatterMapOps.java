/*
 * HPPC
 *
 * Copyright (C) 2010-2020 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntIntScatterMap;
import com.carrotsearch.hppc.benchmarks.IntIntMapOps;

public class HppcIntIntScatterMapOps implements IntIntMapOps {
  private final IntIntScatterMap delegate;

  public HppcIntIntScatterMapOps(int expectedElements, double loadFactor) {
    this.delegate = new IntIntScatterMap(expectedElements, loadFactor);
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
