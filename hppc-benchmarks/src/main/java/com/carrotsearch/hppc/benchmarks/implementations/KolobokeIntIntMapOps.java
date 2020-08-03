package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.benchmarks.IntIntMapOps;
import net.openhft.koloboke.collect.hash.HashConfig;
import net.openhft.koloboke.collect.map.hash.HashIntIntMap;
import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;

public class KolobokeIntIntMapOps implements IntIntMapOps {
  private final HashIntIntMap delegate;

  public KolobokeIntIntMapOps(int expectedElements, double loadFactor) {
    this.delegate = HashIntIntMaps.getDefaultFactory()
            .withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor)).newMutableMap();
    this.delegate.ensureCapacity(expectedElements);
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
