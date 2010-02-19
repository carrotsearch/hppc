package com.carrotsearch.hppc.procedures;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * A procedure that applies to <code>KType</code>, <code>VType</code> pairs.
 * 
 * @see ObjectObjectOpenHashMap#forEach
 */
public interface ObjectObjectProcedure<KType, VType>
{
    public void apply(KType key, VType value);
}
