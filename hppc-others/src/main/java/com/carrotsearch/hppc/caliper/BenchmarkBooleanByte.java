package com.carrotsearch.hppc.caliper;

import it.unimi.dsi.fastutil.HashCommon;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Benchmark boolean vs. byte array access.
 */
public class BenchmarkBooleanByte extends SimpleBenchmark
{
    public byte [] byteArray;
    public boolean [] booleanArray;

    @Param(
    {
        "5000000"
    })
    public int size;

    /*
     * 
     */
    @Override
    protected void setUp() throws Exception
    {
        size = HashCommon.nextPowerOfTwo(size);
        booleanArray = new boolean [size + 1];
        byteArray = new byte [size + 1];
    }

    /**
     * Time boolean array.
     */
    public int timeBoolean(int reps)
    {
        final int size = this.size;
        for (int i = 0, j = 0; i < reps; i++)
        {
            for (int k = 0; k < size * 2; k++)
            {
                j = (j + /* prime */30727) & (size - 1);
                booleanArray[j] = booleanArray[j + 1];
            }
        }

        int count = 10;
        for (int i = 0; i < 10; i++)
            if (booleanArray[i]) count++;

        return count;
    }

    /**
     * Time byte array.
     */
    public int timeByte(int reps)
    {
        final int size = this.size;
        for (int i = 0, j = 0; i < reps; i++)
        {
            for (int k = 0; k < size * 2; k++)
            {
                j = (j + /* prime */30727) & (size - 1);
                byteArray[j] = byteArray[j + 1];
            }
        }

        int count = 10;
        for (int i = 0; i < 10; i++)
            if (byteArray[i] == 0) count++;

        return count;
    }


    public static void main(String [] args)
    {
        Runner.main(BenchmarkBooleanByte.class, args);
    }
}