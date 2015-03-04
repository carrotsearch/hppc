package com.carrotsearch.hppc.hash;

/**
 * Quickly mixes the bits of integers.
 * <p>Those methods mixes the bits of the argument by multiplying by the golden ratio and
 * xorshifting the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
 * it has slightly worse behaviour than {@link MurmurHash3} (in open-addressing hash tables the average number of probes
 * is slightly larger), but it's much faster.
 */
public final class PhiMix
{
    /**
     *  2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    private static final int INT_PHI = 0x9E3779B9;

    /**
     * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    private static final long LONG_PHI = 0x9E3779B97F4A7C15L;

    /**
     * = hash(0)
     */
    public static final int HASH_0 = 0;

    /**
     * = hash(1)
     */
    public static final int HASH_1 = 1640556430;

    private PhiMix()
    {
        // no instances.
    }

    /**
     * Hashes a 4-byte sequence (Java int).
     * @param x an integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     */
    public static int hash(final int x) {
        final int h = x * PhiMix.INT_PHI;
        return h ^ (h >> 16);
    }

    /**
     * Hashes an 8-byte sequence (Java long).
     * @param x a long integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     */
    public static long hash(final long x) {
        long h = x * PhiMix.LONG_PHI;
        h ^= h >> 32;
        return h ^ (h >> 16);
    }
}
