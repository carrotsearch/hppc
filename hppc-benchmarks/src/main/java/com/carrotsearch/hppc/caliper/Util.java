package com.carrotsearch.hppc.caliper;

import java.util.Random;

public class Util
{
    /**
     * Prepare pseudo-random data from a fixed seed.
     */
    public static int [] prepareData(int len, Random rnd)
    {
        int [] randomData = new int [len];
        for (int i = 0; i < len; i++)
            randomData[i] = rnd.nextInt();

        return randomData;
    }

    public static int [] shuffle(int [] array, Random rnd)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            int pos = rnd.nextInt(i + 1);
            int t = array[pos];
            array[pos] = array[i];
            array[i] = t;
        }
        return array;
    }
}
