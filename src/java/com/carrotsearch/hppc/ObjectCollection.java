package com.carrotsearch.hppc;

import com.carrotsearch.hppc.predicates.ObjectPredicate;

/**
 * A collection allows basic, efficient operations on sets of elements 
 * (difference and intersection).
 */
public interface ObjectCollection<KType> extends ObjectContainer<KType>
{
    /**
     * Removes all occurrences of <code>e</code> from this collection.
     * 
     * @param e Element to be removed from this collection, if present.
     * @return The number of removed elements as a result of this call.
     */
    public int removeAllOccurrences(KType e);

    /**
     * Removes all elements in this collection that are present
     * in <code>c</code>. Runs in time proportional to the number
     * of elements in this collection. Equivalent of sets difference.
     * 
     * @return Returns the number of removed elements.
     */
    public int removeAll(ObjectLookupContainer<? extends KType> c);

    /**
     * Removes all elements in this collection for which the
     * given predicate returns <code>true</code>.
     */
    public int removeAll(ObjectPredicate<? super KType> predicate);

    /**
     * Keeps all elements in this collection that are present
     * in <code>c</code>. Runs in time proportional to the number
     * of elements in this collection. Equivalent of sets intersection.
     * 
     * @return Returns the number of removed elements.
     */
    public int retainAll(ObjectLookupContainer<? extends KType> c);

    /**
     * Keeps all elements in this collection for which the
     * given predicate returns <code>true</code>.
     */
    public int retainAll(ObjectPredicate<? super KType> predicate);
}
