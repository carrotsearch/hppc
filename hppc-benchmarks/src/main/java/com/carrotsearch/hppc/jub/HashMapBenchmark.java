package com.carrotsearch.hppc.jub;

import java.util.HashMap;
import java.util.Random;

import org.junit.*;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

/**
 * A micro-benchmark test case for comparing {@link HashMap} against
 * {@link ObjectObjectOpenHashMap}.
 */
public class HashMapBenchmark extends AbstractBenchmark
{
    private static HashMap<Integer, Integer> jre = new HashMap<Integer, Integer>();

    private static int COUNT = 1000000;
    private static Integer [] numbers = new Integer [COUNT];
    
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
        jre.clear();
    }

    /* */
    @Test
    public void testMultipleOperations() throws Exception
    {
        for (int r = 0; r < 2; r++)
        {
            for (int i = 0; i < numbers.length - r; i++)
            {
                if ((numbers[i] & 0x1) == 0)
                    jre.remove(numbers[i + r]);
                else
                    jre.put(numbers[i], numbers[i]);
            }
        }
        jre.clear();
    }
}
