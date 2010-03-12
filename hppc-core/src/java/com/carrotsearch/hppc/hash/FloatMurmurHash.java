package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>float</code>s.
 */
public final class FloatMurmurHash extends FloatHashFunction
{
    @Override
    public int hash(float k)
    {
        return MurmurHash2.hash(Float.floatToIntBits(k));
    }
}