package com.carrotsearch.hppc;

/**
 * A procedure that applies to <code>KType</code> objects.
 */
public interface ObjectProcedure<KType>
{
    public void apply(KType value);
}
