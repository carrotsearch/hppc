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

import com.carrotsearch.hppc.benchmarks.IntSetOps;
import com.koloboke.collect.hash.HashConfig;
import com.koloboke.collect.set.hash.HashIntSet;
import com.koloboke.collect.set.hash.HashIntSets;

public class KolobokeIntSetOps implements IntSetOps {
  private final HashIntSet delegate;

  public KolobokeIntSetOps(int expectedElements, double loadFactor) {
    this.delegate =
        HashIntSets.getDefaultFactory()
            .withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor))
            .newMutableSet();
    this.delegate.ensureCapacity(expectedElements);
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
    return delegate.toIntArray();
  }
}
