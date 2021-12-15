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

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;

/** Examples of how to iterate over HPPC deques. */
public class HppcExample_002_IteratingOverDeques {

  public IntArrayDeque deque;

  @Before
  public void setup() {
    deque = new IntArrayDeque();
    deque.addLast(1, 1, 2, 3, 5);
  }

  /**
   * All deques implement {@link Iterable} interface that returns a "cursor" object that moves over
   * the container's elements. The cursor object remains identical for the entire iteration (very
   * little memory overhead).
   *
   * <p>The cursor's index points at the underlying buffer's position of a given element.
   */
  @Test
  public void cursorFirstToLast() {
    for (IntCursor c : deque) {
      printfln("deque[%d] = %d", c.index, c.value);
    }
  }

  /** Reverse order iterator (cursor). */
  @Test
  public void cursorLastToFirst() {
    for (Iterator<IntCursor> i = deque.descendingIterator(); i.hasNext(); ) {
      IntCursor c = i.next();
      printfln("deque[%d] = %d", c.index, c.value);
    }
  }

  /** A for-each type loop with an anonymous class. */
  @Test
  public void forEachLoop() {
    deque.forEach(
        new IntProcedure() {
          public void apply(int value) {
            printfln("deque[?] = %d", value);
          }
        });
  }

  /** A for-each type loop with an anonymous class. */
  @Test
  public void forEachLoopReversed() {
    deque.descendingForEach(
        new IntProcedure() {
          public void apply(int value) {
            printfln("deque[?] = %d", value);
          }
        });
  }
}
