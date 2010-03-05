package com.carrotsearch.hppc.hash;

/**
 * Austin Appleby's <code>MurmurHash2</code>. Simplified to process <code>int</code>s and
 * <code>long</code>s.
 * 
 * @see "http://sites.google.com/site/murmurhash/"
 */
final class MurmurHash2
{
    private static final int M = 0x5bd1e995;
    private static final int R = 24;
    private static final int SEED = 0xdeadbeef;

    private MurmurHash2()
    {
        // no instances.
    }

    /**
     * Hashes a 4-byte sequence (Java int).
     */
    static int hash(int k)
    {
        k *= M;
        k ^= k >>> R;
        k *= M;

        k ^= SEED * M;

        k ^= k >>> 13;
        k *= M;
        k ^= k >>> 15;

        return k;
    }

    /**
     * Hashes a 8-byte sequence (Java long).
     */
    static int hash(long v)
    {
        int k = (int) (v >>> 32);
        k *= M;
        k ^= k >>> R;
        k *= M;

        int h = SEED * M;
        h ^= k;

        k = (int) v;
        k *= M;
        k ^= k >>> R;
        k *= M;
        h *= M;
        h ^= k;
        
        h ^= h >>> 13;
        h *= M;
        h ^= h >>> 15;

        return h;
    }
}
