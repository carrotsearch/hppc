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

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;
import org.junit.Test;

/** Examples of how to iterate over HPPC lists. */
public class HppcExample_001_IteratingOverLists {
  public final IntArrayList list = IntArrayList.from(1, 1, 2, 3, 5);

  /**
   * All lists implement {@link Iterable} interface that returns a "cursor" object that moves over
   * the container's elements. The cursor object remains identical for the entire iteration (very
   * little memory overhead).
   */
  @Test
  public void cursor() {
    for (IntCursor c : list) {
      printfln("list[%d] = %d", c.index, c.value);
    }
  }

  /** A simple loop over indices of a list. */
  @Test
  public void simpleLoop() {
    for (int i = 0, max = list.size(); i < max; i++) {
      printfln("list[%d] = %d", i, list.get(i));
    }
  }

  /**
   * A for-each type loop with an anonymous class. Note the index of the current element is not
   * passed to the procedure.
   */
  @Test
  public void forEachLoop() {
    list.forEach(
        new IntProcedure() {
          int index;

          public void apply(int value) {
            printfln("list[%d] = %d", index++, value);
          }
        });
  }

  /**
   * A direct buffer access.
   *
   * <p>Also see {@link HppcExample_005_IteratingOverObjectBuffers} for caveats concerning iterating
   * over object buffers.
   */
  @Test
  public void directBufferLoop() {
    final int[] buffer = list.buffer;
    final int size = list.size();
    for (int i = 0; i < size; i++) {
      printfln("list[%d] = %d", i, buffer[i]);
    }
  }
}
