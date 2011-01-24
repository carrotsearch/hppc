package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>long</code> values.
 */
public class LongHashFunction
{
    /**
     * Consistent with {@link Long#hashCode()}. 
     */
    public int hash(long key)
    {
        return (int)(key  ^ (key >>> 32));
    }
}
