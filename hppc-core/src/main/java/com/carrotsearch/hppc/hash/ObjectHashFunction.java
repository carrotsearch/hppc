package com.carrotsearch.hppc.hash;

/**
 * Default hash function for objects. <code>null</code> objects
 * have zero hash.  
 */
public class ObjectHashFunction<T>
{
    public int hash(T key)
    {
        return key == null ? 0 : key.hashCode();
    }
}
