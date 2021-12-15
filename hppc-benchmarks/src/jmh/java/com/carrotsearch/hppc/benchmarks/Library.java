/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks;

import com.carrotsearch.hppc.benchmarks.implementations.*;

/** Benchmarked libraries. */
public enum Library {
  HPPC {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new HppcIntSetOps(expectedElements, loadFactor);
    }

    @Override
    public IntIntMapOps newIntIntMap(int expectedElements, double loadFactor) {
      return new HppcIntIntMapOps(expectedElements, loadFactor);
    }
  },

  WORM {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new WormIntSetOps(expectedElements);
    }

    @Override
    public IntIntMapOps newIntIntMap(int expectedElements, double loadFactor) {
      return new WormIntIntMapOps(expectedElements);
    }
  },

  FASTUTIL {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new FastutilIntSetOps(expectedElements, loadFactor);
    }

    @Override
    public IntIntMapOps newIntIntMap(int expectedElements, double loadFactor) {
      return new FastutilIntIntMapOps(expectedElements, loadFactor);
    }
  },

  KOLOBOKE {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new KolobokeIntSetOps(expectedElements, loadFactor);
    }

    @Override
    public IntIntMapOps newIntIntMap(int expectedElements, double loadFactor) {
      return new KolobokeIntIntMapOps(expectedElements, loadFactor);
    }
  },
  ;

  public abstract IntSetOps newIntSet(int expectedElements, double loadFactor);

  public abstract IntIntMapOps newIntIntMap(int expectedElements, double loadFactor);
}
