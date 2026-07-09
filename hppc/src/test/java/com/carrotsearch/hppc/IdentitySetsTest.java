/*
 * HPPC
 *
 * Copyright (C) 2010-2026 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.newIntegerObject;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentitySetsTest {
  @Test
  public void testSanity() {
    ObjectIdentityHashSet<Integer> m1 = new ObjectIdentityHashSet<>();

    Integer a, b;
    Assertions.assertThat(m1.add(a = newIntegerObject(1))).isTrue();
    Assertions.assertThat(m1.add(a)).isFalse();
    Assertions.assertThat(m1.add(b = newIntegerObject(1))).isTrue();
    Assertions.assertThat(m1.add(b)).isFalse();

    Assertions.assertThat(m1.contains(a)).isTrue();
    Assertions.assertThat(m1.contains(b)).isTrue();
    Assertions.assertThat(m1.contains(newIntegerObject(1))).isFalse();

    Assertions.assertThat(m1.contains(null)).isFalse();
    Assertions.assertThat(m1.add(null)).isTrue();
    Assertions.assertThat(m1.add(null)).isFalse();

    assertEquals(3, m1.size());

    ObjectIdentityHashSet<Integer> m2 = new ObjectIdentityHashSet<>();
    m2.addAll(m1);

    Assertions.assertThat(m1).isEqualTo(m2);

    m2.remove(a);
    Assertions.assertThat(m1).isNotEqualTo(m2);
  }
}
