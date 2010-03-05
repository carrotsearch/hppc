package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>short</code>s.
 */
public final class MurmurHashShort extends HashFunctionShort
{
    @Override
    public int hash(short key)
    {
        return MurmurHash2.hash((int) key);
    }
}