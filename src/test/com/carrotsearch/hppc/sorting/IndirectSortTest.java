package com.carrotsearch.hppc.sorting;

import java.util.Arrays;
import java.util.Random;

import org.junit.*;

/**
 * Test cases for {@link IndirectSort}.
 */
public class IndirectSortTest
{
    /**
     * Data length for certain tests.
     */
    static final int DATA_LENGTH = 1000000;

    /**
     * Implies the same order as the order of indices.
     */
    private static class OrderedInputComparator implements IndirectComparator
    {
        public int compare(int a, int b)
        {
            if (a < b) return -1;
            if (a > b) return 1;
            return 0;
        }
    }

    /**
     * Implies reverse order of indices.
     */
    private static class ReverseOrderedInputComparator extends OrderedInputComparator
    {
        @Override
        public int compare(int a, int b)
        {
            return -super.compare(a, b);
        }
    }

    enum DataDistribution
    {
        ordered, sawtooth, rand, stagger, plateau, shuffle
    }

    enum Algorithm
    {
        QUICKSORT, MERGESORT
    }

    /**
     * Test "certification" program as in Bentley and McIlroy's paper.
     */
    @Test
    public void testSortCertificationMergeSort()
    {
        sortCertification(Algorithm.MERGESORT);
    }

    /**
     * Test "certification" program as in Bentley and McIlroy's paper.
     */
    @Test
    public void testSortCertificationQuickSort()
    {
        sortCertification(Algorithm.QUICKSORT);
    }

    /**
     * Run a "sort certification" test.
     */
    static void sortCertification(Algorithm algorithm)
    {
        int [] n_values =
        {
            100, 1023, 1024, 1025, 1024 * 32
        };

        for (int n : n_values)
        {
            for (int m = 1; m < 2 * n; m *= 2)
            {
                for (DataDistribution dist : DataDistribution.values())
                {
                    int [] x = generate(dist, n, m);

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
    }

    /**
     * Generate <code>n</code>-length data set distributed according to <code>dist</code>.
     * 
     * @param m Step for sawtooth, stagger, plateau and shuffle.
     */
    static int [] generate(final DataDistribution dist, int n, int m)
    {
        // Start from a constant seed (repeatable tests).
        final Random rand = new Random(0x11223344);
        final int [] x = new int [n];
        for (int i = 0, j = 0, k = 1; i < n; i++)
        {
            switch (dist)
            {
                case ordered:
                    x[i] = i;
                    break;
                case sawtooth:
                    x[i] = i % m;
                    break;
                case rand:
                    x[i] = rand.nextInt() % m;
                    break;
                case stagger:
                    x[i] = (i * m + i) % n;
                    break;
                case plateau:
                    x[i] = Math.min(i, m);
                    break;
                case shuffle:
                    x[i] = (rand.nextInt() % m) != 0 ? (j += 2) : (k += 2);
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        return x;
    }

    static int [] sort(int [] x)
    {
        x = copy(x);
        Arrays.sort(x);
        return x;
    }

    static int [] dither(int [] x)
    {
        x = copy(x);
        for (int i = 0; i < x.length; i++)
            x[i] += i % 5;
        return x;
    }

    static int [] reverse(int [] x, int start, int end)
    {
        x = copy(x);
        for (int i = start, j = end - 1; i < j; i++, j--)
        {
            int v = x[i];
            x[i] = x[j];
            x[j] = v;
        }
        return x;
    }

    private static int [] copy(int [] x)
    {
        return (int []) x.clone();
    }

    /*
     * 
     */
    private static void testOn(Algorithm algo, int [] x, String testName)
    {
        final IndirectComparator c = new IndirectComparator.AscendingIntComparator(x);

        final int [] order;
        switch (algo)
        {
            case QUICKSORT:
                order = IndirectSort.sort(0, x.length, c);
                break;
            case MERGESORT:
                order = IndirectSort.mergesort(0, x.length, c);
                break;
            default:
                Assert.fail();
                throw new RuntimeException();
        }

        assertOrder(order, x.length, c);
    }

    /**
     * Empty and single-item input.
     */
    @Test
    public void testEmptyAndSingle()
    {
        final IndirectComparator comparator = new OrderedInputComparator();
        int [] qSortOrder = IndirectSort.sort(0, 0, comparator);
        int [] mSortOrder = IndirectSort.mergesort(0, 0, comparator);
        Assert.assertEquals(qSortOrder.length, 0);
        Assert.assertEquals(mSortOrder.length, 0);

        for (int i = 0; i < 1000; i++)
        {
            qSortOrder = IndirectSort.sort(0, i, comparator);
            mSortOrder = IndirectSort.mergesort(0, i, comparator);
            Assert.assertEquals(qSortOrder.length, i);
            Assert.assertEquals(mSortOrder.length, i);
            Assert.assertArrayEquals(qSortOrder, mSortOrder);
        }
    }

    /**
     * Large ordered input.
     */
    @Test
    public void testOrderedQuickSort()
    {
        final IndirectComparator comparator = new OrderedInputComparator();
        int [] order = IndirectSort.sort(0, DATA_LENGTH, comparator);
        assertOrder(order, DATA_LENGTH, comparator);
    }

    /**
     * Large ordered input.
     */
    @Test
    public void testOrderedMergeSort()
    {
        final IndirectComparator comparator = new OrderedInputComparator();
        int [] order = IndirectSort.mergesort(0, DATA_LENGTH, comparator);
        assertOrder(order, DATA_LENGTH, comparator);
    }

    /**
     * Large reversed input.
     */
    @Test
    public void testReversedQuickSort()
    {
        final IndirectComparator comparator = new ReverseOrderedInputComparator();
        final int [] order = IndirectSort.sort(0, DATA_LENGTH, comparator);
        assertOrder(order, DATA_LENGTH, comparator);
    }

    /**
     * Large reversed input.
     */
    @Test
    public void testReversedMergeSort()
    {
        final IndirectComparator comparator = new ReverseOrderedInputComparator();
        final int [] order = IndirectSort.mergesort(0, DATA_LENGTH, comparator);
        assertOrder(order, DATA_LENGTH, comparator);
    }

    /*
     * 
     */
    private static void assertOrder(final int [] order, int length,
        final IndirectComparator comparator)
    {
        for (int i = 1; i < length; i++)
        {
            Assert.assertTrue(comparator.compare(order[i - 1], order[i]) <= 0);
        }
    }

    /**
     * Randomized input, ascending int comparator.
     */
    @Test
    public void testAscInt()
    {
        final int maxSize = 500;
        final int rounds = 1000;
        final int vocabulary = 10;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final int [] input = generateRandom(maxSize, vocabulary, rnd);

            final IndirectComparator comparator = new IndirectComparator.AscendingIntComparator(
                input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            int [] order = IndirectSort.sort(start, length, comparator);
            assertOrder(order, length, comparator);

            order = IndirectSort.mergesort(start, length, comparator);
            assertOrder(order, length, comparator);
        }
    }

    /**
     * Randomized input, descending int comparator.
     */
    @Test
    public void testDescInt()
    {
        final int maxSize = 500;
        final int rounds = 1000;
        final int vocabulary = 10;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final int [] input = generateRandom(maxSize, vocabulary, rnd);

            final IndirectComparator comparator = new IndirectComparator.DescendingIntComparator(
                input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            int [] order = IndirectSort.sort(start, length, comparator);
            assertOrder(order, length, comparator);

            order = IndirectSort.mergesort(start, length, comparator);
            assertOrder(order, length, comparator);
        }
    }

    /**
     * Randomized input, ascending double comparator.
     */
    @Test
    public void testAscDouble()
    {
        final int maxSize = 1000;
        final int rounds = 1000;
        final Random rnd = new Random(0x11223344);

        for (int round = 0; round < rounds; round++)
        {
            final double [] input = generateRandom(maxSize, rnd);

            final IndirectComparator comparator = new IndirectComparator.AscendingDoubleComparator(
                input);

            final int start = rnd.nextInt(input.length - 1);
            final int length = (input.length - start);

            int [] order = IndirectSort.sort(start, length, comparator);
            assertOrder(order, length, comparator);

            order = IndirectSort.mergesort(start, length, comparator);
            assertOrder(order, length, comparator);
        }
    }

    /*
     * 
     */
    private int [] generateRandom(final int maxSize, final int vocabulary,
        final Random rnd)
    {
        final int [] input = new int [2 + rnd.nextInt(maxSize)];
        for (int i = 0; i < input.length; i++)
        {
            input[i] = vocabulary / 2 - rnd.nextInt(vocabulary);
        }
        return input;
    }

    /*
     * 
     */
    private double [] generateRandom(final int maxSize, final Random rnd)
    {
        final double [] input = new double [2 + rnd.nextInt(maxSize)];
        for (int i = 0; i < input.length; i++)
        {
            input[i] = rnd.nextGaussian();
        }
        return input;
    }
}
