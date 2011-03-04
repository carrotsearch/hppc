package com.carrotsearch.hppc.hash;

/**
 * Hash routines for primitive types. The implementation is based on the finalization step
 * from Austin Appleby's <code>MurmurHash3</code>.
 * 
 * @see "http://sites.google.com/site/murmurhash/"
 */
public final class MurmurHash3
{
    /** MurmurHash for bytes (could be tabularized in fact...). */
    public final static class ByteMurmurHash extends ByteHashFunction
    {
        @Override
        public int hash(byte k)
        {
            return MurmurHash3.hash((int) k);
        }
    }

    /** MurmurHash for chars. */
    public final static class CharMurmurHash extends CharHashFunction
    {
        @Override
        public int hash(char k)
        {
            return MurmurHash3.hash((int) k);
        }
    }

    /** MurmurHash for shorts. */
    public final static class ShortMurmurHash extends ShortHashFunction
    {
        @Override
        public int hash(short k)
        {
            return MurmurHash3.hash((int) k);
        }
    }

    /**
     * Murmur hash for <code>float</code>s.
     */
    public final static class FloatMurmurHash extends FloatHashFunction
    {
        @Override
        public int hash(float k)
        {
            return MurmurHash3.hash(Float.floatToIntBits(k));
        }
    }

    /**
     * Murmur hash for <code>int</code>s.
     */
    public final static class IntMurmurHash extends IntHashFunction
    {
        @Override
        public int hash(int k)
        {
            return MurmurHash3.hash(k);
        }
    }

    /** Murmur hash for <code>double</code>s. */
    public final static class DoubleMurmurHash extends DoubleHashFunction
    {
        @Override
        public int hash(double k)
        {
            return (int) MurmurHash3.hash(Double.doubleToLongBits(k));
        }
    }

    /** Murmur hash for <code>long</code>s. */
    public final static class LongMurmurHash extends LongHashFunction
    {
        @Override
        public int hash(long k)
        {
            return (int) MurmurHash3.hash(k);
        }
    }

    /** Murmur hash for <code>Object</code>s. */
    public final static class ObjectMurmurHash extends ObjectHashFunction<Object>
    {
        @Override
        public int hash(Object key)
        {
            return MurmurHash3.hash(key == null ? 0 : key.hashCode());
        }
    }

    private MurmurHash3()
    {
        // no instances.
    }

    /**
     * Hashes a 4-byte sequence (Java int).
     */
    public static int hash(int k)
    {
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;
        return k;
    }

    /**
     * Hashes an 8-byte sequence (Java long).
     */
    public static long hash(long k)
    {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;

        return k;
    }
}
