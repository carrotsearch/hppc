package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.benchmarks.*;

public class HppcPhiMixIntSetOps implements IntSetOps
{
  private final IntOpenHashSet delegate;

  public HppcPhiMixIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = new IntOpenHashSet(expectedElements, loadFactor) {
      @Override
      protected int hashKey(int key) { // PhiMix.
        final int h = key * 0x9E3779B9;
        return h ^ (h >>> 16);
      }
    };
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
