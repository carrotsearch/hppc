package com.carrotsearch.hppc;

/** */
public class PowerOfTwoSizingStrategy implements ArraySizingStrategy {
  @Override
  public int round(int capacity) {
    return BitUtil.nextHighestPowerOfTwo(requestedCapacity);
  }

  @Override
  public int grow(int currentBufferLength, int elementsCount, int expectedAdditions) {
    return 0;
  }

  /**
   * Returns the next highest power of two, or the current value if it's already
   * a power of two or zero.
   */
  private static int nextHighestPowerOfTwo(int v) {
    v--;
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    v++;
    return v;
  }
}
