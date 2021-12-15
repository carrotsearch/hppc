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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;
import org.junit.Before;
import org.junit.Test;

/** Examples of how to iterate over HPPC sets. */
public class HppcExample_003_IteratingOverSets {

  public IntHashSet hashSet;

  @Before
  public void setup() {
    hashSet = IntHashSet.from(1, 1, 2, 3, 5, 0);
  }

  /**
   * All sets implement the {@link Iterable} interface that returns a "cursor" object that moves
   * over the container's elements. The cursor object remains identical for the entire iteration
   * (very little memory overhead).
   *
   * <p>The cursor's index points at the underlying buffer's position of a given element, the
   * cursor's value is the element itself.
   */
  @Test
  public void cursor() {
    for (IntCursor c : hashSet) {
      printfln("hashSet contains %d (at buffer index %d)", c.value, c.index);
      assertThat(c.value).isEqualTo(hashSet.keys[c.index]);
    }
  }

  /** A for-each type loop with an anonymous class. */
  @Test
  public void forEachLoop() {
    hashSet.forEach(
        new IntProcedure() {
          @Override
          public void apply(int value) {
            printfln("hashSet contains %d", value);
          }
        });
  }
}
