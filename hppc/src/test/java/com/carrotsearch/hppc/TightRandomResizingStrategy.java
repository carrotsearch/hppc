/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import java.util.Random;

public final class TightRandomResizingStrategy implements ArraySizingStrategy {
  private final int maxRandomIncrement;
  private final Random rnd;
  public int growCalls;

  public TightRandomResizingStrategy(Random rnd, int maxRandomIncrement) {
    this.maxRandomIncrement = maxRandomIncrement;
    this.rnd = rnd;
  }

  public TightRandomResizingStrategy(Random rnd) {
    this(rnd, 10);
  }

  @Override
  public int grow(int currentBufferLength, int elementsCount, int expectedAdditions) {
    growCalls++;

    int r = 0;
    if (maxRandomIncrement > 0) {
      r += rnd.nextInt(maxRandomIncrement);
    }

    return Math.max(currentBufferLength, elementsCount + expectedAdditions) + r;
  }

  @Override
  public long ramBytesAllocated() {
    // int: maxRandomIncrement, growCalls
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES * 2;
  }

  @Override
  public long ramBytesUsed() {
    return ramBytesAllocated();
  }
}
