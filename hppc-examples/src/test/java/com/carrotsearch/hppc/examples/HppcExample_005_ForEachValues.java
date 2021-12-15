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

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import org.junit.Test;

/** An example of plucking a value from an anonymous inner class. */
public class HppcExample_005_ForEachValues {
  @Test
  public void forEach_FilteredCounting() {
    // Create a map of key-value pairs.
    final IntIntHashMap map = new IntIntHashMap();
    map.put(1, 2);
    map.put(2, -5);
    map.put(3, 10);
    map.put(4, -1);

    // And count only non-negative values. Note the "counter"
    // is physically part of the anonymous inner class but we can
    // access it because of how forEach is declared (it returns the exact
    // subclass type of the argument).

    int count =
        map.forEach(
                new IntIntProcedure() {
                  int counter;

                  public void apply(int key, int value) {
                    if (value >= 0) {
                      counter++;
                    }
                  }
                })
            .counter;

    System.out.println("There are " + count + " values that are non-negative.");
  }
}
