/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import com.carrotsearch.randomizedtesting.RandomizedContext;

public final class TightRandomResizingStrategy implements ArraySizingStrategy {
  private final int maxRandomIncrement;
  public int growCalls;

  public TightRandomResizingStrategy(int maxRandomIncrement) {
    this.maxRandomIncrement = maxRandomIncrement;
  }

  public TightRandomResizingStrategy() {
    this(10);
  }

  @Override
  public int grow(int currentBufferLength, int elementsCount, int expectedAdditions) {
    growCalls++;

    int r = 0;
    if (maxRandomIncrement > 0) {
      r += RandomizedContext.current().getRandom().nextInt(maxRandomIncrement);
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
