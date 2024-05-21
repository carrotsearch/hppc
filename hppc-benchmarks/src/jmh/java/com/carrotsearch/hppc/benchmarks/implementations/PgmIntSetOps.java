/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks.implementations;

import com.carrotsearch.hppc.IntPgmIndex;
import com.carrotsearch.hppc.benchmarks.IntSetOps;
import java.util.Arrays;

public class PgmIntSetOps implements IntSetOps {
  private IntPgmIndex.IntBuilder builder;
  private int[] keys;
  private IntPgmIndex delegate;

  public PgmIntSetOps(int epsilon, int recursiveEpsilon) {
    builder =
        new IntPgmIndex.IntBuilder().setEpsilon(epsilon).setEpsilonRecursive(recursiveEpsilon);
  }

  @Override
  public void add(int key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(int key) {
    return delegate.contains(key);
  }

  @Override
  public void bulkAdd(int[] keys) {
    if (this.keys != null) {
      throw new UnsupportedOperationException("bulkAdd() can be called only once");
    }
    this.keys = keys;
    Arrays.sort(keys);
    delegate = builder.setSortedKeys(keys, keys.length).build();
    builder = null;
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
    return keys;
  }
}
