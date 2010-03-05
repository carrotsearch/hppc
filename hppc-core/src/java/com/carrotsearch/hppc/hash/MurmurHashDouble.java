package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>double</code>s.
 */
public final class MurmurHashDouble extends HashFunctionDouble
{
    @Override
    public int hash(double k)
    {
        return MurmurHash2.hash(Double.doubleToLongBits(k));
    }
}