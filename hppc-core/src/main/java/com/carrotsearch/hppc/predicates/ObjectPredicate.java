package com.carrotsearch.hppc.predicates;

/**
 * A predicate that applies to <code>KType</code> objects.
 */
public interface ObjectPredicate<KType>
{
    public boolean apply(KType value);
}
