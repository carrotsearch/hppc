
package com.carrotsearch.benchmarks;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;
import org.junit.Test;

import com.carrotsearch.tests.benchmarks.BenchmarkOptions;

/**
 * Microbenchmarks various implementations of ntz/pop in BitUtils.
 */
public class Benchmark_BitUtil_trunk extends Benchmark
{
    @Test
    public void test_pop_array()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil.pop_array(memblock1, 0, memblock1.length);
    }

    @Test
    public void test_pop_xor()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil.pop_xor(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_intersect()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil.pop_intersect(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_andnot()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil.pop_andnot(memblock1, memblock2, 0, memblock1.length);
    }

    @Test
    public void test_pop_union()
    {
        for (int i = 0; i < LOOPS; i++)
            guard = BitUtil.pop_union(memblock1, memblock2, 0, memblock1.length);
    }
    
    @Test
    @BenchmarkOptions(warmupRounds = 2, benchmarkRounds = 5)
    public void test_ntz_iterator_int()
    {
        OpenBitSet bset = new OpenBitSet(memblock1, memblock1.length);
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
        OpenBitSet bset = new OpenBitSet(memblock1, memblock1.length);
        int cnt = 0;
        for (long b = bset.nextSetBit(0); b >= 0; b = bset.nextSetBit(b + 1)) {
            cnt += b;
        }
        guard = cnt;
    }
}
