package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>int</code>s.
 */
public final class MurmurHashInt extends HashFunctionInt
{
    static final int m = 0x5bd1e995;
    static final int r = 24;

    // The seed could be in fact eliminated, but it is present in the original algorithm.  
    static final int seed = 0xdeadbeef;

    @Override
    public int hash(int k)
    {
        k *= m;
        k ^= k >>> r;
        k *= m;

        k ^= seed * m;

        k ^= k >>> 13;
        k *= m;
        k ^= k >>> 15;

        return k;
    }
}