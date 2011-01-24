package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/**
 * An associative container (alias: map, dictionary) from keys to values. Object keys must
 * fulfill the contract of {@link Object#hashCode()} and {@link Object#equals(Object)}.
 * 
 * @see ObjectContainer
 */
public interface ObjectObjectAssociativeContainer<KType, VType> 
    extends Iterable<ObjectObjectCursor<KType, VType>>
{
   /**
    * Returns a cursor over the entries (key-value pairs) in this map. The iterator is
    * implemented as a cursor and it returns <b>the same cursor instance</b> on every
    * call to {@link Iterator#next()}. To read the current key and value use the cursor's 
    * public fields. An example is shown below.
    * <pre>
    * for (IntShortCursor c : intShortMap)
    * {
    *     System.out.println(&quot;index=&quot; + c.index 
    *       + &quot; key=&quot; + c.key
    *       + &quot; value=&quot; + c.value);
    * }
    * </pre>
    * 
    * <p>The <code>index</code> field inside the cursor gives the internal index inside
    * the container's implementation. The interpretation of this index depends on 
    * to the container.  
    */
    @Override
    public Iterator<ObjectObjectCursor<KType, VType>> iterator();

    /**
     * Returns <code>true</code> if this container has an association to a value for
     * the given key. 
     */
    public boolean containsKey(KType key);
    
    /**
     * @return Returns the current size (number of assigned keys) in the container.
     */
    public int size();
    
    /**
     * @return Return <code>true</code> if this hash map contains no assigned keys.
     */
    public boolean isEmpty();    

    /**
     * Removes all keys (and associated values) present in a given container. An alias to:
     * <pre>
     * keySet().removeAll(container)
     * </pre>
     * but with no additional overhead.
     * 
     * @return Returns the number of elements actually removed as a result of this call.
     */
    public int removeAll(ObjectContainer<? extends KType> container);
    
    /**
     * Removes all keys (and associated values) for which the predicate returns <code>true</code>.
     * An alias to:
     * <pre>
     * keySet().removeAll(container)
     * </pre>
     * but with no additional overhead. 
     * 
     * @return Returns the number of elements actually removed as a result of this call.
     */
    public int removeAll(ObjectPredicate<? super KType> predicate);

    /**
     * Applies a given procedure to all keys-value pairs in this container. Returns the argument (any
     * subclass of {@link ObjectObjectProcedure}. This lets the caller to call methods of the argument
     * by chaining the call (even if the argument is an anonymous type) to retrieve computed values,
     * for example. 
     */
    public <T extends ObjectObjectProcedure<? super KType, ? super VType>> T forEach(T procedure);

    /**
     * Clear all keys and values in the container.
     */
    public void clear();
    
    /**
     * Returns a collection of keys of this container. The returned collection is a view
     * over the key set, any modifications introduced to the collection will propagate to the
     * map immediately.
     */
    public ObjectCollection<KType> keySet();
}
