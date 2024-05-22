/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
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
  static final int INSERTION_SORT_THRESHOLD = 16;

  /** /** Below this size threshold, the partition selection is simplified to a single median. */
  static final int SINGLE_MEDIAN_THRESHOLD = 40;

  /** No instantiation. */
  private QuickSort() {
    // No instantiation.
  }

  /**
   * @see #sort(int, int, IntBinaryOperator, IntBinaryOperator)
   */
  public static void sort(int[] array, IntBinaryOperator comparator) {
    sort(array, 0, array.length, comparator);
  }

  /**
   * @see #sort(int, int, IntBinaryOperator, IntBinaryOperator)
   */
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
    int size;
    while ((size = toIndex - fromIndex) > INSERTION_SORT_THRESHOLD) {

      // Pivot selection.
      int last = toIndex - 1;
      int middle = (fromIndex + last) >>> 1;
      int pivot;
      if (size <= SINGLE_MEDIAN_THRESHOLD) {
        // Select the pivot with a single median around the middle element.
        // Do not take the median between [from, mid, last] because it hurts performance
        // if the order is descending.
        int range = size >> 2;
        pivot = median(middle - range, middle, middle + range, comparator);
      } else {
        // Select the pivot with the median of medians.
        int range = size >> 3;
        int doubleRange = range << 1;
        int medianStart = median(fromIndex, fromIndex + range, fromIndex + doubleRange, comparator);
        int medianMiddle = median(middle - range, middle, middle + range, comparator);
        int medianEnd = median(last - doubleRange, last - range, last, comparator);
        pivot = median(medianStart, medianMiddle, medianEnd, comparator);
      }

      // Bentley-McIlroy 3-way partitioning.
      swap(fromIndex, pivot, swapper);
      int i = fromIndex;
      int j = toIndex;
      int p = fromIndex + 1;
      int q = last;
      while (true) {
        int leftCmp, rightCmp;
        while ((leftCmp = compare(++i, fromIndex, comparator)) < 0) {
          // repeat
        }
        while ((rightCmp = compare(--j, fromIndex, comparator)) > 0) {
          // repeat
        }
        if (i >= j) {
          if (i == j && rightCmp == 0) {
            swap(i, p, swapper);
          }
          break;
        }
        swap(i, j, swapper);
        if (rightCmp == 0) {
          swap(i, p++, swapper);
        }
        if (leftCmp == 0) {
          swap(j, q--, swapper);
        }
      }
      i = j + 1;
      for (int k = fromIndex; k < p; ) {
        swap(k++, j--, swapper);
      }
      for (int k = last; k > q; ) {
        swap(k--, i++, swapper);
      }

      // Recursion on the smallest partition.
      // Replace the tail recursion by a loop.
      if (j - fromIndex < last - i) {
        sort(fromIndex, j + 1, comparator, swapper);
        fromIndex = i;
      } else {
        sort(i, toIndex, comparator, swapper);
        toIndex = j + 1;
      }
    }

    insertionSort(fromIndex, toIndex, comparator, swapper);
  }

  /** Sorts between from (inclusive) and to (exclusive) with insertion sort. */
  private static void insertionSort(
      int fromIndex, int toIndex, IntBinaryOperator comparator, IntBinaryOperator swapper) {
    for (int i = fromIndex + 1; i < toIndex; ) {
      int current = i++;
      int previous;
      while (compare((previous = current - 1), current, comparator) > 0) {
        swap(previous, current, swapper);
        if (previous == fromIndex) {
          break;
        }
        current = previous;
      }
    }
  }

  /** Returns the index of the median element among three elements at provided indices. */
  private static int median(int i, int j, int k, IntBinaryOperator comparator) {
    if (compare(i, j, comparator) < 0) {
      if (compare(j, k, comparator) <= 0) {
        return j;
      }
      return compare(i, k, comparator) < 0 ? k : i;
    }
    if (compare(j, k, comparator) >= 0) {
      return j;
    }
    return compare(i, k, comparator) < 0 ? i : k;
  }

  /** Compares two elements at provided indices. */
  private static int compare(int i, int j, IntBinaryOperator comparator) {
    return comparator.applyAsInt(i, j);
  }

  /** Swaps two elements at provided indices. */
  private static void swap(int i, int j, IntBinaryOperator swapper) {
    swapper.applyAsInt(i, j);
  }
}
