package com.carrotsearch.hppc.hash;

/**
 * Default hash function for objects. <code>null</code> objects
 * have zero hash.  
 */
public class ObjectHashFunction
{
    public int hash(Object key)
    {
        return key == null ? 0 : key.hashCode();
    }
}
