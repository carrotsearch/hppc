package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>short</code>s.
 */
public final class ShortMurmurHash extends ShortHashFunction
{
    @Override
    public int hash(short key)
    {
        return MurmurHash2.hash((int) key);
    }
}