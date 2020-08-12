package com.carrotsearch.hppc.benchmarks.implementations;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import com.carrotsearch.hppc.benchmarks.IntIntMapOps;

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
