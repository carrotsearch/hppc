package com.carrotsearch.hppc.caliper;

import static com.carrotsearch.hppc.caliper.Util.prepareData;

import java.util.Random;

import com.google.caliper.*;

/**
 * Benchmark putting a given number of integers into a hashmap.
 */
public class BenchmarkPut extends SimpleBenchmark
{
    /* Prepare some test data */
    public int [] keys;

    public enum Distribution
    {
        RANDOM, LINEAR;
    }

    @Param
    public Distribution distribution;

    @Param
    public Implementations implementation;

    @Param(
    {
        "100000", "1000000", "2000000", "4000000"
    })
    public int size;

    @Override
    protected void setUp() throws Exception
    {
        switch (distribution)
        {
            case RANDOM:
                keys = prepareData(size, new Random(0x11223344));
                break;
            case LINEAR:
                keys = prepareLinear(size);
                break;
            default:
                throw new RuntimeException();
        }
    }

    public int timePut(int reps)
    {
        int count = 0;
        for (int i = 0; i < reps; i++)
        {
            MapImplementation<?> impl = implementation.getInstance();
            count += impl.putAll(keys, keys);
        }
        return count;
    }

    private int [] prepareLinear(int size)
    {
        int [] t = new int [size];
        for (int i = 0; i < size; i++)
            t[i] = i * 2;
        return t;
    }

    public static void main(String [] args)
    {
        Runner.main(BenchmarkPut.class, "--timeUnit", "ms"
        // "-Dsize=1000000",
        // "-DremovedKeys=0.8,0.9,0.99,1",
        // "-Dimplementation=FASTUTIL"
            );
    }
}