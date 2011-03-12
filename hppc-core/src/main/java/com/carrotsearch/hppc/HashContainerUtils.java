package com.carrotsearch.hppc;

import com.carrotsearch.hppc.hash.MurmurHash3;

/**
 * Utilities and constants for hash containers.
 */
public final class HashContainerUtils
{
    public static int rehash(Object o) { return o == null ? 0 : MurmurHash3.hash(o.hashCode()); }
    public static int rehash(byte v) { return MurmurHash3.hash(v); }
    public static int rehash(short v) { return MurmurHash3.hash(v); }
    public static int rehash(int v) { return MurmurHash3.hash(v); }
    public static int rehash(long v) { return (int) MurmurHash3.hash(v); }
    public static int rehash(char v) { return MurmurHash3.hash(v); }
    public static int rehash(float v) { return MurmurHash3.hash(Float.floatToIntBits(v)); }
    public static int rehash(double v) { return (int) MurmurHash3.hash(Double.doubleToLongBits(v)); }
}
