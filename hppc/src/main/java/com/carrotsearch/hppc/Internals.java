package com.carrotsearch.hppc;

import com.carrotsearch.hppc.hash.MurmurHash3;

/**
 * Internal utilities.
 */
final class Internals
{
    static int rehash(Object o, int p) { return o == null ? 0 : MurmurHash3.hash(o.hashCode() ^ p); }
    static int rehash(byte v, int p)   { return MurmurHash3.hash(v ^ p); }
    static int rehash(short v, int p)  { return MurmurHash3.hash(v ^ p); }
    static int rehash(int v, int p)    { return MurmurHash3.hash(v ^ p); }
    static int rehash(long v, int p)   { return (int) MurmurHash3.hash(v ^ p); }
    static int rehash(char v, int p)   { return MurmurHash3.hash(v ^ p); }
    static int rehash(float v, int p)  { return MurmurHash3.hash(Float.floatToIntBits(v) ^ p); }
    static int rehash(double v, int p) { return (int) MurmurHash3.hash(Double.doubleToLongBits(v) ^ p); }

    static int rehash(Object o) { return o == null ? 0 : MurmurHash3.hash(o.hashCode()); }
    static int rehash(byte v)   { return MurmurHash3.hash(v); }
    static int rehash(short v)  { return MurmurHash3.hash(v); }
    static int rehash(int v)    { return MurmurHash3.hash(v); }
    static int rehash(long v)   { return (int) MurmurHash3.hash(v); }
    static int rehash(char v)   { return MurmurHash3.hash(v); }
    static int rehash(float v)  { return MurmurHash3.hash(Float.floatToIntBits(v)); }
    static int rehash(double v) { return (int) MurmurHash3.hash(Double.doubleToLongBits(v)); }

    /**
     * Create and return an array of template objects (<code>Object</code>s in the generic
     * version, corresponding key-primitive type in the generated version).
     * 
     * @param arraySize The size of the array to return.
     */
    @SuppressWarnings("unchecked")
    static <T> T newArray(int arraySize)
    {
        return (T) new Object [arraySize];
    }    
}
