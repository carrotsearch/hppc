package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>byte</code> values.
 */
public class HashFunctionByte
{
    public int hash(byte key)
    {
        return key;
    }
}
