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

import static org.junit.jupiter.api.Assertions.*;

import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import com.carrotsearch.randomizedtesting.jupiter.generators.RandomPicks;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.RepeatedTest;

public class EntryShiftingOnRemovalTest extends RandomizedTest {
  @RepeatedTest(10)
  public void testRemoveSanity(Random rnd) {
    @SuppressWarnings("deprecation")
    IntHashSet v =
        new IntHashSet(8, 0.5d) {
          @Override
          protected int hashKey(int key) {
            return key & 0xff;
          }
        };

    Set<Integer> ref = new HashSet<Integer>();
    for (int i = 0; i < 4; i++) {
      int r = rnd.nextInt() & 0xffff;
      ref.add(r);
      v.add(r);
    }

    Integer[] array = ref.toArray(new Integer[ref.size()]);
    int remove = RandomPicks.randomFrom(rnd, array);
    ref.remove(remove);
    v.remove(remove);

    int[] actual = v.toArray();
    assertTrue(actual.length == ref.size());
    for (Integer ri : ref) {
      assertTrue(v.contains(ri));
    }
  }
}
