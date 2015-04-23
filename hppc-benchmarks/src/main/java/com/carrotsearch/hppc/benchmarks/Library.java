package com.carrotsearch.hppc.benchmarks;

import com.carrotsearch.hppc.benchmarks.implementations.FastutilIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcIntScatterSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.KolobokeIntSetOps;

/**
 * Benchmarked libraries.
 */
public enum Library {
  HPPC {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new HppcIntSetOps(expectedElements, loadFactor);
    }
  },

  HPPC_SCATTER {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new HppcIntScatterSetOps(expectedElements, loadFactor);
    }    
  },

  FASTUTIL {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new FastutilIntSetOps(expectedElements, loadFactor);
    }
  },

  KOLOBOKE {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new KolobokeIntSetOps(expectedElements, loadFactor);
    }
  };
  
  public abstract IntSetOps newIntSet(int expectedElements, double loadFactor);
}