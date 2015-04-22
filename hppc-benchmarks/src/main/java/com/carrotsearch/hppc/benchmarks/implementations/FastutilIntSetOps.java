package com.carrotsearch.hppc.benchmarks.implementations;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import com.carrotsearch.hppc.benchmarks.B002_HashSet_Add;
import com.carrotsearch.hppc.benchmarks.B003_HashSet_Contains;

public class FastutilIntSetOps implements B002_HashSet_Add.Ops, 
                                          B003_HashSet_Contains.Ops
{
  private final IntOpenHashSet delegate;

  public FastutilIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntOpenHashSet(expectedElements, (float) loadFactor); 
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
