/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.benchmarks.IntIntMapOps;
import com.koloboke.collect.hash.HashConfig;
import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;

public class KolobokeIntIntMapOps implements IntIntMapOps {
  private final HashIntIntMap delegate;

  public KolobokeIntIntMapOps(int expectedElements, double loadFactor) {
    this.delegate =
        HashIntIntMaps.getDefaultFactory()
            .withHashConfig(HashConfig.fromLoads(loadFactor / 2, loadFactor, loadFactor))
            .newMutableMap();
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
