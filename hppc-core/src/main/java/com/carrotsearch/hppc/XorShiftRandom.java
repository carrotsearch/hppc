package com.carrotsearch.hppc;

import java.util.Random;

/**
 * XorShift pseudo random number generator. This class is not thread-safe and should be
 * used from a single thread only.
 * 
 * @see "http://en.wikipedia.org/wiki/Xorshift"
 * @see "http://www.jstatsoft.org/v08/i14/paper"
 * @see "http://www.javamex.com/tutorials/random_numbers/xorshift.shtml"
 */
@SuppressWarnings("serial")
public class XorShiftRandom extends Random
{
    private long x;

    public XorShiftRandom()
    {
        this(System.nanoTime());
    }

    public XorShiftRandom(long seed)
    {
        this.setSeed(seed);
    }

    @Override
    public long nextLong()
    {
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x;
    }

    @Override
    protected int next(int bits)
    {
        return ((int) nextLong() >>> (48 - bits));
    }

    @Override
    public void setSeed(long seed)
    {
        this.x = seed;
    }
}
