package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>int</code>s.
 */
public final class IntMurmurHash extends IntHashFunction
{
    @Override
    public int hash(int k)
    {
        return MurmurHash2.hash(k);
    }
}