package com.carrotsearch.hppc.benchmarks.implementations;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import com.carrotsearch.hppc.benchmarks.IntSetOps;

public class FastutilIntSetOps implements IntSetOps {
  private final IntOpenHashSet delegate;

  public FastutilIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntOpenHashSet(expectedElements, (float) loadFactor);
  }

  @Override
  public void add(int key) {
    delegate.add(key);
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
