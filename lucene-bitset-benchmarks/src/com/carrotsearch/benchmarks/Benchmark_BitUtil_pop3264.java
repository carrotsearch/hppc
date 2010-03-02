
package com.carrotsearch.benchmarks;

import org.apache.lucene.util.BitUtil_pop3264;
import org.junit.Test;

/**
 * Microbenchmarks of various implementations of ntz/pop in BitUtils.
 */
public class Benchmark_BitUtil_pop3264 extends Benchmark
{
    @Test
    public void test_pop_array()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_pop3264.pop_array(memblock1, 0, memblock1.length);
    }

    @Test
    public void test_pop_xor()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_pop3264.pop_xor(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_intersect()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_pop3264.pop_intersect(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_andnot()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_pop3264.pop_andnot(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_union()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_pop3264.pop_union(memblock1, memblock2, 0, memblock1.length);
    }
}
