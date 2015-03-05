package com.carrotsearch.hppc.predicates;

import com.google.common.base.Predicate;

/**
 * An adapter between Guava's {@link Predicate} and HPPC's {@link ObjectPredicate}.
 */
public class GuavaPredicateAdapter<KType> implements ObjectPredicate<KType>
{
    private final Predicate<? super KType> predicate;

    public GuavaPredicateAdapter(com.google.common.base.Predicate<? super KType> predicate)
    {
        this.predicate = predicate;
    }

    public boolean apply(KType value)
    {
        return predicate.apply(value);
    }
}
