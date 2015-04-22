package com.carrotsearch.hppc.benchmarks.implementations;

import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.set.hash.HashIntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;

import com.carrotsearch.hppc.benchmarks.B002_HashSet_Add;
import com.carrotsearch.hppc.benchmarks.B003_HashSet_Contains;

public class KolobokeIntSetOps implements B002_HashSet_Add.Ops,
                                          B003_HashSet_Contains.Ops
{
  private final HashIntSet delegate;

  public KolobokeIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = 
        HashIntSets.getDefaultFactory()
          .withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor))
          .newMutableSet();
    this.delegate.ensureCapacity(expectedElements);
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
