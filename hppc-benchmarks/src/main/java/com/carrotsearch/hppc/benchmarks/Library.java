package com.carrotsearch.hppc.benchmarks;

import com.carrotsearch.hppc.benchmarks.implementations.FastutilIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcPhiMixIntSetOps;
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

  HPPC_PHIMIX {
    @Override
    public IntSetOps newIntSet(int expectedElements, double loadFactor) {
      return new HppcPhiMixIntSetOps(expectedElements, loadFactor);
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