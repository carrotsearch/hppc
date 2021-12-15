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

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import org.junit.Assert;
import org.junit.Test;

public class SortTest {

  enum DataDistribution {
    ORDERED,
    SAWTOOTH,
    RANDOM,
    STAGGER,
    PLATEAU,
    SHUFFLE
  }

  enum Algorithm {
    INDIRECT_MERGESORT(
        x -> {
          IntBinaryOperator comparator = (i, j) -> Integer.compare(x[i], x[j]);
          int[] order = IndirectSort.mergesort(0, x.length, comparator);
          assertOrder(order, x.length, comparator);
        }),
    QUICKSORT(
        x -> {
          IntBinaryOperator comparator = (i, j) -> Integer.compare(x[i], x[j]);
          QuickSort.sort(x, comparator);
          assertOrder(x, x.length, Integer::compare);
        }),
    ;
    private final Tester tester;

    Algorithm(Tester tester) {
      this.tester = tester;
    }

    void test(int[] x) {
      tester.test(copy(x));
    }

    private interface Tester {
      void test(int[] x);
    }
  }

  @Test
  public void testSortCertificationIndirectMergeSort() {
    sortCertification(Algorithm.INDIRECT_MERGESORT);
  }

  @Test
  public void testSortCertificationQuickSort() {
    sortCertification(Algorithm.QUICKSORT);
  }

  /** Run a "sort certification" test as in Bentley and McIlroy's paper. */
  private static void sortCertification(Algorithm algorithm) {
    int[] n_values = {100, 1023, 1024, 1025, 1024 * 32};

    long startTimeNs = System.nanoTime();
    for (int n : n_values) {
      for (int m = 1; m < 2 * n; m *= 2) {
        for (DataDistribution dist : DataDistribution.values()) {
          int[] x = generate(dist, n, m);

          String testName = dist + "-" + n + "-" + m;
          testOn(algorithm, x, testName + "-normal");
          testOn(algorithm, reverse(x, 0, n), testName + "-reversed");
          testOn(algorithm, reverse(x, 0, n / 2), testName + "-reversed_front");
          testOn(algorithm, reverse(x, n / 2, n), testName + "-reversed_back");
          testOn(algorithm, sort(x), testName + "-sorted");
          testOn(algorithm, dither(x), testName + "-dither");
        }
      }
    }
    long timeNs = System.nanoTime() - startTimeNs;
    System.out.println(algorithm + " time = " + timeNs + " ns");
  }

  /**
   * Generate <code>n</code>-length data set distributed according to <code>dist</code>.
   *
   * @param m Step for sawtooth, stagger, plateau and shuffle.
   */
  private static int[] generate(final DataDistribution dist, int n, int m) {
    // Start from a constant seed (repeatable tests).
    final Random rand = new Random(0x11223344);
    final int[] x = new int[n];
    for (int i = 0, j = 0, k = 1; i < n; i++) {
      switch (dist) {
        case ORDERED:
          x[i] = i;
          break;
        case SAWTOOTH:
          x[i] = i % m;
          break;
        case RANDOM:
          x[i] = rand.nextInt() % m;
          break;
        case STAGGER:
          x[i] = (i * m + i) % n;
          break;
        case PLATEAU:
          x[i] = Math.min(i, m);
          break;
        case SHUFFLE:
          x[i] = (rand.nextInt() % m) != 0 ? (j += 2) : (k += 2);
          break;
        default:
          throw new RuntimeException();
      }
    }

    return x;
  }

  private static int[] sort(int[] x) {
    x = copy(x);
    Arrays.sort(x);
    return x;
  }

  private static int[] dither(int[] x) {
    x = copy(x);
    for (int i = 0; i < x.length; i++) x[i] += i % 5;
    return x;
  }

  private static int[] reverse(int[] x, int start, int end) {
    x = copy(x);
    for (int i = start, j = end - 1; i < j; i++, j--) {
      int v = x[i];
      x[i] = x[j];
      x[j] = v;
    }
    return x;
  }

  private static int[] copy(int[] x) {
    return x.clone();
  }

  private static void testOn(Algorithm algo, int[] x, String testName) {
    try {
      algo.test(x);
    } catch (AssertionError e) {
      throw new AssertionError(testName + " failed", e);
    }
  }

  static void assertOrder(int[] order, int length, IntBinaryOperator comparator) {
    for (int i = 1; i < length; i++) {
      Assert.assertTrue(comparator.applyAsInt(order[i - 1], order[i]) <= 0);
    }
  }
}
