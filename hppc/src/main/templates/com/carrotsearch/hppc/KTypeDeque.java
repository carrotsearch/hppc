package com.carrotsearch.hppc;

import java.util.Deque;
import java.util.Iterator;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

/**
 * A linear collection that supports element insertion and removal at both ends.
 * 
 * @see Deque
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeDeque<KType> extends KTypeCollection<KType> {
  /**
   * Removes the first element that equals <code>e</code>.
   * 
   * @return The deleted element's index or <code>-1</code> if the element
   *         was not found.
   */
  public int removeFirst(KType e);

  /**
   * Removes the last element that equals <code>e</code>.
   * 
   * @return The deleted element's index or <code>-1</code> if the element
   *         was not found.
   */
  public int removeLast(KType e);

  /**
   * Inserts the specified element at the front of this deque.
   */
  public void addFirst(KType e);

  /**
   * Inserts the specified element at the end of this deque.
   */
  public void addLast(KType e);

  /**
   * Retrieves and removes the first element of this deque.
   *
   * @return the head (first) element of this deque.
   */
  public KType removeFirst();

  /**
   * Retrieves and removes the last element of this deque.
   *
   * @return the tail of this deque.
   */
  public KType removeLast();

  /**
   * Retrieves the first element of this deque but does not remove it.
   *
   * @return the head of this deque.
   */
  public KType getFirst();

  /**
   * Retrieves the last element of this deque but does not remove it.
   *
   * @return the head of this deque.
   */
  public KType getLast();

  /**
   * @return An iterator over elements in this deque in tail-to-head order.
   */
  public Iterator<KTypeCursor<KType>> descendingIterator();

  /**
   * Applies a <code>procedure</code> to all elements in tail-to-head order.
   */
  public <T extends KTypeProcedure<? super KType>> T descendingForEach(T procedure);

  /**
   * Applies a <code>predicate</code> to container elements as long, as the
   * predicate returns <code>true</code>. The iteration is interrupted
   * otherwise.
   */
  public <T extends KTypePredicate<? super KType>> T descendingForEach(T predicate);
}
