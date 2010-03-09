package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * A generic container holding <code>KType</code>s.
 */
public interface ObjectContainer<KType> extends Iterable<ObjectCursor<KType>>
{
    /**
     * Returns an iterator to a cursor traversing the collection. The order of traversal
     * is not defined. More than one cursor may be active at a time. The behavior of
     * iterators is undefined if structural changes are made to the underlying collection.
     */
    public Iterator<ObjectCursor<KType>> iterator();

    /**
     * Return the current number of elements in this container. 
     * Guaranteed to take <code>O(1)</code> time.
     */
    public int size();

    /**
     * Lookup a given element in the container. This operation has no speed
     * guarantees (may be linear with respect to the size of this container).
     * 
     * @return Returns <code>true</code> if this container has an element
     * equal to <code>e</code>.
     */
    public boolean contains(KType e);

    /**
     * Shortcut for <code>size() == 0</code>.
     */
    public boolean isEmpty();

    /**
     * Copies all elements from this container to an array. The returned array is always a copy,
     * regardless of the storage used by the container.
     */
    public KType [] toArray();
}
