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

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ObjectIdentityHashSetTest extends RandomizedTest {
  /* */
  @Repeat(iterations = 500)
  @Test
  @SuppressForbidden("Legitimate use of new Integer()")
  public void testHashKeyUsesSystemIdentity() {
    int expectedElements = 200;
    ObjectIdentityHashSet<Integer> foo = new ObjectIdentityHashSet<>(expectedElements);
    for (int i = 0; i < expectedElements; i++) {
      @SuppressWarnings("deprecation")
      Integer v2 = new Integer(44);
      foo.add(v2);
    }

    // Assert that values didn't "cluster" into one contiguous block.
    // This ensures each object above had a different target bucket.
    int firstValue = 0;
    if (foo.keys[0] != null) {
      int i = firstValue;
      do {
        firstValue = i;
        i = (i - 1) & foo.mask;
      } while (foo.keys[i] != null);
    } else {
      while (foo.keys[firstValue] == null) {
        firstValue++;
      }
    }

    int blockSize = 0;
    while (foo.keys[firstValue] != null) {
      firstValue = (firstValue + 1) & foo.mask;
      blockSize++;
    }

    Assertions.assertThat(blockSize).isNotEqualTo(expectedElements);
  }
}
