package com.carrotsearch.hppc;

/**
 * A procedure that applies to <code>KType</code>, <code>VType</code> pairs.
 * 
 * @see ObjectObjectOpenHashMap#forEach
 */
public interface ObjectObjectProcedure<KType, VType>
{
    public void apply(KType key, VType value);
}
