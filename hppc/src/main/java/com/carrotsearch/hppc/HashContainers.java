package com.carrotsearch.hppc;

public final class HashContainers {
  /**
   * Maximum array size for hash containers (power-of-two and still
   * allocable in Java, not a negative int).  
   */
  public final static int MAX_HASH_ARRAY_LENGTH = 0x80000000 >>> 1;

  /**
   * Minimum hash buffer size.
   */
  public final static int MIN_HASH_ARRAY_LENGTH = 4;

  /**
   * Default load factor.
   */
  public final static float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * Minimal sane load factor (99 empty slots per 100).
   */
  public final static float MIN_LOAD_FACTOR = 1 / 100.0f;

  /**
   * Maximum sane load factor (1 empty slot per 100).
   */
  public final static float MAX_LOAD_FACTOR = 99 / 100.0f;

  /**
   * Compute and return the maximum number of elements (inclusive)
   * that can be stored in a hash container for a given load factor.
   */
  public static int maxElements(double loadFactor) {
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
    length = Math.max(MIN_HASH_ARRAY_LENGTH, BitUtil.nextHighestPowerOfTwo(length));

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
    assert checkPowerOfTwo(arraySize);
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
    assert checkPowerOfTwo(arraySize);
    // Take care of hash container invariant (there has to be at least one empty slot to ensure
    // the lookup loop finds either the element or an empty slot).
    return Math.min(arraySize - 1, (int) Math.ceil(arraySize * loadFactor));
  }

  /** */
  static void checkLoadFactor(double loadFactor, double minAllowedInclusive, double maxAllowedInclusive) {
    if (loadFactor < minAllowedInclusive || loadFactor > maxAllowedInclusive) {
      throw new BufferAllocationException(
          "The load factor should be in range [%.2f, %.2f]: %f",
          minAllowedInclusive,
          maxAllowedInclusive,
          loadFactor);
    }    
  }
  
  /** */
  static boolean checkPowerOfTwo(int arraySize) {
    // These are internals, we can just assert without retrying.
    assert arraySize > 1;
    assert BitUtil.nextHighestPowerOfTwo(arraySize) == arraySize;
    return true;
  }  
}
