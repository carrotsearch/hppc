/*
 * HPPC
 *
 * Copyright (C) 2010-2020 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.sorting;

import java.util.function.IntBinaryOperator;

/**
 * In-place Quick sort with 3-way partitioning and ending with Insertion sort.
 *
 * <p>The sorting is not stable. Performance is O(n.log(n)) and memory is O(1) (although recursion
 * memory is O(log(n))).
 */
public final class QuickSort {

  /** Below this size threshold, the sub-range is sorted using Insertion sort. */
  static final int INSERTION_SORT_THRESHOLD = 10;

  /** Below this size threshold, the partition selection is simplified to a single median. */
  static final int SINGLE_MEDIAN_THRESHOLD = 50;

  /** No instantiation. */
  private QuickSort() {
    // No instantiation.
  }

  /** @see #sort(int, int, IntBinaryOperator, IntBinaryOperator) */
  public static void sort(int[] array, IntBinaryOperator comparator) {
    sort(array, 0, array.length, comparator);
  }

  /** @see #sort(int, int, IntBinaryOperator, IntBinaryOperator) */
  public static void sort(int[] array, int fromIndex, int toIndex, IntBinaryOperator comparator) {
    sort(
        fromIndex,
        toIndex,
        comparator,
        (i, j) -> {
          int swap = array[i];
          array[i] = array[j];
          array[j] = swap;
          return 0;
        });
  }

  /**
   * Performs a recursive in-place Quick sort. The sorting is not stable.
   *
   * @param fromIndex Index where to start sorting in the array, inclusive.
   * @param toIndex Index where to stop sorting in the array, exclusive.
   * @param comparator Compares elements based on their indices. Given indices i and j in the
   *     provided array, this comparator returns respectively -1/0/1 if the element at index i is
   *     respectively less/equal/greater than the element at index j.
   * @param swapper Swaps the elements in the array at the given indices. For example, a custom
   *     swapper may allow sorting two arrays simultaneously.
   */
  public static void sort(
      int fromIndex, int toIndex, IntBinaryOperator comparator, IntBinaryOperator swapper) {
    sortInner(fromIndex, toIndex - 1, comparator, swapper);
  }

  /**
   * @param start Start index, inclusive.
   * @param end End index, inclusive.
   * @param c Compares elements based on their indices.
   * @param s Swaps the elements in the array at the given indices.
   */
  private static void sortInner(
      final int start, final int end, IntBinaryOperator c, IntBinaryOperator s) {
    final int size = end - start + 1;

    if (size <= INSERTION_SORT_THRESHOLD) {
      insertionSort(start, end, c, s);
      return;
    }

    int middle = start + size >> 1;
    int median;
    if (size <= SINGLE_MEDIAN_THRESHOLD) {
      // Select the partition with a single median.
      median = median(start, middle, end, c);
    } else {
      // Select the partition with the median of medians.
      int range = size >> 3;
      int doubleRange = range << 1;
      int medianStart = median(start, start + range, start + doubleRange, c);
      int medianMiddle = median(middle - range, middle, middle + range, c);
      int medianEnd = median(end - doubleRange, end - range, end, c);
      median = median(medianStart, medianMiddle, medianEnd, c);
    }
    swap(start, median, s);

    // 3-way partitioning.
    int i = start;
    int j = end + 1;
    int p = start + 1;
    int q = end;
    while (true) {
      while (++i < end && comp(i, start, c) < 0) ;
      while (--j > start && comp(j, start, c) > 0) ;
      if (i >= j) {
        if (i == j && comp(i, start, c) == 0) {
          swap(i, p, s);
        }
        break;
      }
      swap(i, j, s);
      if (comp(i, start, c) == 0) {
        swap(i, p++, s);
      }
      if (comp(j, start, c) == 0) {
        swap(j, q--, s);
      }
    }
    i = j + 1;
    for (int k = start; k < p; k++) {
      swap(k, j--, s);
    }
    for (int k = end; k > q; k--) {
      swap(k, i++, s);
    }

    sortInner(start, j, c, s);
    sortInner(i, end, c, s);
  }

  /** Sorts from start to end indices inclusive with insertion sort. */
  private static void insertionSort(int start, int end, IntBinaryOperator c, IntBinaryOperator s) {
    for (int i = start + 1; i <= end; i++) {
      for (int j = i; j > start && comp(j - 1, j, c) > 0; j--) {
        swap(j - 1, j, s);
      }
    }
  }

  /** Returns the index of the median element among three elements at provided indices. */
  private static int median(int i, int j, int k, IntBinaryOperator c) {
    if (comp(i, j, c) < 0) {
      if (comp(j, k, c) <= 0) {
        return j;
      }
      return comp(i, k, c) < 0 ? k : i;
    }
    if (comp(j, k, c) >= 0) {
      return j;
    }
    return comp(i, k, c) < 0 ? i : k;
  }

  /** Compares two elements at provided indices. */
  private static int comp(int i, int j, IntBinaryOperator comparator) {
    return comparator.applyAsInt(i, j);
  }

  /** Swaps two elements at provided indices. */
  private static void swap(int i, int j, IntBinaryOperator swapper) {
    swapper.applyAsInt(i, j);
  }
}
