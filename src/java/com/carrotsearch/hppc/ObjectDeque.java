package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.predicates.ObjectPredicate;
import com.carrotsearch.hppc.procedures.ObjectProcedure;


/**
 * A double-linked queue of <code>KType</code>s.
 */
public interface ObjectDeque<KType> extends ObjectCollection<KType>
{
    /**
     * Removes the first element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public int removeFirstOccurrence(KType e1);
    
    /**
     * Removes the last element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public int removeLastOccurrence(KType e1);

    /**
     * Inserts the specified element at the front of this deque.
     *
     * @param e1 the element to add
     */
    public void addFirst(KType e1);

    /**
     * Inserts the specified element at the end of this deque.
     *
     * @param e1 the element to add
     */
    public void addLast(KType e1);
    
    /**
     * Retrieves and removes the first element of this deque.
     *
     * @return the head element of this deque.
     * @throws AssertionError if this deque is empty and assertions are enabled.
     */
    public KType removeFirst();
    
    /**
     * Retrieves and removes the last element of this deque.
     *
     * @return the tail of this deque.
     * @throws AssertionError if this deque is empty and assertions are enabled.
     */
    public KType removeLast();
    
    /**
     * Retrieves, but does not remove, the first element of this deque.
     *
     * @return the head of this deque.
     * @throws AssertionError if this deque is empty and assertions are enabled.
     */
    public KType getFirst();

    /**
     * Retrieves, but does not remove, the last element of this deque.
     *
     * @return the tail of this deque.
     * @throws AssertionError if this deque is empty and assertions are enabled.
     */
    public KType getLast();

    /**
     * @return An iterator over elements in this deque in tail-to-head order. 
     */
    public Iterator<ObjectCursor<KType>> descendingIterator();
    
    /**
     * Applies a <code>procedure</code> to all container elements.
     */
    public void descendingForEach(ObjectProcedure<? super KType> procedure);

    /**
     * Applies a <code>predicate</code> to container elements as long, as the predicate
     * returns <code>true</code>. The iteration is interrupted otherwise. 
     */
    public void descendingForEach(ObjectPredicate<? super KType> predicate);
}
