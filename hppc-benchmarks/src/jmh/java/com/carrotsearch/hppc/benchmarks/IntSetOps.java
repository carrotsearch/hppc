/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
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
