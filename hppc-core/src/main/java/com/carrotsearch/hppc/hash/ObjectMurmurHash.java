package com.carrotsearch.hppc.hash;

/**
 * Murmur hash for <code>Object</code>s.
 */
public final class ObjectMurmurHash extends ObjectHashFunction<Object>
{
    @Override
    public int hash(Object key)
    {
        return MurmurHash2.hash(key == null ? 0 : key.hashCode());
    }
}