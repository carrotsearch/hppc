package com.carrotsearch.hppc;

final class HashContainers {
  /**
   * Maximum array size for hash containers (power-of-two and still
   * allocable in Java, not a negative int).  
   */
  final static int MAX_HASH_ARRAY_LENGTH = 0x80000000 >>> 1;

  /**
   * Minimum buffer size to allocate.
   */
  final static int MIN_ARRAY_SIZE = 2;

  /**
   * By default a hash container will store this many elements 
   * without resizing.
   */
  final static int DEFAULT_EXPECTED_ELEMENTS = 4;
  
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
    checkLoadFactor(loadFactor, 0, 1);
    return expandAtCount(MAX_HASH_ARRAY_LENGTH, loadFactor) - 1;
  }

  /** */
  static int minBufferSize(int elements, double loadFactor) {
    if (elements < 0) { 
      throw new IllegalArgumentException(
          "Number of elements must be >= 0: " + elements);
    }

    long length = (long) Math.ceil(elements / loadFactor);
    if (length == elements) {
      length++;
    }
    length = Math.max(MIN_ARRAY_SIZE, BitUtil.nextHighestPowerOfTwo(length));

    if (length > MAX_HASH_ARRAY_LENGTH) {
      throw new BufferAllocationException(
          "Maximum array size exceeded for this load factor (elements: %d, load factor: %f)",
          elements,
          loadFactor);
    }

    return (int) length;
  }

  /** */
  static int nextBufferSize(int arraySize, int elements, double loadFactor) {
    checkValidArraySize(arraySize);
    if (arraySize == MAX_HASH_ARRAY_LENGTH) {
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
    // Take care of hash container invariant (there has to be at least one empty slot to ensure
    // the lookup loop finds either the element or an empty slot).
    return Math.min(arraySize - 1, (int) Math.ceil(arraySize * loadFactor));
  }

  private static void checkValidArraySize(int arraySize) {
    // These are internals, we can just assert without retrying.
    assert arraySize > 1;
    assert BitUtil.nextHighestPowerOfTwo(arraySize) == arraySize;
  }

  static void checkLoadFactor(double loadFactor, double minAllowedInclusive, double maxAllowedInclusive) {
    if (loadFactor < minAllowedInclusive || loadFactor > maxAllowedInclusive) {
      throw new BufferAllocationException(
          "The load factor should be in range [%.2f, %.2f]: %f",
          minAllowedInclusive,
          maxAllowedInclusive,
          loadFactor);
    }    
  }
}
