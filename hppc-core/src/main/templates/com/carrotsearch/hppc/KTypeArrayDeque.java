package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

import static com.carrotsearch.hppc.Internals.*;

/**
 * An array-backed deque (doubly linked queue) of KTypes. A single array is used to store and 
 * manipulate all elements. Reallocations are governed by a {@link ArraySizingStrategy}
 * and may be expensive if they move around really large chunks of memory.
 *
#if ($TemplateOptions.KTypeGeneric)
 * A brief comparison of the API against the Java Collections framework:
 * <table class="nice" summary="Java Collections ArrayDeque and HPPC ObjectArrayDeque, related methods.">
 * <caption>Java Collections ArrayDeque and HPPC {@link ObjectArrayDeque}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain ArrayDeque java.util.ArrayDeque}</th>
 *         <th scope="col">{@link ObjectArrayDeque}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>addFirst       </td><td>addFirst       </td></tr>
 * <tr class="odd"><td>addLast        </td><td>addLast        </td></tr>
 * <tr            ><td>removeFirst    </td><td>removeLast     </td></tr>
 * <tr class="odd"><td>getFirst       </td><td>getFirst       </td></tr>                     
 * <tr            ><td>getLast        </td><td>getLast        </td></tr>
 * <tr class="odd"><td>removeFirstOccurrence,
 *                     removeLastOccurrence
 *                                    </td><td>removeFirstOccurrence,
 *                                             removeLastOccurrence
 *                                                            </td></tr>
 * <tr            ><td>size           </td><td>size           </td></tr>
 * <tr class="odd"><td>Object[] toArray()</td><td>KType[] toArray()</td></tr> 
 * <tr            ><td>iterator       </td><td>{@linkplain #iterator cursor over values}</td></tr>
 * <tr class="odd"><td>other methods inherited from Stack, Queue</td><td>not implemented</td></tr>
 * </tbody>
 * </table>
#else
 * <p>See {@link ObjectArrayDeque} class for API similarities and differences against Java
 * Collections.
#end
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayDeque<KType> 
    extends AbstractKTypeCollection<KType> implements KTypeDeque<KType>, Cloneable
{
    /**
     * Default capacity if no other capacity is given in the constructor.
     */
    public final static int DEFAULT_CAPACITY = 5;

    /**
     * Internal array for storing elements.
     * 
     * #if ($TemplateOptions.KTypeGeneric)
     * <p>The actual value in this field is always an instance of <code>Object[]</code>,
     * regardless of the generic type used. The JDK is inconsistent here too --
     * {@link ArrayList} declares internal <code>Object[]</code> buffer, but
     * {@link ArrayDeque} declares an array of generic type objects like we do. The
     * tradeoff is probably minimal, but you should be aware of additional casts generated
     * by <code>javac</code> when <code>buffer</code> is directly accessed - these casts
     * may result in exceptions at runtime. A workaround is to cast directly to
     * <code>Object[]</code> before accessing the buffer's elements.#end
     */
    public KType [] buffer;

    /**
     * The index of the element at the head of the deque or an
     * arbitrary number equal to tail if the deque is empty.
     */
    public int head;

    /**
     * The index at which the next element would be added to the tail
     * of the deque.
     */
    public int tail;

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
    public KTypeArrayDeque()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public KTypeArrayDeque(int initialCapacity)
    {
        this(initialCapacity, new BoundedProportionalArraySizingStrategy());
    }

    /**
     * Create with a custom buffer resizing strategy.
     */
    public KTypeArrayDeque(int initialCapacity, ArraySizingStrategy resizer)
    {
        assert initialCapacity >= 0 : "initialCapacity must be >= 0: " + initialCapacity;
        assert resizer != null;

        this.resizer = resizer;
        initialCapacity = resizer.round(initialCapacity);
        buffer = Intrinsics.newKTypeArray(initialCapacity);
    }

    /**
     * Creates a new deque from elements of another container, appending them
     * at the end of this deque. 
     */
    public KTypeArrayDeque(KTypeContainer<? extends KType> container)
    {
        this(container.size());
        addLast(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFirst(KType e1)
    {
        int h = oneLeft(head, buffer.length);
        if (h == tail)
        {
            ensureBufferSpace(1);
            h = oneLeft(head, buffer.length);
        }
        buffer[head = h] = e1;
    }

    /**
     * Vararg-signature method for adding elements at the front of this deque.
     * 
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     */
    public void addFirst(KType... elements)
    {
        ensureBufferSpace(elements.length);

        // For now, naive loop.
        for (int i = 0; i < elements.length; i++)
            addFirst(elements[i]);
    }

    /**
     * Inserts all elements from the given container to the front of this deque.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call.
     */
    public int addFirst(KTypeContainer<? extends KType> container)
    {
        int size = container.size();
        ensureBufferSpace(size);

        for (KTypeCursor<? extends KType> cursor : container)
        {
            addFirst(cursor.value);
        }

        return size;
    }

    /**
     * Inserts all elements from the given iterable to the front of this deque.
     * 
     * @return Returns the number of elements actually added as a result of this call.
     */
    public int addFirst(Iterable<? extends KTypeCursor<? extends KType>> iterable)
    {
        int size = 0;
        for (KTypeCursor<? extends KType> cursor : iterable)
        {
            addFirst(cursor.value);
            size++;
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLast(KType e1)
    {
        int t = oneRight(tail, buffer.length);
        if (head == t)
        {
            ensureBufferSpace(1);
            t = oneRight(tail, buffer.length);
        }
        buffer[tail] = e1;
        tail = t;
    }
    
    /**
     * Vararg-signature method for adding elements at the end of this deque.
     * 
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     */
    public void addLast(KType... elements)
    {
        ensureBufferSpace(1);

        // For now, naive loop.
        for (int i = 0; i < elements.length; i++)
            addLast(elements[i]);
    }

    /**
     * Inserts all elements from the given container to the end of this deque.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call.
     */
    public int addLast(KTypeContainer<? extends KType> container)
    {
        int size = container.size();
        ensureBufferSpace(size);

        for (KTypeCursor<? extends KType> cursor : container)
        {
            addLast(cursor.value);
        }

        return size;
    }
    
    /**
     * Inserts all elements from the given iterable to the end of this deque.
     * 
     * @return Returns the number of elements actually added as a result of this call.
     */
    public int addLast(Iterable<? extends KTypeCursor<? extends KType>> iterable)
    {
        int size = 0;
        for (KTypeCursor<? extends KType> cursor : iterable)
        {
            addLast(cursor.value);
            size++;
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType removeFirst()
    {
        assert size() > 0 : "The deque is empty.";

        final KType result = buffer[head];
        buffer[head] = Intrinsics.<KType>defaultKTypeValue();
        head = oneRight(head, buffer.length); 
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType removeLast()
    {
        assert size() > 0 : "The deque is empty.";

        tail = oneLeft(tail, buffer.length); 
        final KType result = buffer[tail];
        buffer[tail] = Intrinsics.<KType>defaultKTypeValue();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType getFirst()
    {
        assert size() > 0 : "The deque is empty.";

        return buffer[head];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KType getLast()
    {
        assert size() > 0 : "The deque is empty.";

        return buffer[oneLeft(tail, buffer.length)];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeFirstOccurrence(KType e1)
    {
        final int index = bufferIndexOf(e1);
        if (index >= 0) removeAtBufferIndex(index);
        return index;
    }

    /**
     * Return the index of the first (counting from head) element equal to
     * <code>e1</code>. The index points to the {@link #buffer} array.
     *   
     * @param e1 The element to look for.
     * @return Returns the index of the first element equal to <code>e1</code>
     * or <code>-1</code> if not found.
     */
    public int bufferIndexOf(KType e1)
    {
        final int last = tail;
        final int bufLen = buffer.length;
        for (int i = head; i != last; i = oneRight(i, bufLen))
        {
            if (Intrinsics.equalsKType(e1, buffer[i]))
                return i;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeLastOccurrence(KType e1)
    {
        final int index = lastBufferIndexOf(e1);
        if (index >= 0) removeAtBufferIndex(index);
        return index;
    }

    /**
     * Return the index of the last (counting from tail) element equal to
     * <code>e1</code>. The index points to the {@link #buffer} array.
     *   
     * @param e1 The element to look for.
     * @return Returns the index of the first element equal to <code>e1</code>
     * or <code>-1</code> if not found.
     */
    public int lastBufferIndexOf(KType e1)
    {
        final int bufLen = buffer.length;
        final int last = oneLeft(head, bufLen);
        for (int i = oneLeft(tail, bufLen); i != last; i = oneLeft(i, bufLen))
        {
            if (Intrinsics.equalsKType(e1, buffer[i]))
                return i;
        }

        return -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAllOccurrences(KType e1)
    {
        int removed = 0;
        final int last = tail;
        final int bufLen = buffer.length;
        int from, to;
        for (from = to = head; from != last; from = oneRight(from, bufLen))
        {
            if (Intrinsics.equalsKType(e1, buffer[from]))
            {
                buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                removed++;
                continue;
            }
    
            if (to != from)
            {
                buffer[to] = buffer[from];
                buffer[from] = Intrinsics.<KType>defaultKTypeValue();
            }
    
            to = oneRight(to, bufLen);
        }
    
        tail = to;
        return removed;
    }

    /**
     * Removes the element at <code>index</code> in the internal
     * {#link {@link #buffer}} array, returning its value.
     * 
     * @param index Index of the element to remove. The index must be located between
     * {@link #head} and {@link #tail} in modulo {@link #buffer} arithmetic. 
     */
    public void removeAtBufferIndex(int index)
    {
        assert (head <= tail 
            ? index >= head && index < tail
            : index >= head || index < tail) : "Index out of range (head=" 
                + head + ", tail=" + tail + ", index=" + index + ").";

        // Cache fields in locals (hopefully moved to registers).
        final KType [] b = this.buffer;
        final int bufLen = b.length;
        final int lastIndex = bufLen - 1;
        final int head = this.head;
        final int tail = this.tail;

        final int leftChunk = Math.abs(index - head) % bufLen;
        final int rightChunk = Math.abs(tail - index) % bufLen;

        if (leftChunk < rightChunk)
        {
            if (index >= head)
            {
                System.arraycopy(b, head, b, head + 1, leftChunk);
            }
            else
            {
                System.arraycopy(b, 0, b, 1, index);
                b[0] = b[lastIndex];
                System.arraycopy(b, head, b, head + 1, lastIndex - head);
            }
            b[head] = Intrinsics.<KType>defaultKTypeValue();
            this.head = oneRight(head, bufLen);
        }
        else
        {
            if (index < tail)
            {
                System.arraycopy(b, index + 1, b, index, rightChunk);
            }
            else
            {
                System.arraycopy(b, index + 1, b, index, lastIndex - index);
                b[lastIndex] = b[0];
                System.arraycopy(b, 1, b, 0, tail);
            }
            b[tail] = Intrinsics.<KType>defaultKTypeValue();
            this.tail = oneLeft(tail, bufLen);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        if (head <= tail)
            return tail - head;
        else
            return (tail - head + buffer.length);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>The internal array buffers are not released as a result of this call.</p>
     * 
     * @see #release()
     */
    @Override
    public void clear()
    {
        if (head < tail)
        {
            Arrays.fill(buffer, head, tail, Intrinsics.<KType>defaultKTypeValue());
        }
        else
        {
            Arrays.fill(buffer, 0, tail, Intrinsics.<KType>defaultKTypeValue());
            Arrays.fill(buffer, head, buffer.length, Intrinsics.<KType>defaultKTypeValue());
        }
        this.head = tail = 0;
    }

    /**
     * Release internal buffers of this deque and reallocate the smallest buffer possible.
     */
    public void release()
    {
        this.head = tail = 0;
        buffer = Intrinsics.newKTypeArray(resizer.round(DEFAULT_CAPACITY));
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * <code>expectedAdditions</code>. Increases internal buffer size if needed.
     */
    protected void ensureBufferSpace(int expectedAdditions)
    {
        final int bufferLen = (buffer == null ? 0 : buffer.length);
        final int elementsCount = size();
        // +1 because there is always one empty slot in a deque.
        final int requestedMinimum = 1 + elementsCount + expectedAdditions; 
        if (requestedMinimum >= bufferLen)
        {
            final int newSize = resizer.grow(bufferLen, elementsCount, expectedAdditions + 1);
            assert newSize >= requestedMinimum : "Resizer failed to" +
                    " return sensible new size: " + newSize + " <= " 
                    + (elementsCount + expectedAdditions);

            final KType [] newBuffer = Intrinsics.<KType[]>newKTypeArray(newSize);
            if (bufferLen > 0)
            {
                toArray(newBuffer);
                tail = elementsCount;
                head = 0;
            }
            this.buffer = newBuffer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*! #if ($TemplateOptions.KTypePrimitive) 
    public KType [] toArray()
        #else !*/
    public Object [] toArray()
    /*! #end !*/
    {
        final int size = size();
        return toArray(Intrinsics.<KType[]>newKTypeArray(size));
    }

    /**
     * Copies elements of this deque to an array. The content of the <code>target</code>
     * array is filled from index 0 (head of the queue) to index <code>size() - 1</code>
     * (tail of the queue).
     * 
     * @param target The target array must be large enough to hold all elements.
     * @return Returns the target argument for chaining.
     */
    public KType [] toArray(KType [] target)
    {
        assert target.length >= size() : "Target array must be >= " + size();

        if (head < tail)
        {
            // The contents is not wrapped around. Just copy.
            System.arraycopy(buffer, head, target, 0, size());
        }
        else if (head > tail)
        {
            // The contents is split. Merge elements from the following indexes:
            // [head...buffer.length - 1][0, tail - 1]
            final int rightCount = buffer.length - head;
            System.arraycopy(buffer, head, target, 0, rightCount);
            System.arraycopy(buffer,    0, target, rightCount, tail);
        }

        return target;
    }

    /**
     * Clone this object. The returned clone will reuse the same hash function
     * and array resizing strategy.
     */
    @Override
    public KTypeArrayDeque<KType> clone()
    {
        try
        {
            /* #if ($TemplateOptions.KTypeGeneric) */
            @SuppressWarnings("unchecked")
            /* #end */
            KTypeArrayDeque<KType> cloned = (KTypeArrayDeque<KType>) super.clone();
            cloned.buffer = buffer.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Move one index to the left, wrapping around buffer. 
     */
    protected static int oneLeft(int index, int modulus)
    {
        if (index >= 1) return index - 1;
        return modulus - 1;
    }

    /**
     * Move one index to the right, wrapping around buffer. 
     */
    protected static int oneRight(int index, int modulus)
    {
        if (index + 1 == modulus) return 0;
        return index + 1;
    }

    /**
     * An iterator implementation for {@link ObjectArrayDeque#iterator}.
     */
    private final class ValueIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        private final KTypeCursor<KType> cursor;
        private int remaining;

        public ValueIterator()
        {
            cursor = new KTypeCursor<KType>();
            cursor.index = oneLeft(head, buffer.length);
            this.remaining = size();
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {
            if (remaining == 0)
                return done();

            remaining--;
            cursor.value = buffer[cursor.index = oneRight(cursor.index, buffer.length)];
            return cursor;
        }
    }

    /**
     * An iterator implementation for {@link ObjectArrayDeque#descendingIterator()}.
     */
    private final class DescendingValueIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        private final KTypeCursor<KType> cursor;
        private int remaining;

        public DescendingValueIterator()
        {
            cursor = new KTypeCursor<KType>();
            cursor.index = tail;
            this.remaining = size();
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {
            if (remaining == 0)
                return done();

            remaining--;
            cursor.value = buffer[cursor.index = oneLeft(cursor.index, buffer.length)];
            return cursor;
        }
    }

    /**
     * Returns a cursor over the values of this deque (in head to tail order). The
     * iterator is implemented as a cursor and it returns <b>the same cursor instance</b>
     * on every call to {@link Iterator#next()} (to avoid boxing of primitive types). To
     * read the current value (or index in the deque's buffer) use the cursor's public
     * fields. An example is shown below.
     * 
     * <pre>
     * for (IntValueCursor c : intDeque)
     * {
     *     System.out.println(&quot;buffer index=&quot; 
     *         + c.index + &quot; value=&quot; + c.value);
     * }
     * </pre>
     */
    public Iterator<KTypeCursor<KType>> iterator()
    {
        return new ValueIterator();
    }

    /**
     * Returns a cursor over the values of this deque (in tail to head order). The
     * iterator is implemented as a cursor and it returns <b>the same cursor instance</b>
     * on every call to {@link Iterator#next()} (to avoid boxing of primitive types). To
     * read the current value (or index in the deque's buffer) use the cursor's public
     * fields. An example is shown below.
     * 
     * <pre>
     * for (Iterator<IntCursor> i = intDeque.descendingIterator(); i.hasNext(); )
     * {
     *     final IntCursor c = i.next();
     *     System.out.println(&quot;buffer index=&quot; 
     *         + c.index + &quot; value=&quot; + c.value);
     * }
     * </pre>
     */
    public Iterator<KTypeCursor<KType>> descendingIterator()
    {
        return new DescendingValueIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(T procedure)
    {
        forEach(procedure, head, tail);
        return procedure;
    }

    /**
     * Applies <code>procedure</code> to a slice of the deque,
     * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, 
     * exclusive.
     */
    private void forEach(KTypeProcedure<? super KType> procedure, int fromIndex, final int toIndex)
    {
        final KType [] buffer = this.buffer;
        for (int i = fromIndex; i != toIndex; i = oneRight(i, buffer.length))
        {
            procedure.apply(buffer[i]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(T predicate)
    {
        int fromIndex = head;
        int toIndex = tail;

        final KType [] buffer = this.buffer;
        for (int i = fromIndex; i != toIndex; i = oneRight(i, buffer.length))
        {
            if (!predicate.apply(buffer[i]))
                break;
        }
        
        return predicate;
    }

    /**
     * Applies <code>procedure</code> to all elements of this deque, tail to head. 
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T descendingForEach(T procedure)
    {
        descendingForEach(procedure, head, tail);
        return procedure;
    }

    /**
     * Applies <code>procedure</code> to a slice of the deque,
     * <code>toIndex</code>, exclusive, down to <code>fromIndex</code>, inclusive.
     */
    private void descendingForEach(KTypeProcedure<? super KType> procedure, 
        int fromIndex, final int toIndex)
    {
        if (fromIndex == toIndex)
            return;

        final KType [] buffer = this.buffer;
        int i = toIndex;
        do
        {
            i = oneLeft(i, buffer.length);
            procedure.apply(buffer[i]);
        } while (i != fromIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T descendingForEach(T predicate)
    {
        descendingForEach(predicate, head, tail);
        return predicate;
    }
    
    /**
     * Applies <code>predicate</code> to a slice of the deque,
     * <code>toIndex</code>, exclusive, down to <code>fromIndex</code>, inclusive
     * or until the predicate returns <code>false</code>.
     */
    private void descendingForEach(KTypePredicate<? super KType> predicate, 
        int fromIndex, final int toIndex)
    {
        if (fromIndex == toIndex)
            return;

        final KType [] buffer = this.buffer;
        int i = toIndex;
        do
        {
            i = oneLeft(i, buffer.length);
            if (!predicate.apply(buffer[i]))
                break;
        } while (i != fromIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(KTypePredicate<? super KType> predicate)
    {
        int removed = 0;
        final int last = tail;
        final int bufLen = buffer.length;
        int from, to;
        from = to = head;
        try
        {
            for (from = to = head; from != last; from = oneRight(from, bufLen))
            {
                if (predicate.apply(buffer[from]))
                {
                    buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                    removed++;
                    continue;
                }
    
                if (to != from)
                {
                    buffer[to] = buffer[from];
                    buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                }
        
                to = oneRight(to, bufLen);
            }
        }
        finally
        {
            // Keep the deque in consistent state even if the predicate throws an exception.
            for (; from != last; from = oneRight(from, bufLen))
            {
                if (to != from)
                {
                    buffer[to] = buffer[from];
                    buffer[from] = Intrinsics.<KType>defaultKTypeValue();
                }
        
                to = oneRight(to, bufLen);
            }
            tail = to;
        }

        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(KType e)
    {
        int fromIndex = head;
        int toIndex = tail;

        final KType [] buffer = this.buffer;
        for (int i = fromIndex; i != toIndex; i = oneRight(i, buffer.length))
        {
            if (Intrinsics.equalsKType(e, buffer[i]))
                return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int h = 1;
        int fromIndex = head;
        int toIndex = tail;

        final KType [] buffer = this.buffer;
        for (int i = fromIndex; i != toIndex; i = oneRight(i, buffer.length))
        {
            h = 31 * h + rehash(this.buffer[i]);
        }
        return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /* #if ($TemplateOptions.KTypeGeneric) */ 
    @SuppressWarnings("unchecked") 
    /* #end */
    public boolean equals(Object obj)
    {
        if (obj != null)
        {
            if (obj instanceof KTypeDeque<?>)
            {
                KTypeDeque<Object> other = (KTypeDeque<Object>) obj;
                if (other.size() == this.size())
                {
                    int fromIndex = head;
                    final KType [] buffer = this.buffer;
                    int i = fromIndex;
                    for (KTypeCursor<Object> c : other)
                    {
                        if (!Intrinsics.equalsKType(c.value, buffer[i]))
                            return false;
                        i = oneRight(i, buffer.length);                        
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static /* #if ($TemplateOptions.KTypeGeneric) */ <KType> /* #end */
      KTypeArrayDeque<KType> newInstance()
    {
        return new KTypeArrayDeque<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static /* #if ($TemplateOptions.KTypeGeneric) */ <KType> /* #end */
        KTypeArrayDeque<KType> newInstanceWithCapacity(int initialCapacity)
    {
        return new KTypeArrayDeque<KType>(initialCapacity);
    }

    /**
     * Create a new deque by pushing a variable number of arguments to the end of it.
     */
    public static /* #if ($TemplateOptions.KTypeGeneric) */ <KType> /* #end */ 
        KTypeArrayDeque<KType> from(KType... elements)
    {
        final KTypeArrayDeque<KType> coll = new KTypeArrayDeque<KType>(elements.length);
        coll.addLast(elements);
        return coll;
    }

    /**
     * Create a new deque by pushing a variable number of arguments to the end of it.
     */
    public static /* #if ($TemplateOptions.KTypeGeneric) */ <KType> /* #end */ 
        KTypeArrayDeque<KType> from(KTypeArrayDeque<KType> container)
    {
        return new KTypeArrayDeque<KType>(container);
    }
}
