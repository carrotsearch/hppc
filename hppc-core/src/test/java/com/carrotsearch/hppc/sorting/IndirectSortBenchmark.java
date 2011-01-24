
package com.carrotsearch.hppc.sorting;

import static com.carrotsearch.hppc.sorting.IndirectSortTest.*;

import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.hppc.sorting.IndirectSortTest.DataDistribution;

/**
 * {@link IndirectSort} benchmarks for various input data distributions.
 */
public class IndirectSortBenchmark
{
    /**
     * Test performance of the sorting routine. Requires large heap (integers take 4x
     * the number of elements + the returned order array) and takes a long time.
     */
    @Test @Ignore
    public void testPerformance()
    {
        final int MB = 1024 * 1024;
        int [] n_values = { 10 * MB, 100 * MB };

        for (int n : n_values)
        {
            for (DataDistribution dist : DataDistribution.values())
            {
                final int [] m_values = 
                    (dist == DataDistribution.ordered 
                        ? new int [] {1} : new int [] { 1, 10, n/2, n });

                for (int m : m_values)
                {
                    int [] x = generate(dist, n, m);

                    String testName = dist + "-" + n + "-" + m;
                    ptestOn(x, testName + "-normal");
                    ptestOn(reverse(x, 0, n), testName + "-reversed");
                    ptestOn(reverse(x, 0, n/2), testName + "-reversed_front");
                    ptestOn(reverse(x, n/2, n), testName + "-reversed_back");
                    ptestOn(sort(x), testName + "-sorted");
                    ptestOn(dither(x), testName + "-dither");
                }
            }
        }
    }

    /*
     * 
     */
    private static void ptestOn(int [] x, String testName)
    {
        final int rounds = 5;
        for (int i = 0; i < rounds; i++)
        {
            final long start = System.currentTimeMillis();
            final IndirectComparator c = new IndirectComparator.AscendingIntComparator(x);
            IndirectSort.sort(0, x.length, c);
            final long time = System.currentTimeMillis() - start;

            System.out.println(testName + " : " + time);
        }
    }
}
