package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>char</code>s.
 */
public final class MurmurHashChar extends HashFunctionChar
{
    @Override
    public int hash(char k)
    {
        return MurmurHash2.hash((int) k);
    }
}