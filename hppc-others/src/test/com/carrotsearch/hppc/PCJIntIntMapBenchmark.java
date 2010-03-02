package com.carrotsearch.hppc;

import java.util.Random;

import org.junit.*;

import bak.pcj.map.IntKeyIntChainedHashMap;
import bak.pcj.map.IntKeyIntOpenHashMap;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

/**
 * A micro-benchmark test case for comparing PCJ's {@link IntKeyIntOpenHashMap}
 * and {@link IntKeyIntChainedHashMap} against HPPC
 * {@link IntIntOpenHashMap}.
 */
@BenchmarkOptions(callgc = false, benchmarkRounds = 20)
public class PCJIntIntMapBenchmark extends AbstractBenchmark
{
    private static IntKeyIntOpenHashMap pcj1 = new IntKeyIntOpenHashMap();
    private static IntKeyIntChainedHashMap pcj2 = new IntKeyIntChainedHashMap();
    private static IntIntOpenHashMap hppc = new IntIntOpenHashMap();

    private static int COUNT = 1000000;
    private static int [] numbers = new int [COUNT];

    /* */
    @BeforeClass
    public static void createTestSequence()
    {
        final Random rnd = new Random(0x11223344);
        for (int i = 0; i < COUNT; i++)
            numbers[i] = rnd.nextInt();
    }

    /* */
    @Before
    public void before()
    {
        pcj1.clear();
        pcj2.clear();
        hppc.clear();
    }

    /* */
    @Test
    public void testPcjBug() throws Exception
    {    
        IntKeyIntOpenHashMap m = new IntKeyIntOpenHashMap();

        m.put(0, 1);
        m.put(99, 2);
        m.put(198, 3);
        org.junit.Assert.assertEquals(3, m.size());
        m.remove(99);
        org.junit.Assert.assertEquals(2, m.size());
        m.put(198, 4); // Should be in the map already!
        org.junit.Assert.assertEquals(2, m.size());
    }
    
    /* */
    @Test
    public void testHPPC() throws Exception
    {
        for (int r = 0; r < 2; r++)
        {
            for (int i = 0; i < numbers.length - r; i++)
            {
                if ((numbers[i] & 0x1) == 0)
                    hppc.remove(numbers[i + r]);
                else
                    hppc.put(numbers[i], numbers[i]);
            }
        }
        hppc.clear();
    }

    /* */
    @Test
    public void testPCJ_linked() throws Exception
    {
        for (int r = 0; r < 2; r++)
        {
            for (int i = 0; i < numbers.length - r; i++)
            {
                if ((numbers[i] & 0x1) == 0)
                    pcj2.remove(numbers[i + r]);
                else
                    pcj2.put(numbers[i], numbers[i]);
            }
        }
        pcj2.clear();
    }

    /* */
    @Test
    public void testPCJ_open() throws Exception
    {
        for (int r = 0; r < 2; r++)
        {
            for (int i = 0; i < numbers.length - r; i++)
            {
                if ((numbers[i] & 0x1) == 0)
                    pcj1.remove(numbers[i + r]);
                else
                    pcj1.put(numbers[i], numbers[i]);
            }
        }
        pcj1.clear();
    }
}
