package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.predicates.ObjectPredicate;


/**
 * A generic set of operations on a collection of <code>KType</code>s. A given collection
 * may implement a subset of these operations and throw a
 * {@link UnsupportedOperationException} for unsupported operations.
 */
public interface ObjectCollection<KType> extends ObjectContainer<KType>
{
    /**
     * Add an element to the collection.
     * 
     * @param e Element whose presence in this collection is to be ensured.
     * @return <tt>1</tt> if this collection changed as a result of the call,
     * zero otherwise (the number of added elements to the collection).
     */
    public int add(KType e);

    /**
     * Removes all occurrences of <code>e</code> from this collection.
     * 
     * @param e Element to be removed from this collection, if present.
     * @return The number of removed elements as a result of this call.
     */
    public int removeAllOccurrences(KType e);

    /**
     * Adds all elements from a given container to this collection.
     * @return The number of added elements.
     */
    public int addAll(Iterable<? extends ObjectCursor<? extends KType>> c);

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

    /**
     * Removes all elements from this collection.
     */
    public void clear();

    /**
     * Copies all elements of this collection to an array.  
     */
    public KType [] toArray();
}
