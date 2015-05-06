package com.carrotsearch.hppc.examples;

import static com.carrotsearch.hppc.examples.Helpers.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.IntLongHashMap;
import com.carrotsearch.hppc.IntLongScatterMap;
import com.carrotsearch.hppc.cursors.IntLongCursor;
import com.carrotsearch.hppc.procedures.IntLongProcedure;

/**
 * Examples of how to iterate over HPPC maps.
 */
public class HppcExample_004_IteratingOverMaps {

  public IntLongHashMap hashMap;
  public IntLongScatterMap scatterMap;

  @Before
  public void setup() {
    // Note that copying the other way around wouldn't be safe (and is thus impossible
    // because there is no IntLongHashMap constructor that can copy keys from another
    // container).
    scatterMap = IntLongScatterMap.from(
        new int  [] {1, 1, 2, 3, 5, 0},
        new long [] {1, 2, 3, 4, 5, 6});

    hashMap = new IntLongHashMap(scatterMap);
  }

  /**
   * All maps implement the {@link Iterable} interface that returns a "cursor"
   * object that moves over the container's elements. The cursor object remains
   * identical for the entire iteration (very little memory overhead).
   * 
   * The cursor's index points at the underlying buffer's position of a 
   * given element, the cursor's key is the key itself and cursor's value is the
   * value associated with the key.
   */
  @Test
  public void cursor() {
    for (IntLongCursor c : scatterMap) {
      printfln("scatterMap %d => %d (at buffer index %d)", c.key, c.value, c.index);
      assertThat(c.value).isEqualTo(scatterMap.values[c.index]);
      assertThat(c.key).isEqualTo(scatterMap.keys[c.index]);
    }

    for (IntLongCursor c : hashMap) {
      printfln("hashMap %d => %d (at buffer index %d)", c.key, c.value, c.index);
      assertThat(c.value).isEqualTo(hashMap.values[c.index]);
      assertThat(c.key).isEqualTo(hashMap.keys[c.index]);
    }
  }

  /**
   * A for-each type loop with an anonymous class.
   */
  @Test
  public void forEachLoop() {
    scatterMap.forEach(new IntLongProcedure() {
      @Override
      public void apply(int key, long value) {
        printfln("scatterMap %d => %d", key, value);
      }
    });
    
    hashMap.forEach(new IntLongProcedure() {
      @Override
      public void apply(int key, long value) {
        printfln("hashMap %d => %d", key, value);
      }
    });    
  }
}
