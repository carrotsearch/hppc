/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.examples;

import static com.carrotsearch.hppc.examples.Helpers.*;
import static org.assertj.core.api.Assertions.*;

import com.carrotsearch.hppc.IntLongHashMap;
import com.carrotsearch.hppc.IntLongWormMap;
import com.carrotsearch.hppc.cursors.IntLongCursor;
import com.carrotsearch.hppc.procedures.IntLongProcedure;
import com.carrotsearch.hppc.procedures.IntProcedure;
import org.junit.Before;
import org.junit.Test;

/** Examples of how to iterate over HPPC maps. */
public class HppcExample_004_IteratingOverMaps {

  public IntLongHashMap hashMap;
  public IntLongWormMap wormMap;

  @Before
  public void setup() {
    hashMap = IntLongHashMap.from(new int[] {1, 1, 2, 3, 5, 0}, new long[] {1, 2, 3, 4, 5, 6});
    // WormMap is a hash map efficient for less than 2M entries, and more get() than put().
    wormMap = IntLongWormMap.from(new int[] {1, 1, 2, 3, 5, 0}, new long[] {1, 2, 3, 4, 5, 6});
  }

  /**
   * All maps implement the {@link Iterable} interface that returns a "cursor" object that moves
   * over the container's elements. The cursor object remains identical for the entire iteration
   * (very little memory overhead).
   *
   * <p>The cursor's index points at the underlying buffer's position of a given element, the
   * cursor's key is the key itself and cursor's value is the value associated with the key.
   */
  @Test
  public void cursor() {
    for (IntLongCursor c : hashMap) {
      printfln("hashMap %d => %d (at buffer index %d)", c.key, c.value, c.index);
      assertThat(c.value).isEqualTo(hashMap.values[c.index]);
      assertThat(c.key).isEqualTo(hashMap.keys[c.index]);
    }
  }

  /** A for-each type loop with an anonymous class. */
  @Test
  public void forEachLoop() {
    hashMap.forEach(
        new IntLongProcedure() {
          @Override
          public void apply(int key, long value) {
            printfln("hashMap %d => %d", key, value);
          }
        });
  }

  /** A for-each type loop on keys. */
  @Test
  public void keys() {
    wormMap
        .keys()
        .forEach(
            new IntProcedure() {
              @Override
              public void apply(int key) {
                printfln("key %d", key);
              }
            });
  }
}
