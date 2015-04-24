package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

/**
 * A generic container holding <code>KType</code>s.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeContainer<KType> extends Iterable<KTypeCursor<KType>> {
  /**
   * Returns an iterator to a cursor traversing the collection. The order of
   * traversal is not defined. More than one cursor may be active at a time. The
   * behavior of iterators is undefined if structural changes are made to the
   * underlying collection.
   * 
   * <p>
   * The iterator is implemented as a cursor and it returns <b>the same cursor
   * instance</b> on every call to {@link Iterator#next()} (to avoid boxing of
   * primitive types). To read the current list's value (or index in the list)
   * use the cursor's public fields. An example is shown below.
   * </p>
   * 
   * <pre>
   * for (KTypeCursor&lt;KType&gt; c : container) {
   *   System.out.println(&quot;index=&quot; + c.index + &quot; value=&quot; + c.value);
   * }
   * </pre>
   */
  public Iterator<KTypeCursor<KType>> iterator();

  /**
   * Lookup a given element in the container. This operation has no speed
   * guarantees (may be linear with respect to the size of this container).
   * 
   * @return Returns <code>true</code> if this container has an element equal to
   *         <code>e</code>.
   */
  public boolean contains(KType e);

  /**
   * Return the current number of elements in this container. The time for
   * calculating the container's size may take <code>O(n)</code> time, although
   * implementing classes should try to maintain the current size and return in
   * constant time.
   */
  public int size();

  /**
   * Shortcut for <code>size() == 0</code>.
   */
  public boolean isEmpty();

  /**
   * Copies all elements of this container to an array.
   * 
   * The returned array is always a copy, regardless of the storage used by the
   * container.
   */
  /*! #if ($TemplateOptions.KTypePrimitive)   
  public KType [] toArray();
      #else !*/
  public Object[] toArray();
  /*! #end !*/

  /* #if ($TemplateOptions.KTypeGeneric) */
  /**
   * Copies all elements of this container to a dynamically created array of the
   * given component type.
   * 
   * @throws ArrayStoreException Thrown if this container's elements are not 
   * assignable to the array's component type.
   */
  public <T> T[] toArray(Class<T> componentClass);
  /* #end */

  /**
   * Applies a <code>procedure</code> to all container elements. Returns the
   * argument (any subclass of {@link KTypeProcedure}. This lets the caller to
   * call methods of the argument by chaining the call (even if the argument is
   * an anonymous type) to retrieve computed values, for example (IntContainer):
   * 
   * <pre>
   * int count = container.forEach(new IntProcedure() {
   *   int count; // this is a field declaration in an anonymous class.
   * 
   *   public void apply(int value) {
   *     count++;
   *   }
   * }).count;
   * </pre>
   */
  public <T extends KTypeProcedure<? super KType>> T forEach(T procedure);

  /**
   * Applies a <code>predicate</code> to container elements as long, as the
   * predicate returns <code>true</code>. The iteration is interrupted
   * otherwise.
   */
  public <T extends KTypePredicate<? super KType>> T forEach(T predicate);
}
