package com.carrotsearch.hppc;

import java.util.Arrays;
import java.util.Random;

import org.junit.*;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

/**
 * Microbenchmarks for ntz and pop methods (BitUtils) from Lucene.
 */
@BenchmarkOptions(warmupRounds = 5, benchmarkRounds = 15, callgc = false)
@Ignore
public class NtzPopBenchmark extends AbstractBenchmark
{
    /** how many bits for ntz/pop testing? */
    private static long BIT_COUNT = (/* MB */ 200 * 1024 * 1024) / 8 * 64;

    /* */
    private static BitSet random = new BitSet(BIT_COUNT);

    /* */
    private static BitSet single = new BitSet(BIT_COUNT);

    /* */
    public static volatile int guard;

    /* */
    @BeforeClass
    public static void before()
    {
        // Initialize with random data.
        final Random rnd = new Random(0x11223344);
        for (int i = random.bits.length; --i >= 0;)
        {
            random.bits[i] = rnd.nextLong();
        }

        // Initialize so that every long has a single bit.
        long value = 1;
        for (int i = single.bits.length; --i >= 0;)
        {
            single.bits[i] = value;
            value = (value >>> 1) | (value << 63);
        }

        System.out.print("# ");
        for (String key : Arrays.asList(
            "java.version",
            "java.vm.name",
            "java.vm.version",
            "java.vm.vendor"
        ))
        {
            System.out.print(System.getProperty(key) + ", ");
        }
        System.out.println();
    }

    /* random data */

    @Test
    public void test_POP2_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.pop(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_POP_JDK_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += Long.bitCount(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_POP_BitUtil_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.pop_orig(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ_JDK_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += Long.numberOfTrailingZeros(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ_BitUtil_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ2_BitUtil_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz2(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ3_BitUtil_random() throws Exception
    {
        final long [] longs = NtzPopBenchmark.random.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz3(longs[i]);
        }
        dontRemove(cnt);
    }

    /* single walking bit. */

    @Test
    public void test_POP2_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.pop(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_POP_JDK_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += Long.bitCount(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_POP_BitUtil_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.pop_orig(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ_JDK_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += Long.numberOfTrailingZeros(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ_BitUtil_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ2_BitUtil_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz2(longs[i]);
        }
        dontRemove(cnt);
    }

    @Test
    public void test_NTZ3_BitUtil_single() throws Exception
    {
        final long [] longs = NtzPopBenchmark.single.bits;
        int cnt = 0;
        for (int i = longs.length; --i >= 0;)
        {
            cnt += BitUtil.ntz3(longs[i]);
        }
        dontRemove(cnt);
    }

    private void dontRemove(int cnt)
    {
        guard = cnt;
    }

    public static void main(String [] args) throws Throwable
    {
        org.junit.runner.JUnitCore.runClasses(NtzPopBenchmark.class);
    }
}
