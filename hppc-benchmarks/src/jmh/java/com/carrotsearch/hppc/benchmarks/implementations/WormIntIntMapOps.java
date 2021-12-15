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

import com.carrotsearch.hppc.IntIntWormMap;
import com.carrotsearch.hppc.benchmarks.IntIntMapOps;

public class WormIntIntMapOps implements IntIntMapOps {
  private final IntIntWormMap delegate;

  public WormIntIntMapOps(int expectedSize) {
    this.delegate = new IntIntWormMap(expectedSize);
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
