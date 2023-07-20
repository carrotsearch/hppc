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

import java.util.Arrays;

/**
 * Basic growable int array helper for HPPC templates (so before {@code IntArrayList} is generated).
 */
public class IntGrowableArray implements Accountable {

  public int[] buffer;
  public int size;

  public IntGrowableArray(int initialCapacity) {
    buffer = new int[initialCapacity];
  }

  public void add(int e) {
    ensureBufferSpace(1);
    buffer[size++] = e;
  }

  public int[] toArray() {
    return buffer.length == size ? buffer : Arrays.copyOf(buffer, size);
  }

  private void ensureBufferSpace(int expectedAdditions) {
    if (size + expectedAdditions > buffer.length) {
      int newSize =
          BoundedProportionalArraySizingStrategy.DEFAULT_INSTANCE.grow(
              buffer.length, size, expectedAdditions);
      buffer = Arrays.copyOf(buffer, newSize);
    }
  }

  @Override
  public long ramBytesAllocated() {
    // int: size
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + Integer.BYTES
        + RamUsageEstimator.shallowSizeOfArray(buffer);
  }

  @Override
  public long ramBytesUsed() {
    // int: size
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + Integer.BYTES
        + RamUsageEstimator.shallowUsedSizeOfArray(buffer, size);
  }
}
