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

import com.carrotsearch.hppc.IntWormSet;
import com.carrotsearch.hppc.benchmarks.IntSetOps;

public class WormIntSetOps implements IntSetOps {
  private final IntWormSet delegate;

  public WormIntSetOps(int expectedElements) {
    this.delegate = new IntWormSet(expectedElements);
  }

  @Override
  public void add(int key) {
    delegate.add(key);
  }

  @Override
  public boolean contains(int key) {
    return delegate.contains(key);
  }

  @Override
  public void bulkAdd(int[] keys) {
    for (int key : keys) {
      delegate.add(key);
    }
  }

  @Override
  public int bulkContains(int[] keys) {
    int v = 0;
    for (int key : keys) {
      if (delegate.contains(key)) {
        v++;
      }
    }
    return v;
  }

  @Override
  public int[] iterationOrderArray() {
    return delegate.toArray();
  }
}
