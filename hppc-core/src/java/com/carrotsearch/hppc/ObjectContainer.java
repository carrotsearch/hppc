package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.predicates.ObjectPredicate;
import com.carrotsearch.hppc.procedures.ObjectProcedure;

/**
 * A generic container holding <code>KType</code>s. An overview of interface relationships
 * is given in the figure below:
 * 
 * <p><img src="doc-files/interfaces.png"
 *      alt="HPPC interfaces" /></p>
 */
public interface ObjectContainer<KType> extends Iterable<ObjectCursor<KType>>
{
    /**
     * Returns an iterator to a cursor traversing the collection. The order of traversal
     * is not defined. More than one cursor may be active at a time. The behavior of
     * iterators is undefined if structural changes are made to the underlying collection.
     * 
     * <p>The iterator is implemented as a
     * cursor and it returns <b>the same cursor instance</b> on every call to
     * {@link Iterator#next()} (to avoid boxing of primitive types). To read the current
     * list's value (or index in the list) use the cursor's public fields. An example is
     * shown below.</p>
     * 
     * <pre>
     * for (IntCursor c : intList) {
     *   System.out.println("index=" + c.index + " value=" + c.value);
     * }
     * </pre> 
     */
    public Iterator<ObjectCursor<KType>> iterator();

    /**
     * Lookup a given element in the container. This operation has no speed
     * guarantees (may be linear with respect to the size of this container).
     * 
     * @return Returns <code>true</code> if this container has an element
     * equal to <code>e</code>.
     */
    public boolean contains(KType e);

    /**
     * Return the current number of elements in this container. The time for calculating
     * the container's size may take <code>O(n)</code> time, although implementing classes
     * should try to maintain the current size and return in constant time.
     */
    public int size();

    /**
     * Shortcut for <code>size() == 0</code>.
     */
    public boolean isEmpty();

    /**
     * Copies all elements from this container to an array. The returned array is always a copy,
     * regardless of the storage used by the container.
     */
    public KType [] toArray();

    /**
     * Applies a <code>procedure</code> to all container elements.
     */
    public void forEach(ObjectProcedure<? super KType> procedure);

    /**
     * Applies a <code>predicate</code> to container elements as long, as the predicate
     * returns <code>true</code>. The iteration is interrupted otherwise. 
     */
    public void forEach(ObjectPredicate<? super KType> predicate);
}
