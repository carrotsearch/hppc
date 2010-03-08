package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.ObjectPredicate;
import com.carrotsearch.hppc.procedures.*;

/**
 * An array-backed list of KTypes. A single array is used to store and manipulate
 * all elements. Reallocations are governed by a {@link ArraySizingStrategy}
 * and may be expensive if they move around really large chunks of memory.
 *
 * A brief comparison of the API against the Java Collections framework:
 * <table class="nice" summary="Java Collections ArrayList and HPPC ObjectArrayList, related methods.">
 * <caption>Java Collections ArrayList and HPPC {@link ObjectArrayList}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain ArrayList java.util.ArrayList}</th>
 *         <th scope="col">{@link ObjectArrayList}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>add            </td><td>add            </td></tr>
 * <tr class="odd"><td>add(index,v)   </td><td>insert(index,v)</td></tr>
 * <tr            ><td>get            </td><td>get            </td></tr>
 * <tr class="odd"><td>removeRange, 
 *                     removeElementAt</td><td>removeRange, remove</td></tr>                     
 * <tr            ><td>remove(Object) </td><td>removeFirstOccurrence, removeLastOccurrence, 
 *                                             removeAllOccurrences</td></tr>
 * <tr class="odd"><td>clear          </td><td>clear, release </td></tr>
 * <tr            ><td>size           </td><td>size           </td></tr>
 * <tr class="odd"><td>ensureCapacity </td><td>ensureCapacity, resize</td></tr>
 * <tr            ><td>indexOf        </td><td>indexOf        </td></tr>
 * <tr class="odd"><td>lastIndexOf    </td><td>lastIndexOf    </td></tr>
 * <tr            ><td>trimToSize     </td><td>trimtoSize</td></tr>
 * <tr class="odd"><td>Object[] toArray()</td><td>KType[] toArray()</td></tr> 
 * <tr            ><td>iterator       </td><td>{@linkplain #iterator() cursor over values}</td></tr>
 * </tbody>
 * </table>
 */
public class ObjectArrayList<KType>
    extends AbstractObjectCollection<KType>
{
    /**
     * Default capacity if no other capacity is given in the constructor.
     */
    public final static int DEFAULT_CAPACITY = 5;

    /**
     * Internal static instance of an empty buffer.
     */
    private final static Object EMPTY = Intrinsics.newKTypeArray(0);

    /* removeIf:primitive */
    /*
     * The actual value in this field is always <code>Object[]</code>, regardless of the
     * generic type used. The JDK is inconsistent here too -- {@link ArrayList} declares
     * internal <code>Object[]</code> buffer, but {@link ArrayDeque} declares an array of
     * generic type objects like we do. The tradeoff is probably minimal, but you should
     * be aware of additional casts generated by <code>javac</code>
     * when <code>buffer</code> is directly accessed - these casts may result in exceptions
     * at runtime. A workaround is to cast directly to <code>Object[]</code> before
     * accessing the buffer's elements.
     */
    /* end:removeIf */

    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ({@link #size()}).
     */
    public KType [] buffer;

    /**
     * Current number of elements stored in {@link #buffer}.
     */
    public int elementsCount;

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * Create with default sizing strategy and initial capacity for storing 
     * {@value #DEFAULT_CAPACITY} elements.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public ObjectArrayList()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public ObjectArrayList(int initialCapacity)
    {
        this(initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a custom buffer resizing strategy.
     */
    public ObjectArrayList(int initialCapacity, ArraySizingStrategy resizer)
    {
        assert initialCapacity >= 0 : "initialCapacity must be >= 0: " + initialCapacity;
        assert resizer != null;

        this.resizer = resizer;
        ensureBufferSpace(resizer.round(initialCapacity));
    }

    /**
     * {@inheritDoc}
     */
    public int add(KType e1)
    {
        ensureBufferSpace(1);
        buffer[elementsCount++] = e1;
        return 1;
    }

    /**
     * Appends two elements at the end of the list. To add more than two elements,
     * use <code>add</code> (vararg-version) or access the buffer directly (tight
     * loop).
     */
    public final void add(KType e1, KType e2)
    {
        ensureBufferSpace(2);
        buffer[elementsCount++] = e1;
        buffer[elementsCount++] = e2;
    }

    /**
     * Add all elements from a range of given array to the list.
     */
    public final void add(KType [] elements, int start, int length)
    {
        assert length >= 0 : "Length must be >= 0";

        ensureBufferSpace(length);
        System.arraycopy(elements, start, buffer, elementsCount, length);
        elementsCount += length;
    }

    /**
     * Vararg-signature method for adding elements at the end of the list.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     */
    public final void add(KType... elements)
    {
        add(elements, 0, elements.length);
    }

    /**
     * Adds all elements from a cursor iterator.
     * 
     * @param iterator An iterator returning a cursor over a collection of KType elements. 
     * @return Returns the number of elements actually added as a result of this
     * call.
     */
    public final int addAll(Iterator<? extends ObjectCursor<? extends KType>> iterator)
    {
        int count = 0;
        while (iterator.hasNext())
        {
            add(iterator.next().value);
            count++;
        }

        return count;
    }

    /**
     * Adds all element from an iterable.
     * 
     * @see #addAll(Iterator)
     */
    @Override
    public final int addAll(Iterable<? extends ObjectCursor<? extends KType>> iterable)
    {
        return addAll(iterable.iterator());
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * 
     * @param index The index at which the element should be inserted, shifting
     * any existing and subsequent elements to the right.
     */
    public final void insert(int index, KType e1)
    {
        assert (index >= 0 && index <= size()) :
            "Index " + index + " out of bounds [" + 0 + ", " + size() + "].";

        ensureBufferSpace(1);
        System.arraycopy(buffer, index, buffer, index + 1, elementsCount - index);
        buffer[index] = e1;
        elementsCount++;
    }

    /**
     * @return Returns the element at index <code>index</code> from the list.
     */
    public final KType get(int index)
    {
        assert (index >= 0 && index < size()) :
            "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        return buffer[index];
    }

    /**
     * Replaces the element at the specified position in this list 
     * with the specified element. Returns the previous value in the list.
     */
    public final KType set(int index, KType e1)
    {
        assert (index >= 0 && index < size()) :
            "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        final KType v = buffer[index];
        buffer[index] = e1;
        return v;
    }

    /**
     * Removes the element at the specified position in this list and returns it.
     * 
     * <p><b>Careful.</b> Do not confuse this method with the overridden signature in
     * Java Collections ({@link List#remove(Object)}). Use {@link #removeAll},
     * {@link #removeFirstOccurrence} or {@link #removeLastOccurrence} for this purpose.</p> 
     */
    public final KType remove(int index)
    {
        assert (index >= 0 && index < size()) :
            "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        final KType v = buffer[index];
        if (index + 1 < elementsCount)
            System.arraycopy(buffer, index + 1, buffer, index, elementsCount - index - 1);
        elementsCount--;
        buffer[elementsCount] = Intrinsics.<KType>defaultKTypeValue();
        return v;
    }

    /**
     * Removes from this list all of the elements whose index is between 
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
     */
    public final void removeRange(int fromIndex, int toIndex)
    {
        assert (fromIndex >= 0 && fromIndex <= size()) :
            "Index " + fromIndex + " out of bounds [" + 0 + ", " + size() + ").";

        assert (toIndex >= 0 && toIndex <= size()) :
            "Index " + toIndex + " out of bounds [" + 0 + ", " + size() + "].";
        
        assert fromIndex <= toIndex : "fromIndex must be <= toIndex: "
            + fromIndex + ", " + toIndex;

        System.arraycopy(buffer, toIndex, buffer, fromIndex, elementsCount - toIndex);

        final int count = toIndex - fromIndex;
        elementsCount -= count;
        Arrays.fill(buffer, elementsCount, elementsCount + count, 
            Intrinsics.<KType>defaultKTypeValue());
    }

    /**
     * Removes the first element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public final int removeFirstOccurrence(KType e1)
    {
        final int index = indexOf(e1);
        if (index >= 0) remove(index);
        return index;
    }

    /**
     * Removes the last element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public final int removeLastOccurrence(KType e1)
    {
        final int index = lastIndexOf(e1);
        if (index >= 0) remove(index);
        return index;
    }

    /**
     * Removes all elements that equal <code>e1</code>. This method does a single
     * scan through the list.
     * 
     * @return Returns the count of elements removed from the list.
     */
    @Override
    public final int removeAllOccurrences(KType e1)
    {
        int to = 0;
        for (int from = 0; from < elementsCount; from++)
        {
            if (Intrinsics.equals(e1, buffer[from]))
            {
                buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                continue;
            }

            if (to != from)
            {
                buffer[to] = buffer[from];
                buffer[from] = Intrinsics.<KType>defaultKTypeValue();
            }
            to++;
        }

        final int deleted = elementsCount - to; 
        this.elementsCount = to;
        return deleted;
    }

    /**
     * Returns <code>true</code> if this list contains the specified element
     * (linear scan).
     */
    @Override
    public final boolean contains(KType e1)
    {
        return indexOf(e1) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list, 
     * or -1 if this list does not contain the element.
     */
    public final int indexOf(KType e1)
    {
        for (int i = 0; i < elementsCount; i++)
            if (Intrinsics.equals(e1, buffer[i]))
                return i;

        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list, 
     * or -1 if this list does not contain the element.
     */
    public final int lastIndexOf(KType e1)
    {
        for (int i = elementsCount - 1; i >= 0; i--)
            if (Intrinsics.equals(e1, buffer[i]))
                return i;

        return -1;
    }

    /**
     * Return <code>true</code> if this list is empty. 
     */
    @Override
    public final boolean isEmpty()
    {
        return elementsCount == 0;
    }

    /**
     * Increases the capacity of this instance, if necessary, to ensure 
     * that it can hold at least the number of elements specified by 
     * the minimum capacity argument.
     */
    public final void ensureCapacity(int minCapacity) 
    {
        if (minCapacity > this.buffer.length)
            ensureBufferSpace(minCapacity - size());
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * <code>expectedAdditions</code>. Increases internal buffer size if needed.
     */
    protected final void ensureBufferSpace(int expectedAdditions)
    {
        final int bufferLen = (buffer == null ? 0 : buffer.length);
        if (elementsCount + expectedAdditions >= bufferLen)
        {
            final int newSize = resizer.grow(bufferLen, elementsCount, expectedAdditions);
            assert newSize >= elementsCount + expectedAdditions : "Resizer failed to" +
                    " return sensible new size: " + newSize + " <= " 
                    + (elementsCount + expectedAdditions);

            final KType [] newBuffer = Intrinsics.newKTypeArray(newSize);
            if (bufferLen > 0)
            {
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                /* removeIf:primitiveKType */
                Arrays.fill(buffer, null); // Help the GC.
                /* end:removeIf */
            }
            this.buffer = newBuffer;
        }
    }

    /**
     * Truncate or expand the list to the new size. If the list is truncated, the buffer
     * will not be reallocated (use {@link #trimToSize()} if you need a truncated buffer),
     * but the truncated values will be reset to the default value (zero). If the list is
     * expanded, the elements beyond the current size are initialized with JVM-defaults
     * (zero or <code>null</code> values).
     */
    public void resize(int newSize)
    {
        if (newSize <= buffer.length)
        {
            if (newSize < elementsCount)
            {
                Arrays.fill(buffer, newSize, elementsCount, 
                    Intrinsics.<KType>defaultKTypeValue());
            }
            else
            {
                Arrays.fill(buffer, elementsCount, newSize, 
                    Intrinsics.<KType>defaultKTypeValue());
            }
        }
        else
        {
            ensureCapacity(newSize);
        }
        this.elementsCount = newSize; 
    }

    /**
     * @return The number of currently stored elements.
     */
    @Override
    public final int size()
    {
        return elementsCount;
    }

    /**
     * Trim the internal buffer to the current size.
     */
    public final void trimToSize()
    {
        if (size() != this.buffer.length)
            this.buffer = toArray();
    }

    /**
     * Sets the number of stored elements to zero. Releases and initializes the
     * internal storage array to default values for object lists
     * to allow garbage collection. For primitive lists, the buffer is not cleared, use
     * <pre>
     * resize(0);
     * </pre>  
     * to clean the buffer and the array at the same time.
     */
    @Override
    public final void clear()
    {
        /* removeIf:primitiveKType */
        Arrays.fill(buffer, 0, elementsCount, null); 
        /* end:removeIf */
        this.elementsCount = 0;
    }

    /**
     * Sets the number of stored elements to zero and releases the internal storage array.
     */
    /* removeIf:primitive */ 
    @SuppressWarnings("unchecked") 
    /* end:removeIf */
    public final void release()
    {
        clear();
        /* removeIf:primitiveKType */
        Arrays.fill(buffer, null); // Help the GC.
        /* end:removeIf */
        this.buffer = (KType []) EMPTY;
    }

    /**
     * Create a copy of the list's elements. The returned array is sized to match exactly
     * the number of elements of the stack.
     */
    @Override
    public final KType [] toArray()
    {
        final KType [] cloned = Intrinsics.newKTypeArray(elementsCount);
        System.arraycopy(buffer, 0, cloned, 0, elementsCount);
        return cloned;
    }

    /**
     * Currently not implemented and throws an {@link UnsupportedOperationException}. 
     */
    @Override
    public int hashCode()
    {
        throw new UnsupportedOperationException("hashCode() not implemented.");
    }

    /**
     * Currently not implemented and throws an {@link UnsupportedOperationException}. 
     */
    @Override
    public boolean equals(Object obj)
    {
        throw new UnsupportedOperationException("equals() not implemented.");
    }

    /**
     * An iterator implementation for {@link ObjectArrayList#iterator}.
     */
    private final class ValueIterator implements Iterator<ObjectCursor<KType>>
    {
        private final ObjectCursor<KType> cursor;

        /** The last index at which {@link #hasNext()} will return <code>true</code>. */
        private final int lastIndex;

        public ValueIterator()
        {
            cursor = new ObjectCursor<KType>();
            cursor.index = -1;
            lastIndex = size() - 1;
        }

        public boolean hasNext()
        {
            return cursor.index < lastIndex;
        }

        public ObjectCursor<KType> next()
        {
            assert cursor.index <= lastIndex;

            cursor.index++;
            cursor.value = buffer[cursor.index];
            return cursor;
        }

        public void remove()
        {
            /* 
             * It will be much more efficient to have a removal using a closure-like 
             * structure (then we can simply move elements to proper slots as we iterate
             * over the array as in #removeAll). 
             */
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a cursor over the values of this list. The iterator is implemented as a
     * cursor and it returns <b>the same cursor instance</b> on every call to
     * {@link Iterator#next()} (to avoid boxing of primitive types). To read the current
     * list's value (or index in the list) use the cursor's public fields. An example is
     * shown below.
     * 
     * <pre>
     * for (IntValueCursor c : intList) {
     *   System.out.println("index=" + c.index + " value=" + c.value);
     * }
     * </pre>
     */
    public Iterator<ObjectCursor<KType>> iterator()
    {
        return new ValueIterator();
    }

    /**
     * Applies <code>procedure</code> to all elements of this list. This method
     * is about twice as fast as running an iterator and nearly as fast
     * as running a code loop over the buffer content (!).
     *
     * @see "HPPC benchmarks." 
     */
    public void forEach(ObjectProcedure<? super KType> procedure)
    {
        forEach(procedure, 0, size());
    }

    /**
     * Applies <code>procedure</code> to a slice of the list,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, 
     * exclusive.
     */
    public void forEach(ObjectProcedure<? super KType> procedure, int fromIndex,
        final int toIndex)
    {
        assert (fromIndex >= 0 && fromIndex <= size()) :
            "Index " + fromIndex + " out of bounds [" + 0 + ", " + size() + ").";

        assert (toIndex >= 0 && toIndex <= size()) :
            "Index " + toIndex + " out of bounds [" + 0 + ", " + size() + "].";
        
        assert fromIndex <= toIndex : "fromIndex must be <= toIndex: "
            + fromIndex + ", " + toIndex;

        final KType [] buffer = this.buffer;
        for (int i = fromIndex; i < toIndex; i++)
        {
            procedure.apply(buffer[i]);
        }
    }

    /**
     * Create a list from a variable number of arguments or an array of <code>KType</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static /* removeIf:primitive */<KType> /* end:removeIf */ 
      ObjectArrayList<KType> from(KType... elements)
    {
        final ObjectArrayList<KType> list = new ObjectArrayList<KType>(elements.length);
        list.add(elements);
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(ObjectPredicate<? super KType> predicate)
    {
        final int elementsCount = this.elementsCount;
        int to = 0;
        try
        {
            for (int from = 0; from < elementsCount; from++)
            {
                if (predicate.apply(buffer[from]))
                {
                    buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                    continue;
                }
    
                if (to != from)
                {
                    buffer[to] = buffer[from];
                    buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                }
                to++;
            }
        }
        finally
        {
            this.elementsCount = to;
        }

        return elementsCount - to; 
    }
}
