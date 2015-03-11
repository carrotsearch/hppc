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
  public int round(int expectedElements) {
    return expectedElements;
  }

  @Override
  public int grow(int currentBufferLength, int elementsCount, int expectedAdditions) {
    growCalls++;
    // System.out.println(String.format(Locale.ROOT, "%d %d %d", currentBufferLength, elementsCount, expectedAdditions));
    int r = 0;
    if (maxRandomIncrement > 0) {
      r += RandomizedContext.current().getRandom().nextInt(maxRandomIncrement);
    }

    return Math.max(currentBufferLength, elementsCount + expectedAdditions) + r;
  }
}