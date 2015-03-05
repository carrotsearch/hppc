package com.carrotsearch.hppc.procedures;

import com.google.common.base.Predicate;

/**
 * An adapter between Guava's {@link Predicate} and HPPC's {@link ObjectProcedure}.
 */
public class GuavaProcedureAdapter<KType> implements ObjectProcedure<KType>
{
    private final Predicate<? super KType> predicate;

    public GuavaProcedureAdapter(com.google.common.base.Predicate<? super KType> predicate)
    {
        this.predicate = predicate;
    }

    public void apply(KType value)
    {
        predicate.apply(value);
    }
}
