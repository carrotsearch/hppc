package com.carrotsearch.hppc;

final class HashContainers {
  /**
   * Maximum capacity for an array that is of power-of-two size and still
   * allocable in Java (not a negative int).  
   */
  final static int MAX_ARRAY_SIZE = 0x80000000 >>> 1;

  /**
   * Minimum buffer size to allocate.
   */
  final static int MIN_ARRAY_SIZE = 2;

  /**
   * By default a hash container will store this many elements 
   * without resizing.
   */
  final static int DEFAULT_CAPACITY = 4;
  
  /**
   * Default load factor.
   */
  final static float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * Minimal sane load factor (99 empty slots per 100).
   */
  final static float MIN_LOAD_FACTOR = 1 / 100.0f;

  /**
   * Maximum sane load factor (1 empty slot per 100).
   */
  final static float MAX_LOAD_FACTOR = 99 / 100.0f;

  /**
   * Compute and return the maximum capacity (maximum number of elements)
   * that can be stored in a hash container for a given load factor.
   */
  static int maxCapacity(double loadFactor) {
    checkLoadFactor(loadFactor);
    return expandAtCount(MAX_ARRAY_SIZE, loadFactor) - 1;
  }

  /** */
  static int minBufferSize(int elements, double loadFactor) {
    checkLoadFactor(loadFactor);

    if (elements < 0) { 
      throw new IllegalArgumentException(
          "Number of elements must be >= 0: " + elements);
    }

    final long requiredSize = Math.max(MIN_ARRAY_SIZE, BitUtil.nextHighestPowerOfTwo((long) Math.ceil(elements / loadFactor)));
    if (requiredSize > MAX_ARRAY_SIZE) {
      throw new BufferAllocationException(
          "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
          elements,
          loadFactor);
    }

    return (int) requiredSize;
  }

  /** */
  static int nextBufferSize(int arraySize, int elements, double loadFactor) {
    checkValidArraySize(arraySize);
    if (arraySize == MAX_ARRAY_SIZE) {
      throw new BufferAllocationException(
          "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
          elements,
          loadFactor);
    }

    return (int) arraySize << 1;
  }

  /** */
  static int expandAtCount(int arraySize, double loadFactor) {
    checkValidArraySize(arraySize);
    checkLoadFactor(loadFactor);
    return (int) Math.ceil(arraySize * loadFactor);
  }

  private static void checkValidArraySize(int arraySize) {
    // These are internals, we can just assert without retrying.
    assert arraySize > 1;
    assert BitUtil.nextHighestPowerOfTwo(arraySize) == arraySize;
  }

  private static void checkLoadFactor(double loadFactor) {
    if (loadFactor < MIN_LOAD_FACTOR || loadFactor > MAX_LOAD_FACTOR) {
      throw new BufferAllocationException(
          "The load factor should be in range [%.2f, %.2f]: %f",
          MIN_LOAD_FACTOR,
          MAX_LOAD_FACTOR,
          loadFactor);
    }    
  }
}
