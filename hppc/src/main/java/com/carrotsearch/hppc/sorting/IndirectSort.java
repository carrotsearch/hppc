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

import java.util.Comparator;
import java.util.function.IntBinaryOperator;

/**
 * Sorting routines that return an array of sorted indices implied by a given comparator rather than
 * move elements of whatever the comparator is using for comparisons.
 *
 * <p>A practical use case for this class is when the index of an array is meaningful and one wants
 * to acquire the order of values in that array. None of the methods in Java Collections would
 * provide such functionality directly and creating a collection of boxed {@link Integer} objects
 * for indices seems to be too costly.
 */
public final class IndirectSort {
  /** Minimum window length to apply insertion sort in merge sort. */
  static int MIN_LENGTH_FOR_INSERTION_SORT = 30;

  /** No instantiation. */
  private IndirectSort() {
    // No instantiation.
  }

  /**
   * Returns the order of elements between indices <code>start</code> and <code>length</code>, as
   * indicated by the given <code>comparator</code>.
   *
   * <p>This routine uses merge sort. It is guaranteed to be stable. It creates a new indices array,
   * and clones it while sorting.
   */
  public static int[] mergesort(int start, int length, IntBinaryOperator comparator) {
    final int[] src = createOrderArray(start, length);
    return mergesort(src, comparator);
  }

  /**
   * Returns a sorted copy of the order array provided, using the given <code>comparator</code>.
   *
   * <p>This routine uses merge sort. It is guaranteed to be stable. The provided {@code
   * indicesArray} is cloned while sorting and the clone is returned.
   */
  public static int[] mergesort(int[] orderArray, IntBinaryOperator comparator) {
    if (orderArray.length <= 1) {
      return orderArray;
    }
    final int[] dst = orderArray.clone();
    topDownMergeSort(orderArray, dst, 0, orderArray.length, comparator);
    return dst;
  }

  /**
   * Returns the order of elements between indices <code>start</code> and <code>length</code>, as
   * indicated by the given <code>comparator</code>.
   *
   * <p>This routine uses merge sort. It is guaranteed to be stable. It creates a new indices array,
   * and clones it while sorting.
   */
  public static <T> int[] mergesort(
      T[] input, int start, int length, Comparator<? super T> comparator) {
    return mergesort(start, length, (a, b) -> comparator.compare(input[a], input[b]));
  }

  /**
   * Perform a recursive, descending merge sort.
   *
   * @param fromIndex inclusive
   * @param toIndex exclusive
   */
  private static void topDownMergeSort(
      int[] src, int[] dst, int fromIndex, int toIndex, IntBinaryOperator comp) {
    if (toIndex - fromIndex <= MIN_LENGTH_FOR_INSERTION_SORT) {
      insertionSort(fromIndex, toIndex - fromIndex, dst, comp);
      return;
    }

    final int mid = (fromIndex + toIndex) >>> 1;
    topDownMergeSort(dst, src, fromIndex, mid, comp);
    topDownMergeSort(dst, src, mid, toIndex, comp);

    /*
     * Both splits in of src are now sorted.
     */
    if (comp.applyAsInt(src[mid - 1], src[mid]) <= 0) {
      /*
       * If the lowest element in upper slice is larger than the highest element in
       * the lower slice, simply copy over, the data is fully sorted.
       */
      System.arraycopy(src, fromIndex, dst, fromIndex, toIndex - fromIndex);
    } else {
      /*
       * Run a manual merge.
       */
      for (int i = fromIndex, j = mid, k = fromIndex; k < toIndex; k++) {
        if (j == toIndex || (i < mid && comp.applyAsInt(src[i], src[j]) <= 0)) {
          dst[k] = src[i++];
        } else {
          dst[k] = src[j++];
        }
      }
    }
  }

  /** Internal insertion sort for <code>int</code>s. */
  private static void insertionSort(
      final int off, final int len, int[] order, IntBinaryOperator intComparator) {
    for (int i = off + 1; i < off + len; i++) {
      final int v = order[i];
      int j = i, t;
      while (j > off && intComparator.applyAsInt(t = order[j - 1], v) > 0) {
        order[j--] = t;
      }
      order[j] = v;
    }
  }

  /** Creates the initial order array. */
  private static int[] createOrderArray(final int start, final int length) {
    final int[] order = new int[length];
    for (int i = 0; i < length; i++) {
      order[i] = start + i;
    }
    return order;
  }
}
