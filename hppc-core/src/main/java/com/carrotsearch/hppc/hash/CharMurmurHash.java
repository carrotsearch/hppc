package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>char</code>s.
 */
public final class CharMurmurHash extends CharHashFunction
{
    @Override
    public int hash(char k)
    {
        return MurmurHash2.hash((int) k);
    }
}