package com.carrotsearch.hppc;

public final class TightRandomResizingStrategy implements ArraySizingStrategy {
  public int growCalls;

  @Override
  public int round(int expectedElements) {
    return expectedElements;
  }

  @Override
  public int grow(int currentBufferLength, int elementsCount, int expectedAdditions) {
    growCalls++;
    return Math.max(currentBufferLength, elementsCount + expectedAdditions) + KTypeArrayDequeTest.randomIntBetween(0, 10);
  }
}