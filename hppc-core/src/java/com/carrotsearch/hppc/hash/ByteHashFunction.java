package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>byte</code> values.
 */
public class ByteHashFunction
{
    public int hash(byte key)
    {
        return key;
    }
}
