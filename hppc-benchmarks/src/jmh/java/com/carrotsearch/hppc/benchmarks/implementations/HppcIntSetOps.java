/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.benchmarks.IntSetOps;

public class HppcIntSetOps implements IntSetOps {
  private final IntHashSet delegate;

  public HppcIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntHashSet(expectedElements, loadFactor);
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
