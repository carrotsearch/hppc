/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.newIntegerObject;

import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class ObjectIdentityHashSetTest extends RandomizedTest {
  /* */
  @RepeatedTest(500)
  public void testHashKeyUsesSystemIdentity() {
    int expectedElements = 200;
    ObjectIdentityHashSet<Integer> foo = new ObjectIdentityHashSet<>(expectedElements);
    for (int i = 0; i < expectedElements; i++) {
      Integer v2 = newIntegerObject(44);
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
