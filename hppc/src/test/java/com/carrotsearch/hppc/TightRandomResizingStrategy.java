package com.carrotsearch.hppc;


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
    return Math.max(currentBufferLength, elementsCount + expectedAdditions) 
        + KTypeArrayDequeTest.randomIntBetween(0, maxRandomIncrement);
  }
}