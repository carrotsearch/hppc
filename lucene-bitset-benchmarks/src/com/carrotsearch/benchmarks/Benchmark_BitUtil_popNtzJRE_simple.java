
package com.carrotsearch.benchmarks;

import org.apache.lucene.util.BitUtil_popNtzJRE_simple;
import org.junit.Test;

/**
 * Microbenchmarks of various implementations of ntz/pop in BitUtils.
 */
public class Benchmark_BitUtil_popNtzJRE_simple extends Benchmark
{
    @Test
    public void test_pop_array()
    {
        for (int i = 0; i < LOOPS; i++)
        {
            guard = BitUtil_popNtzJRE_simple.pop_array(memblock1, 0, memblock1.length);
        }
    }

    @Test
    public void test_pop_xor()
    {
        for (int i = 0; i < LOOPS; i++)
        {
            guard = BitUtil_popNtzJRE_simple.pop_xor(memblock1, memblock2, 0, memblock1.length);
        }
    }

    @Test
    public void test_pop_intersect()
    {
        for (int i = 0; i < LOOPS; i++)
        {
            guard = BitUtil_popNtzJRE_simple.pop_intersect(memblock1, memblock2, 0, memblock1.length);
        }
    }

    @Test
    public void test_pop_andnot()
    {
        for (int i = 0; i < LOOPS; i++)
        {
            guard = BitUtil_popNtzJRE_simple.pop_andnot(memblock1, memblock2, 0, memblock1.length);
        }
    }

    @Test
    public void test_pop_union()
    {
        for (int i = 0; i < LOOPS; i++)
        {
            guard = BitUtil_popNtzJRE_simple.pop_union(memblock1, memblock2, 0, memblock1.length);
        }
    }

    public static void main(String [] args) throws Throwable
    {
        org.junit.runner.JUnitCore.runClasses(
            Benchmark_BitUtil_popNtzJRE_simple.class
            );
    }
}
