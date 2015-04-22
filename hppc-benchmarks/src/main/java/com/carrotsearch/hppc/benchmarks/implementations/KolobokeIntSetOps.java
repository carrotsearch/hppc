package com.carrotsearch.hppc.benchmarks.implementations;

import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.set.hash.HashIntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;

import com.carrotsearch.hppc.benchmarks.IntSetOps;

public class KolobokeIntSetOps implements IntSetOps {
  private final HashIntSet delegate;

  public KolobokeIntSetOps(int expectedElements, double loadFactor) {
    this.delegate = HashIntSets.getDefaultFactory()
        .withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor)).newMutableSet();
    this.delegate.ensureCapacity(expectedElements);
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
