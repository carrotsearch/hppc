package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>byte</code>s.
 */
public final class MurmurHashByte extends HashFunctionByte
{
    @Override
    public int hash(byte k)
    {
        return MurmurHash2.hash((int) k);
    }
}