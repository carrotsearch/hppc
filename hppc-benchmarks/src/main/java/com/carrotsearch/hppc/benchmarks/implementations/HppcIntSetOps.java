package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.benchmarks.*;

public class HppcIntSetOps implements B002_HashSet_Add.Ops,
                                      B003_HashSet_Contains.Ops
{
  private final IntOpenHashSet delegate;

  public HppcIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntOpenHashSet(expectedElements, loadFactor);
  }

  @Override
  public Object addAll(int[] keys) {
    for (int key : keys) {
      delegate.add(key);
    }
    return delegate;
  }
  
  @Override
  public int contains(int[] keys) {
    int v = 0;
    for (int key : keys) {
      if (delegate.contains(key)) {
        v++;
      }
    }
    return v;
  }
}
