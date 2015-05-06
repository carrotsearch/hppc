package com.carrotsearch.hppc.examples;

import static com.carrotsearch.hppc.examples.Helpers.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntScatterSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;

/**
 * Examples of how to iterate over HPPC sets.
 */
public class HppcExample_003_IteratingOverSets {

  public IntHashSet hashSet;
  public IntScatterSet scatterSet;

  @Before
  public void setup() {
    // Note that copying the other way around wouldn't be safe (and is thus impossible
    // because there is no IntScatterSet constructor that can copy keys from another
    // container).
    scatterSet = IntScatterSet.from(1, 1, 2, 3, 5, 0);
    hashSet = new IntHashSet(scatterSet);
  }

  /**
   * All sets implement the {@link Iterable} interface that returns a "cursor"
   * object that moves over the container's elements. The cursor object remains
   * identical for the entire iteration (very little memory overhead).
   * 
   * The cursor's index points at the underlying buffer's position of a 
   * given element, the cursor's value is the element itself.
   */
  @Test
  public void cursor() {
    for (IntCursor c : scatterSet) {
      printfln("scatterSet contains %d (at buffer index %d)", c.value, c.index);
      assertThat(c.value).isEqualTo(scatterSet.keys[c.index]);
    }

    for (IntCursor c : hashSet) {
      printfln("hashSet contains %d (at buffer index %d)", c.value, c.index);
      assertThat(c.value).isEqualTo(hashSet.keys[c.index]);
    }
  }

  /**
   * A for-each type loop with an anonymous class.
   */
  @Test
  public void forEachLoop() {
    scatterSet.forEach(new IntProcedure() {
      @Override
      public void apply(int value) {
        printfln("scatterSet contains %d", value);
      }
    });
    
    hashSet.forEach(new IntProcedure() {
      @Override
      public void apply(int value) {
        printfln("hashSet contains %d", value);
      }
    });    
  }
}
