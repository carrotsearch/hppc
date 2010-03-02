package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>float</code> values.
 */
public class HashFunctionFloat
{
    public int hash(float key)
    {
        return Float.floatToIntBits(key);
    }
}
