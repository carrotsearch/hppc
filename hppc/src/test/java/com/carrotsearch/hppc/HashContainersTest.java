package com.carrotsearch.hppc;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

import static com.carrotsearch.hppc.HashContainers.*;

public class HashContainersTest extends RandomizedTest {
  /* */
  @Test
  public void testCapacityCalculations() {
    assertEquals(MIN_HASH_ARRAY_LENGTH, minBufferSize(0, 0.5f));
    assertEquals(MIN_HASH_ARRAY_LENGTH, minBufferSize(1, 0.5f));

    assertEquals(0x20, minBufferSize(0x10, 0.5f));
    assertEquals(0x40, minBufferSize(0x10, 0.49f));

    int maxCapacity = maxElements(HashContainers.MAX_LOAD_FACTOR);
    assertEquals(0x40000000, minBufferSize(maxCapacity, MAX_LOAD_FACTOR));

    // This would fill the array fully, validating the invariant, but should
    // be possible without reallocating the buffer.
    minBufferSize(maxCapacity + 1, MAX_LOAD_FACTOR);
    assertEquals(maxCapacity + 1, expandAtCount(MAX_HASH_ARRAY_LENGTH, MAX_LOAD_FACTOR));

    try {
      // This should be impossible because it'd create a negative-sized array.
      minBufferSize(maxCapacity + 2, MAX_LOAD_FACTOR);
      fail();
    } catch (BufferAllocationException e) {
      // Expected.
    }
  }

  /* */
  @Test
  public void testLoadFactorOne() {
    assertEquals(0x100, minBufferSize(0x80, 1d));
    assertEquals(0x7f, expandAtCount(0x80, 1d));
    assertEquals(0xff, expandAtCount(0x100, 1d));
  }
}
