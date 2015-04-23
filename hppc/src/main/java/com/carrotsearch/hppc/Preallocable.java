package com.carrotsearch.hppc;

/**
 * Anything that can preallocate buffers given prior knowledge of the number of
 * stored elements.
 */
public interface Preallocable {
  /**
   * Ensure this container can hold at least the given number of elements
   * without resizing its buffers.
   * 
   * @param expectedElements
   *          The total number of elements, inclusive.
   */
  public void ensureCapacity(int expectedElements);
}
