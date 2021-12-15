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

public interface IntSetOps {
  void add(int key);

  boolean contains(int key);

  void bulkAdd(int[] keys);

  int bulkContains(int[] keys);

  int[] iterationOrderArray();
}
