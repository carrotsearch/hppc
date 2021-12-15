/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.sorting;

import static com.carrotsearch.hppc.sorting.SortTest.assertOrder;
import static org.junit.Assert.assertTrue;

import com.carrotsearch.hppc.XorShift128P;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import org.junit.Assert;
import org.junit.Test;

/** Test cases for {@link IndirectSort}. */
public class IndirectSortTest {
  private static final int DATA_LENGTH = 1000000;

  /** Empty and single-item input. */
  @Test
  public void testEmptyAndSingle() {
    int[] mSortOrder = IndirectSort.mergesort(0, 0, Integer::compare);
    Assert.assertEquals(mSortOrder.length, 0);

    for (int i = 0; i < 1000; i++) {
      mSortOrder = IndirectSort.mergesort(0, i, Integer::compare);
      Assert.assertEquals(mSortOrder.length, i);
    }
  }

  /** Large ordered input. */
  @Test
  public void testOrderedMergeSort() {
    int[] order = IndirectSort.mergesort(0, DATA_LENGTH, Integer::compare);
    assertOrder(order, DATA_LENGTH, Integer::compare);
  }

  /** Randomized input, ascending int comparator. */
  @Test
  public void testAscInt() {
    final int maxSize = 500;
    final int rounds = 1000;
    final int vocabulary = 10;
    final Random rnd = new Random(0x11223344);

    for (int round = 0; round < rounds; round++) {
      final int[] input = generateRandom(maxSize, vocabulary, rnd);
      final int start = rnd.nextInt(input.length - 1);
      final int length = (input.length - start);

      IntBinaryOperator intBinaryOperator = (a, b) -> Integer.compare(input[a], input[b]);
      int[] order = IndirectSort.mergesort(start, length, intBinaryOperator);

      assertOrder(order, length, intBinaryOperator);
    }
  }

  /** Randomized input, ascending double comparator. */
  @Test
  public void testAscDouble() {
    final int maxSize = 1000;
    final int rounds = 1000;
    final Random rnd = new Random(0x11223344);

    for (int round = 0; round < rounds; round++) {
      final double[] input = generateRandom(maxSize, rnd);

      final IntBinaryOperator comparator = (a, b) -> Double.compare(input[a], input[b]);

      final int start = rnd.nextInt(input.length - 1);
      final int length = (input.length - start);

      int[] order = IndirectSort.mergesort(start, length, comparator);
      assertOrder(order, length, comparator);
    }
  }

  /**
   * Sort random integers from the range 0..0xff based on their 4 upper bits. The relative order of
   * 0xf0-masked integers should be preserved from the input.
   */
  @Test
  public void testMergeSortIsStable() {
    final XorShift128P rnd = new XorShift128P(0xdeadbeefL);
    final int[] data = new int[10000];

    for (int i = 0; i < data.length; i++) {
      data[i] = rnd.nextInt(0x100);
    }

    int[] order =
        IndirectSort.mergesort(
            0, data.length, (indexA, indexB) -> (data[indexA] & 0xf0) - (data[indexB] & 0xf0));

    for (int i = 1; i < order.length; i++) {
      if ((data[order[i - 1]] & 0xf0) == (data[order[i]] & 0xf0)) {
        assertTrue(order[i - 1] < order[i]);
      }
    }
  }

  private int[] generateRandom(final int maxSize, final int vocabulary, final Random rnd) {
    final int[] input = new int[2 + rnd.nextInt(maxSize)];
    for (int i = 0; i < input.length; i++) {
      input[i] = vocabulary / 2 - rnd.nextInt(vocabulary);
    }
    return input;
  }

  private double[] generateRandom(final int maxSize, final Random rnd) {
    final double[] input = new double[2 + rnd.nextInt(maxSize)];
    for (int i = 0; i < input.length; i++) {
      input[i] = rnd.nextGaussian();
    }
    return input;
  }
}
