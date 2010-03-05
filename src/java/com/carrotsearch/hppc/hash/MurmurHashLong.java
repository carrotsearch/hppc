package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>long</code>s.
 */
public final class MurmurHashLong extends HashFunctionLong
{
    @Override
    public int hash(long k)
    {
        return MurmurHash2.hash(k);
    }
}