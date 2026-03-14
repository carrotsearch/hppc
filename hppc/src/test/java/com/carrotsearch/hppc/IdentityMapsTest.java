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
import static org.junit.jupiter.api.Assertions.*;

import com.carrotsearch.hppc.internals.SuppressForbidden;
import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import org.junit.jupiter.api.Test;

public class IdentityMapsTest extends RandomizedTest {
  @Test
  public void testSanity() {
    ObjectCharIdentityHashMap<Integer> m1 = new ObjectCharIdentityHashMap<>();

    Integer a, b;
    m1.put(a = newIntegerObject(1), 'a');
    m1.put(b = newIntegerObject(1), 'b');

    assertEquals('a', m1.get(a));
    assertEquals('b', m1.get(b));
    assertEquals(2, m1.size());

    ObjectCharIdentityHashMap<Integer> m2 = new ObjectCharIdentityHashMap<>();
    m2.put(b, 'b');
    m2.put(a, 'a');

    assertEquals(m1, m2);
    assertEquals(m2, m1);

    m2.remove(a);
    m2.put(newIntegerObject(1), 'a');
    assertNotEquals(m1, m2);
    assertNotEquals(m2, m1);
  }

  @Test
  public void testEqualsComparesValuesByReference() {
    ObjectObjectIdentityHashMap<String, String> m1 = new ObjectObjectIdentityHashMap<>();
    ObjectObjectIdentityHashMap<String, String> m2 = new ObjectObjectIdentityHashMap<>();

    String a = "a";
    String av = "av";
    String b = "b";
    String bv = "bv";

    m1.put(a, av);
    m1.put(b, bv);

    m2.put(b, bv);
    m2.put(a, av);

    assertEquals(m1, m2);
    assertEquals(m2, m1);

    m2.put(a, new String(av));
    assertNotEquals(m1, m2);
    assertNotEquals(m2, m1);
  }

  @SuppressForbidden
  @SuppressWarnings("removal")
  @Test
  public void testNaNsInValues() {
    ObjectDoubleIdentityHashMap<String> m1 = new ObjectDoubleIdentityHashMap<>();
    ObjectDoubleIdentityHashMap<String> m2 = new ObjectDoubleIdentityHashMap<>();

    String a = "a";
    Double av = Double.NaN;

    m1.put(a, av);
    m2.put(a, av);

    assertEquals(m1, m2);
    assertEquals(m2, m1);

    // value storage is an array of primitives, so NaNs should be equal, even though the object
    // was different.
    m2.put(a, new Double(Double.NaN));
    assertEquals(m1, m2);
    assertEquals(m2, m1);

    DoubleContainer values = m1.values();
    assertTrue(values.contains(Double.NaN));
  }
}
