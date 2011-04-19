package com.carrotsearch.hppc.procedures;

/**
 * A procedure that applies to <code>KType</code> objects.
 */
public interface KTypeProcedure<KType>
{
    public void apply(KType value);
}
