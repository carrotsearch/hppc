package com.carrotsearch.hppc.cursors;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * A cursor over map entries (keys and values).
 * 
 * @see ObjectObjectOpenHashMap#iterator()
 */
public final class ObjectObjectCursor<KType, VType>
{
    /**
     * The current index in the internal buffers of the map 
     * being traversed.
     */
    public int index;

    /**
     * The current key.
     */
    public KType key;

    /**
     * The current value.
     */
    public VType value;
}
