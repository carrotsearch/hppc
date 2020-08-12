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
