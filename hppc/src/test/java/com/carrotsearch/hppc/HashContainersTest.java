/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.HashContainers.*;
import static org.junit.Assert.*;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import java.util.HashSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

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

  @SuppressForbidden("new Integer() intentional.")
  @SuppressWarnings("deprecation")
  @Test
  public void testAddReplacements() {
    ObjectHashSet<Integer> set = new ObjectHashSet<>();
    HashSet<Integer> reference = new HashSet<>();

    Integer i1 = 1;
    Integer i2 = new Integer(i1.intValue());

    assertTrue(set.add(i1));
    assertTrue(reference.add(i1));

    assertFalse(set.add(i2));
    assertFalse(reference.add(i2));

    Assertions.assertThat(reference.iterator().next()).isSameAs(i1);
    Assertions.assertThat(set.iterator().next().value).isSameAs(i1);
  }
}
