package com.carrotsearch.hppc.cursors;

import com.carrotsearch.hppc.ObjectArrayList;

/**
 * A cursor over an array of <code>KTypes</code> or an {@link ObjectArrayList}.
 * 
 * @see ObjectArrayList#iterator()
 */
public final class ObjectCursor<KType>
{
    /**
     * The current index in the array being traversed.
     */
    public int index;
    
    /**
     * The current value at {@link #index}.
     */
    public KType value;
}
