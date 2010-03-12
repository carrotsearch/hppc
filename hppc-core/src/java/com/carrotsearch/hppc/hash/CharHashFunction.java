package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>char</code> values.
 */
public class CharHashFunction
{
    public int hash(char key)
    {
        return key;
    }
}
