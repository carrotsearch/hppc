package com.carrotsearch.hppc.caliper;

import static com.carrotsearch.hppc.caliper.Util.prepareData;
import static com.carrotsearch.hppc.caliper.Util.shuffle;

import java.util.Random;

import org.apache.mahout.math.Arrays;

import com.google.caliper.*;

/**
 * Create a large map of int keys, remove a fraction of the keys and query with half/half keys
 * and a some random values.
 */
public class BenchmarkContainsWithRemoved extends SimpleBenchmark
{
    /* Prepare some test data */
    public int [] keys;
    public int [] queryKeys;

    @Param(
    {
        "0", "0.25", "0.5", "0.75", "1"
    })
    public double removedKeys;

    @Param
    public Implementations implementation;
    public MapImplementation<?> impl;

    @Param(
    {
        "2000000"
    })
    public int size;

    @Override
    protected void setUp() throws Exception
    {
        Random rnd = new Random(0x11223344);

        // Our tested implementation.
        impl = implementation.getInstance();

        // Random keys
        keys = prepareData(size, rnd);

        // Half keys, half random. Shuffle order.
        queryKeys = Arrays.copyOf(keys, keys.length);
        for (int i = 0; i < queryKeys.length / 2; i++)
            queryKeys[i] = rnd.nextInt();

        shuffle(queryKeys, rnd);

        // Fill with random keys.
        for (int i = 0; i < keys.length; i++)
            impl.put(keys[i], 0);

        // Shuffle keys and remove a fraction of them.
        int [] randomized = shuffle(Arrays.copyOf(keys, keys.length), rnd);
        int removeKeys = (int) (removedKeys * keys.length);
        for (int i = 0; removeKeys > 0; removeKeys--, i++)
        {
            impl.remove(randomized[i]);
        }
    }

    public int timeContains(int reps)
    {
        int count = 0;
        for (int i = 0; i < reps; i++)
        {
            count += impl.containKeys(keys);
        }
        return count;
    }

    @Override
    protected void tearDown() throws Exception
    {
        impl = null;
    }

    public static void main(String [] args)
    {
        Runner.main(BenchmarkContainsWithRemoved.class, args);
    }
}