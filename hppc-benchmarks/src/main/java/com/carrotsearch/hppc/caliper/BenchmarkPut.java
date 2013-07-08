package com.carrotsearch.hppc.caliper;

import static com.carrotsearch.hppc.caliper.Util.prepareData;

import com.carrotsearch.hppc.XorShiftRandom;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Benchmark putting a given number of integers into a hashmap.
 */
public class BenchmarkPut extends SimpleBenchmark
{
    /* Prepare some test data */
    public int [] keys;

    public enum Distribution
    {
        RANDOM, LINEAR, HIGHBITS;
    }

    @Param
    public Distribution distribution;

    @Param
    public Implementations implementation;

    @Param(
    {
        "1000000"
    })
    public int size;

    /*
     * 
     */
    @Override
    protected void setUp() throws Exception
    {
        switch (distribution)
        {
            case RANDOM:
                keys = prepareData(size, new XorShiftRandom(0x11223344));
                break;
            case LINEAR:
                keys = prepareLinear(size);
                break;
            case HIGHBITS:
                keys = prepareHighbits(size);
                break;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Time the 'put' operation. 
     */
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

    /**
     * Linear increment by 1.
     */
    private int [] prepareLinear(int size)
    {
        int [] t = new int [size];
        for (int i = 0; i < size; i++)
            t[i] = i * 2;
        return t;
    }

    /**
     * Linear increments on 12 high bits first, then on lower bits. 
     */
    private int [] prepareHighbits(int size)
    {
        int [] t = new int [size];
        for (int i = 0; i < size; i++)
            t[i] = (i << (32 - 12)) | (i >>> 12);
        return t;
    }

    public static void main(String [] args)
    {
        Runner.main(BenchmarkPut.class, args);
    }
}