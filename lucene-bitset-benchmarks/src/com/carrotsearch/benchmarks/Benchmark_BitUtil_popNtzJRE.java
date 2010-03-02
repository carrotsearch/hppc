
package com.carrotsearch.benchmarks;

import org.apache.lucene.util.BitUtil_popNtzJRE;
import org.apache.lucene.util.OpenBitSet_popNtzJRE;
import org.junit.Test;

import com.carrotsearch.tests.benchmarks.BenchmarkOptions;

/**
 * Microbenchmarks of various implementations of ntz/pop in BitUtils.
 */
public class Benchmark_BitUtil_popNtzJRE extends Benchmark
{
    @Test
    public void test_pop_array()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_popNtzJRE.pop_array(memblock1, 0, memblock1.length);
    }

    @Test
    public void test_pop_xor()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_popNtzJRE.pop_xor(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_intersect()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_popNtzJRE.pop_intersect(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_andnot()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_popNtzJRE.pop_andnot(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_union()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil_popNtzJRE.pop_union(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    @BenchmarkOptions(warmupRounds = 2, benchmarkRounds = 5)
    public void test_ntz_iterator_int()
    {
        OpenBitSet_popNtzJRE bset = new OpenBitSet_popNtzJRE(memblock1, memblock1.length);
        int cnt = 0;
        for (int b = bset.nextSetBit(0); b >= 0; b = bset.nextSetBit(b + 1)) {
            cnt += b;
        }
        guard = cnt;
    }

    @Test
    @BenchmarkOptions(warmupRounds = 2, benchmarkRounds = 5)
    public void test_ntz_iterator_long()
    {
        OpenBitSet_popNtzJRE bset = new OpenBitSet_popNtzJRE(memblock1, memblock1.length);
        int cnt = 0;
        for (long b = bset.nextSetBit(0); b >= 0; b = bset.nextSetBit(b + 1)) {
            cnt += b;
        }
        guard = cnt;
    }
    
    public static void main(String [] args) throws Throwable
    {
        org.junit.runner.JUnitCore.runClasses(
            Benchmark_BitUtil_popNtzJRE.class
            );
    }
}
