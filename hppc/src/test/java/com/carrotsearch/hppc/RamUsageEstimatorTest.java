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
import java.util.Random;
import org.junit.jupiter.api.Test;

public class RamUsageEstimatorTest extends RandomizedTest {

  @Test
  public void testConstants() {
    assertTrue(
        RamUsageEstimator.NUM_BYTES_OBJECT_REF == 4 || RamUsageEstimator.NUM_BYTES_OBJECT_REF == 8);
    assertTrue(RamUsageEstimator.NUM_BYTES_OBJECT_HEADER >= 8);
    assertEquals(
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES,
        RamUsageEstimator.NUM_BYTES_ARRAY_HEADER);
    assertTrue(RamUsageEstimator.NUM_BYTES_OBJECT_ALIGNMENT >= 8);

    assertEquals(1, RamUsageEstimator.primitiveSizes.get(boolean.class).intValue());
    assertEquals(1, RamUsageEstimator.primitiveSizes.get(byte.class).intValue());
    assertEquals(Character.BYTES, RamUsageEstimator.primitiveSizes.get(char.class).intValue());
    assertEquals(Short.BYTES, RamUsageEstimator.primitiveSizes.get(short.class).intValue());
    assertEquals(Integer.BYTES, RamUsageEstimator.primitiveSizes.get(int.class).intValue());
    assertEquals(Float.BYTES, RamUsageEstimator.primitiveSizes.get(float.class).intValue());
    assertEquals(Double.BYTES, RamUsageEstimator.primitiveSizes.get(double.class).intValue());
    assertEquals(Long.BYTES, RamUsageEstimator.primitiveSizes.get(long.class).intValue());
    assertEquals(8, RamUsageEstimator.primitiveSizes.size());

    assertThrows(
        UnsupportedOperationException.class,
        () -> RamUsageEstimator.primitiveSizes.put(void.class, 0));
  }

  @Test
  public void testAlignObjectSize() {
    int alignment = RamUsageEstimator.NUM_BYTES_OBJECT_ALIGNMENT;

    assertEquals(0L, RamUsageEstimator.alignObjectSize(0));
    assertEquals(alignment, RamUsageEstimator.alignObjectSize(1));
    assertEquals(alignment, RamUsageEstimator.alignObjectSize(alignment - 1));
    assertEquals(alignment, RamUsageEstimator.alignObjectSize(alignment));
    assertEquals(2L * alignment, RamUsageEstimator.alignObjectSize(alignment + 1));
    assertEquals(2L * alignment, RamUsageEstimator.alignObjectSize(2L * alignment));

    for (long size = 0; size < 1000; size++) {
      long aligned = RamUsageEstimator.alignObjectSize(size);
      assertEquals(0, aligned % alignment);
      assertTrue(aligned >= size);
      assertTrue(aligned - size < alignment);
    }
  }

  @Test
  public void testAlignObjectSizeRandom(Random rnd) {
    int alignment = RamUsageEstimator.NUM_BYTES_OBJECT_ALIGNMENT;
    for (int i = 0; i < 5000; i++) {
      long size = Math.abs(rnd.nextLong()) % 1_000_000_000L;
      long aligned = RamUsageEstimator.alignObjectSize(size);
      assertEquals(0, aligned % alignment);
      assertTrue(aligned >= size);
      assertTrue(aligned - size < alignment);
    }
  }

  @Test
  public void testShallowSizeOfPrimitiveArrays() {
    int len = 100;
    boolean[] booleans = new boolean[len];
    byte[] bytes = new byte[len];
    char[] chars = new char[len];
    short[] shorts = new short[len];
    int[] ints = new int[len];
    float[] floats = new float[len];
    double[] doubles = new double[len];
    long[] longs = new long[len];

    assertEquals(
        RamUsageEstimator.alignObjectSize(RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len),
        RamUsageEstimator.shallowSizeOfArray(booleans));

    assertEquals(
        RamUsageEstimator.alignObjectSize(RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len),
        RamUsageEstimator.shallowSizeOfArray(bytes));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Character.BYTES),
        RamUsageEstimator.shallowSizeOfArray(chars));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Short.BYTES),
        RamUsageEstimator.shallowSizeOfArray(shorts));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Integer.BYTES),
        RamUsageEstimator.shallowSizeOfArray(ints));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Float.BYTES),
        RamUsageEstimator.shallowSizeOfArray(floats));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Double.BYTES),
        RamUsageEstimator.shallowSizeOfArray(doubles));

    assertEquals(
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) len * Long.BYTES),
        RamUsageEstimator.shallowSizeOfArray(longs));
  }

  @Test
  public void testShallowSizeOfObjectArrays() {
    int len = 50;
    Object[] objects = new Object[len];
    String[] strings = new String[len];
    Integer[] integers = new Integer[len];
    int[][] multiArray = new int[len][];

    long expected =
        RamUsageEstimator.alignObjectSize(
            RamUsageEstimator.NUM_BYTES_ARRAY_HEADER
                + (long) len * RamUsageEstimator.NUM_BYTES_OBJECT_REF);

    assertEquals(expected, RamUsageEstimator.shallowSizeOfArray(objects));
    assertEquals(expected, RamUsageEstimator.shallowSizeOfArray(strings));
    assertEquals(expected, RamUsageEstimator.shallowSizeOfArray(integers));
    assertEquals(expected, RamUsageEstimator.shallowSizeOfArray(multiArray));
  }

  @Test
  public void testShallowSizeOfEmptyArrays() {
    long emptyArraySize =
        RamUsageEstimator.alignObjectSize(RamUsageEstimator.NUM_BYTES_ARRAY_HEADER);

    assertEquals(emptyArraySize, RamUsageEstimator.shallowSizeOfArray(new byte[0]));
    assertEquals(emptyArraySize, RamUsageEstimator.shallowSizeOfArray(new int[0]));
    assertEquals(emptyArraySize, RamUsageEstimator.shallowSizeOfArray(new long[0]));
    assertEquals(emptyArraySize, RamUsageEstimator.shallowSizeOfArray(new Object[0]));
  }

  @Test
  public void testShallowUsedSizeOfArray() {
    int capacity = 100;
    int[] ints = new int[capacity];

    long sizeAt0 = RamUsageEstimator.shallowUsedSizeOfArray(ints, 0);
    long expectedSizeAt0 =
        RamUsageEstimator.alignObjectSize(RamUsageEstimator.NUM_BYTES_ARRAY_HEADER);
    assertEquals(expectedSizeAt0, sizeAt0);

    for (int used = 0; used <= capacity; used++) {
      long usedSize = RamUsageEstimator.shallowUsedSizeOfArray(ints, used);
      long expected =
          RamUsageEstimator.alignObjectSize(
              RamUsageEstimator.NUM_BYTES_ARRAY_HEADER + (long) used * Integer.BYTES);
      assertEquals(expected, usedSize);
    }

    Object[] objects = new Object[capacity];
    for (int used = 0; used <= capacity; used++) {
      long usedSize = RamUsageEstimator.shallowUsedSizeOfArray(objects, used);
      long expected =
          RamUsageEstimator.alignObjectSize(
              RamUsageEstimator.NUM_BYTES_ARRAY_HEADER
                  + (long) used * RamUsageEstimator.NUM_BYTES_OBJECT_REF);
      assertEquals(expected, usedSize);
    }
  }

  @Test
  public void testShallowSizeMatchesShallowUsedSize() {
    int len = 42;
    int[] ints = new int[len];
    assertEquals(
        RamUsageEstimator.shallowSizeOfArray(ints),
        RamUsageEstimator.shallowUsedSizeOfArray(ints, len));

    String[] strings = new String[len];
    assertEquals(
        RamUsageEstimator.shallowSizeOfArray(strings),
        RamUsageEstimator.shallowUsedSizeOfArray(strings, len));
  }
}
