
package com.carrotsearch.hppc.sorting;

import java.util.Comparator;

/**
 * Sorting routines that return an array of sorted indices implied by a given comparator
 * rather than move elements of whatever the comparator is using for comparisons.
 * <p>
 * A practical use case for this class is when the index of an array is meaningful and one
 * wants to acquire the order of values in that array. None of the methods in Java
 * Collections would provide such functionality directly and creating a collection of
 * boxed {@link Integer} objects for indices seems to be too costly.
 */
public final class IndirectSort
{
    /**
     * Minimum array length to apply quicksort (smaller arrays are sorted with insertion
     * sort). We use a bit larger values than Sedgewick's "magic constants".
     */
    static int MIN_LENGTH_FOR_QUICKSORT = 15;

    /** We use a bit larger values than Sedgewick's "magic constants". */
    static int MIN_MED3_SIZE = 30;

    /** We use a bit larger values than Sedgewick's "magic constants". */
    static int MIN_MED9_SIZE = 60;

    /**
     * No instantiation.
     */
    private IndirectSort()
    {
        // No instantiation.
    }

    /**
     * Returns the order of elements between indices <code>start</code> and
     * <code>length</code>, as indicated by the given <code>comparator</code>.
     */
    public static int [] sort(int start, int length, IndirectComparator comparator)
    {
        final int [] order = createOrderArray(start, length);
        quickSort(0, length, order, comparator);
        return order;
    }

    /**
     * Returns the order of elements between indices <code>start</code> and
     * <code>length</code>, as indicated by the given <code>comparator</code>. This method
     * is equivalent to calling {@link #sort(int, int, IndirectComparator)} with
     * {@link IndirectComparator.DelegatingComparator}.
     */
    public static <T> int [] sort(T [] input, int start, int length,
        Comparator<? super T> comparator)
    {
        return sort(start, length, new IndirectComparator.DelegatingComparator<T>(input,
            comparator));
    }

    /**
     * A slightly modified classic 3-way quicksort from L. Bentley and M. Douglas
     * McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol.
     * 23(11) P. 1249-1265 (November 1993), with fragments from Sedgewick's
     * "Algorithms in Java: Parts 1-4". The baseline implementation comes from the JDK
     * (it's almost a direct copy from Bentley's paper, so I don't think copyright applies
     * here), but has been changed in places.
     */
    private static void quickSort(int off, int len, int [] order, IndirectComparator comp)
    {
        if (len < MIN_LENGTH_FOR_QUICKSORT)
        {
            insertionSort(off, len, order, comp);
            return;
        }

        // Find the partition value's index v.
        final int v;
        {
            /* Small arrays: medium element, mid-size: median of 3, big: median of 9. */
            int m = off + (len >> 1);
            if (len > MIN_MED3_SIZE)
            {
                int l = off;
                int n = off + len - 1;
                if (len > MIN_MED9_SIZE)
                {
                    int s = len / 8;
                    l = med3(order, l, l + s, l + 2 * s, comp);
                    m = med3(order, m - s, m, m + s, comp);
                    n = med3(order, n - 2 * s, n - s, n, comp);
                }
                m = med3(order, l, m, n, comp);
            }
            v = order[m];
        }

        // Partitioning.
        int a = off, b = a, c = off + len - 1, d = c, t;
        while (true)
        {
            while (b <= c && (t = comp.compare(order[b], v)) <= 0)
            {
                if (t == 0) swap(order, a++, b);
                b++;
            }

            while (c >= b && (t = comp.compare(order[c], v)) >= 0)
            {
                if (t == 0) swap(order, c, d--);
                c--;
            }

            if (b > c) break;
            swap(order, b++, c--);
        }

        // Swap back equal elements.
        int s, n = off + len;
        s = Math.min(a - off, b - a);   vecswap(order, off, b - s, s);
        s = Math.min(d - c, n - d - 1); vecswap(order, b, n - s, s);

        // Go for smaller fragments first to reduce stack use.
        final int s1 = b - a;
        final int s2 = d - c;

        assert (s1 < len && s2 < len) 
            : "Recursive call must decrease range length, comparator: " + comp;

        if (s1 < s2)
        {
            if (s1 > 1) quickSort(off, s1, order, comp);
            if (s2 > 1) quickSort(n - s2, s2, order, comp);
        }
        else
        {
            if (s2 > 1) quickSort(n - s2, s2, order, comp);
            if (s1 > 1) quickSort(off, s1, order, comp);
        }
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], int a, int b, final int n)
    {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b);
    }


    /**
     * Internal insertion sort for <code>int</code>s.
     */
    private static void insertionSort(final int off, final int len, int [] order,
        IndirectComparator intComparator)
    {
        for (int i = off + 1; i < off + len; i++)
        {
            final int v = order[i];
            int j = i, t;
            while (j > off && intComparator.compare(t = order[j - 1], v) > 0)
            {
                order[j--] = t;
            }
            order[j] = v;
        }
    }

    /**
     * Median of three.
     */
    private static int med3(int [] order, int a, int b, int c, IndirectComparator comp)
    {
        final int va = order[a];
        final int vb = order[b];
        final int vc = order[c];

        return (comp.compare(va, vb) < 0 ? (comp.compare(vb, vc) < 0 ? b : comp.compare(
            va, vc) < 0 ? c : a) : (comp.compare(vb, vc) > 0 ? b
            : comp.compare(va, vc) > 0 ? c : a));
    }

    /**
     * Swap elements of an array. Should be inlined, hopefully.
     */
    private static final void swap(int [] array, int l, int i)
    {
        final int temp = array[l];
        array[l] = array[i];
        array[i] = temp;
    }

    /**
     * Creates the initial order array.
     */
    private static int [] createOrderArray(final int start, final int length)
    {
        final int [] order = new int [length];
        for (int i = 0; i < length; i++)
        {
            order[i] = start + i;
        }
        return order;
    }
}
