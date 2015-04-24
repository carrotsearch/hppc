package com.carrotsearch.hppc.sorting;

import java.util.Comparator;

/**
 * Sorting routines that return an array of sorted indices implied by a given
 * comparator rather than move elements of whatever the comparator is using for
 * comparisons.
 * <p>
 * A practical use case for this class is when the index of an array is
 * meaningful and one wants to acquire the order of values in that array. None
 * of the methods in Java Collections would provide such functionality directly
 * and creating a collection of boxed {@link Integer} objects for indices seems
 * to be too costly.
 */
public final class IndirectSort {
  /**
   * Minimum window length to apply insertion sort in merge sort.
   */
  static int MIN_LENGTH_FOR_INSERTION_SORT = 30;

  /**
   * No instantiation.
   */
  private IndirectSort() {
    // No instantiation.
  }

  /**
   * Returns the order of elements between indices <code>start</code> and
   * <code>length</code>, as indicated by the given <code>comparator</code>.
   * <p>
   * This routine uses merge sort. It is guaranteed to be stable.
   * </p>
   */
  public static int[] mergesort(int start, int length, IndirectComparator comparator) {
    final int[] src = createOrderArray(start, length);

    if (length > 1) {
      final int[] dst = (int[]) src.clone();
      topDownMergeSort(src, dst, 0, length, comparator);
      return dst;
    }

    return src;
  }

  /**
   * Returns the order of elements between indices <code>start</code> and
   * <code>length</code>, as indicated by the given <code>comparator</code>.
   * This method is equivalent to calling
   * {@link #mergesort(int, int, IndirectComparator)} with
   * {@link IndirectComparator.DelegatingComparator}.
   * <p>
   * This routine uses merge sort. It is guaranteed to be stable.
   * </p>
   */
  public static <T> int[] mergesort(T[] input, int start, int length, Comparator<? super T> comparator) {
    return mergesort(start, length, new IndirectComparator.DelegatingComparator<T>(input, comparator));
  }

  /**
   * Perform a recursive, descending merge sort.
   * 
   * @param fromIndex
   *          inclusive
   * @param toIndex
   *          exclusive
   */
  private static void topDownMergeSort(int[] src, int[] dst, int fromIndex, int toIndex, IndirectComparator comp) {
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
    if (comp.compare(src[mid - 1], src[mid]) <= 0) {
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
        if (j == toIndex || (i < mid && comp.compare(src[i], src[j]) <= 0)) {
          dst[k] = src[i++];
        } else {
          dst[k] = src[j++];
        }
      }
    }
  }

  /**
   * Internal insertion sort for <code>int</code>s.
   */
  private static void insertionSort(final int off, final int len, int[] order, IndirectComparator intComparator) {
    for (int i = off + 1; i < off + len; i++) {
      final int v = order[i];
      int j = i, t;
      while (j > off && intComparator.compare(t = order[j - 1], v) > 0) {
        order[j--] = t;
      }
      order[j] = v;
    }
  }

  /**
   * Creates the initial order array.
   */
  private static int[] createOrderArray(final int start, final int length) {
    final int[] order = new int[length];
    for (int i = 0; i < length; i++) {
      order[i] = start + i;
    }
    return order;
  }
}
