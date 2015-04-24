package com.carrotsearch.hppc.sorting;

import java.util.Comparator;

/**
 * Compare objects at two given indices and return the result of their
 * comparison consistent with {@link Comparator}'s contract.
 * <p>
 * <b>Beware of the <code>return (int - int) idiom</code></b>, it is usually
 * broken if arbitrary numbers can appear on input. Use regular comparison
 * operations - they are very fast anyway.
 */
public interface IndirectComparator {
  /**
   * See class documentation.
   */
  public int compare(int indexA, int indexB);

  /**
   * A natural-order comparator for integers.
   */
  public static class AscendingIntComparator implements IndirectComparator {
    private final int[] array;

    public AscendingIntComparator(int[] array) {
      this.array = array;
    }

    public int compare(int indexA, int indexB) {
      final int a = array[indexA];
      final int b = array[indexB];

      if (a < b)
        return -1;
      if (a > b)
        return 1;
      return 0;
    }
  }

  /**
   * A reverse-order comparator for integers.
   */
  public static class DescendingIntComparator extends AscendingIntComparator {
    public DescendingIntComparator(int[] array) {
      super(array);
    }

    public final int compare(int indexA, int indexB) {
      return -super.compare(indexA, indexB);
    }
  }

  /**
   * A natural-order comparator for integers.
   */
  public static class AscendingShortComparator implements IndirectComparator {
    private final short[] array;

    public AscendingShortComparator(short[] array) {
      this.array = array;
    }

    public int compare(int indexA, int indexB) {
      final short a = array[indexA];
      final short b = array[indexB];

      if (a < b)
        return -1;
      if (a > b)
        return 1;
      return 0;
    }
  }

  /**
   * A reverse-order comparator for shorts.
   */
  public static class DescendingShortComparator extends AscendingShortComparator {
    public DescendingShortComparator(short[] array) {
      super(array);
    }

    public final int compare(int indexA, int indexB) {
      return -super.compare(indexA, indexB);
    }
  }

  /**
   * A natural-order comparator for doubles.
   */
  public static class AscendingDoubleComparator implements IndirectComparator {
    private final double[] array;

    public AscendingDoubleComparator(double[] array) {
      this.array = array;
    }

    public int compare(int indexA, int indexB) {
      final double a = array[indexA];
      final double b = array[indexB];

      if (a < b)
        return -1;
      if (a > b)
        return 1;
      return 0;
    }
  }

  /**
   * A reverse-order comparator for doubles.
   */
  public static class DescendingDoubleComparator extends AscendingDoubleComparator {
    public DescendingDoubleComparator(double[] array) {
      super(array);
    }

    public final int compare(int indexA, int indexB) {
      return -super.compare(indexA, indexB);
    }
  }

  /**
   * A natural-order comparator for floats.
   */
  public static class AscendingFloatComparator implements IndirectComparator {
    private final float[] array;

    public AscendingFloatComparator(float[] array) {
      this.array = array;
    }

    public int compare(int indexA, int indexB) {
      final float a = array[indexA];
      final float b = array[indexB];

      if (a < b)
        return -1;
      if (a > b)
        return 1;
      return 0;
    }
  }

  /**
   * A reverse-order comparator for floats.
   */
  public static class DescendingFloatComparator extends AscendingFloatComparator {
    public DescendingFloatComparator(float[] array) {
      super(array);
    }

    public final int compare(int indexA, int indexB) {
      return -super.compare(indexA, indexB);
    }
  }

  /**
   * A delegating comparator for object types.
   */
  public final static class DelegatingComparator<T> implements IndirectComparator {
    private final T[] array;
    private final Comparator<? super T> delegate;

    public DelegatingComparator(T[] array, Comparator<? super T> delegate) {
      this.array = array;
      this.delegate = delegate;
    }

    public final int compare(int indexA, int indexB) {
      return delegate.compare(array[indexA], array[indexB]);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + " -> " + delegate;
    }
  }
}
