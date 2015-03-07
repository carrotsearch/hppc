package com.carrotsearch.hppc;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

import static com.carrotsearch.hppc.HashContainers.*;

public class HashContainersTest extends RandomizedTest {
  /* */
  @Test
  public void testRoundCapacity()
  {
      assertEquals(MIN_ARRAY_SIZE, minBufferSize(0, 0.5f));
      assertEquals(MIN_ARRAY_SIZE, minBufferSize(1, 0.5f));

      assertEquals(0x20, minBufferSize(0x10, 0.5f));
      assertEquals(0x40, minBufferSize(0x10, 0.49f));

      int maxCapacity = maxCapacity(HashContainers.MAX_LOAD_FACTOR);
      assertEquals(0x40000000, minBufferSize(maxCapacity, MAX_LOAD_FACTOR));
      
      // This would fill the array fully, validating the invariant, but should
      // be possible without reallocating the buffer.
      minBufferSize(maxCapacity + 1, MAX_LOAD_FACTOR);
      assertEquals(maxCapacity + 1, expandAtCount(MAX_ARRAY_SIZE, MAX_LOAD_FACTOR));

      try {
        // This should be impossible because it'd create a negative-sized array.
        minBufferSize(maxCapacity + 2, MAX_LOAD_FACTOR);
        fail();
      } catch (BufferAllocationException e) {
        // Expected.
      }
  }
}
