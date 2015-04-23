package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntScatterSet;
import com.carrotsearch.hppc.benchmarks.*;

public class HppcIntScatterSetOps implements IntSetOps
{
  private final IntScatterSet delegate;

  public HppcIntScatterSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntScatterSet(expectedElements, loadFactor);
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
    return delegate.toArray();
  }
}
