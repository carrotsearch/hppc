
package com.carrotsearch.hppc.sorting;

import static com.carrotsearch.hppc.sorting.IndirectSortTest.generate;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.sorting.IndirectSortTest.DataDistribution;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

/**
 * A single-shot pass at random data.
 */
@BenchmarkOptions(benchmarkRounds = 4, warmupRounds = 1, callgc = false)
public class IndirectSortBenchmark2 extends AbstractBenchmark
{
    private final static ArrayList<int[]> data = new ArrayList<int[]>(); 

    @BeforeClass
    public static void prepare()
    {
        final int MB = 1024 * 1024;
        int [] n_values = { 1 * MB };

        for (int n : n_values)
        {
            for (DataDistribution dist : DataDistribution.values())
            {
                final int [] m_values = 
                    (dist == DataDistribution.ordered 
                        ? new int [] {1} : new int [] { 1, 10, n/2, n });

                for (int m : m_values)
                {
                    data.add(generate(dist, n, m));
                }
            }
        }
    }

    /**
     * 
     */
    @Test
    public void testMergeSort()
    {
        for (int [] x : data)
        {
            final IndirectComparator c = new IndirectComparator.AscendingIntComparator(x);
            IndirectSort.mergesort(0, x.length, c);
        }
    }
    
    /**
     * 
     */
    @Test
    public void testQuickSort()
    {
        for (int [] x : data)
        {
            final IndirectComparator c = new IndirectComparator.AscendingIntComparator(x);
            IndirectSort.sort(0, x.length, c);
        }
    }    
}
