package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.hash.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.HashContainerUtils.*;

/**
 * A hash set of <code>KType</code>s, implemented using using open
 * addressing with linear probing for collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}), {@link #allocated})
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 *
 * <p>
 * A brief comparison of the API against the Java Collections framework:
 * </p>
 * <table class="nice" summary="Java Collections HashSet and HPPC ObjectOpenHashSet, related methods.">
 * <caption>Java Collections {@link HashSet} and HPPC {@link ObjectOpenHashSet}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain HashSet java.util.HashSet}</th>
 *         <th scope="col">{@link ObjectOpenHashSet}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>boolean add(E) </td><td>boolean add(E)</td></tr>
 * <tr class="odd"><td>boolean remove(E)    </td><td>int removeAllOccurrences(E)</td></tr>
 * <tr            ><td>size, clear, 
 *                     isEmpty</td><td>size, clear, isEmpty</td></tr>
 * <tr class="odd"><td>contains(E)    </td><td>contains(E), lget()</td></tr>
 * <tr            ><td>iterator       </td><td>{@linkplain #iterator() iterator} over set values,
 *                                               pseudo-closures</td></tr>
 * </tbody>
 * </table>
 * 
 * <p>This implementation supports <code>null</code> keys.</p>
 * 
 * <p><b>Important node.</b> The implementation uses power-of-two tables and linear
 * probing, which may cause poor performance (many collisions) if hash values are
 * not properly distributed. 
 * This implementation uses {@link MurmurHash3} for rehashing keys.</p>
 * 
 * @author This code is inspired by the collaboration and implementation in the <a
 *         href="http://fastutil.dsi.unimi.it/">fastutil</a> project.
 */
public class ObjectOpenHashSet<KType>
    extends AbstractObjectCollection<KType> 
    implements ObjectLookupContainer<KType>, ObjectSet<KType>, Cloneable
{
    /**
     * Default capacity.
     */
    public final static int DEFAULT_CAPACITY = 16;

    /**
     * Minimum capacity for the set.
     */
    public final static int MIN_CAPACITY = 4;

    /**
     * Default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Hash-indexed array holding all set entries.
     * 
     * @see #allocated
     */
    public KType [] keys;

    /**
     * Information if an entry (slot) in the {@link #keys} table is allocated
     * or empty.
     * 
     * @see #assigned
     */
    public boolean [] allocated;

    /**
     * Cached number of assigned slots in {@link #allocated}.
     */
    public int assigned;

    /**
     * The load factor for this set (fraction of allocated or deleted slots
     * before the buffers must be rehashed or reallocated).
     */
    public final float loadFactor;

    /**
     * Cached capacity threshold at which we must resize the buffers. 
     */
    private int resizeThreshold;

    /**
     * The most recent slot accessed in {@link #contains} (required for
     * {@link #lget}).
     * 
     * @see #contains
     * @see #lget
     */
    private int lastSlot;

    /**
     * Creates a hash set with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
`     */
    public ObjectOpenHashSet()
    {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     */
    public ObjectOpenHashSet(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity and load factor.
     */
    public ObjectOpenHashSet(int initialCapacity, float loadFactor)
    {
        initialCapacity = Math.max(MIN_CAPACITY, initialCapacity); 

        assert initialCapacity > 0
            : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";
        assert loadFactor > 0 && loadFactor < 1
            : "Load factor must be between (0, 1).";

        this.loadFactor = loadFactor;
        allocateBuffers(roundCapacity(initialCapacity));
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public ObjectOpenHashSet(ObjectContainer<KType> container)
    {
        this((int) (container.size() * (1 + DEFAULT_LOAD_FACTOR)));
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(KType e)
    {
        if (assigned >= resizeThreshold)
            expandAndRehash();

        final int mask = allocated.length - 1;
        int slot = rehash(e) & mask;
        while (allocated[slot])
        {
            if (/* replaceIf:primitiveKType 
               (keys[slot] == e) */ 
                e == null ? keys[slot] == null : e.equals(keys[slot]) /* end:replaceIf */ )
            {
                return false;
            }

            slot = (slot + 1) & mask;
        }

        assigned++;
        allocated[slot] = true;
        keys[slot] = e;
        return true;
    }

    /**
     * Adds two elements to the set.
     */
    public int add(KType e1, KType e2)
    {
        int count = 0;
        if (add(e1)) count++;
        if (add(e2)) count++;
        return count;
    }

    /**
     * Vararg-signature method for adding elements to this set.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     * 
     * @return Returns the number of elements that were added to the set
     * (were not present in the set).
     */
    public int add(KType... elements)
    {
        int count = 0;
        for (KType e : elements)
            if (add(e)) count++;
        return count;
    }

    /**
     * Adds all elements from a given container to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public final int addAll(ObjectContainer<? extends KType> container)
    {
        return addAll((Iterable<? extends ObjectCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from a given iterable to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public final int addAll(Iterable<? extends ObjectCursor<? extends KType>> iterable)
    {
        int count = 0;
        for (ObjectCursor<? extends KType> cursor : iterable)
        {
            if (add(cursor.value)) count++;
        }
        return count;
    }

    /**
     * Expand the internal storage buffers (capacity) or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    private void expandAndRehash()
    {
        final KType [] oldKeys = this.keys;
        final boolean [] oldStates = this.allocated;

        assert assigned >= resizeThreshold;
        allocateBuffers(nextCapacity(keys.length));

        /*
         * Rehash all assigned slots from the old hash table. Deleted
         * slots are discarded.
         */
        final int mask = allocated.length - 1;
        for (int i = 0; i < oldStates.length; i++)
        {
            if (oldStates[i])
            {
                final KType key = oldKeys[i];
                
                /* removeIf:primitiveKType */ oldKeys[i] = null; /* end:removeIf */

                int slot = rehash(key) & mask;
                while (allocated[slot])
                {
                    if (/* replaceIf:primitiveKType 
                       (keys[slot] == key) */ 
                        key == null ? keys[slot] == null : key.equals(keys[slot]) /* end:replaceIf */ )
                    {
                        break;
                    }
                    slot = (slot + 1) & mask;
                }

                allocated[slot] = true;
                keys[slot] = key;                
            }
        }

        /*
         * The number of assigned items does not change, the number of deleted
         * items is zero since we have resized.
         */
        lastSlot = -1;
    }

    /**
     * Allocate internal buffers for a given capacity.
     * 
     * @param capacity New capacity (must be a power of two).
     */
    private void allocateBuffers(int capacity)
    {
        this.keys = Intrinsics.newKTypeArray(capacity);
        this.allocated = new boolean [capacity];
        this.resizeThreshold = (int) (capacity * DEFAULT_LOAD_FACTOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAllOccurrences(KType key)
    {
        return remove(key) ? 1 : 0;
    }

    /**
     * An alias for the (preferred) {@link #removeAllOccurrences}.
     */
    public boolean remove(KType key)
    {
        final int mask = allocated.length - 1;
        int slot = rehash(key) & mask; 

        while (allocated[slot])
        {
            if (/* replaceIf:primitiveKType 
                (keys[slot] == key) */ 
                key == null ? keys[slot] == null : key.equals(keys[slot]) /* end:replaceIf */ )
             {
                assigned--;
                shiftConflictingKeys(slot);
                return true;
             }
             slot = (slot + 1) & mask;
        }

        return false;
    }


    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>. 
     */
    protected final void shiftConflictingKeys(int slotCurr)
    {
        // Copied nearly verbatim from fastutil's impl.
        final int mask = allocated.length - 1;
        int slotPrev, slotOther;
        while (true)
        {
            slotCurr = ((slotPrev = slotCurr) + 1) & mask;

            while (allocated[slotCurr])
            {
                slotOther = rehash(keys[slotCurr]) & mask;
                if (slotPrev <= slotCurr)
                {
                    // we're on the right of the original slot.
                    if (slotPrev >= slotOther || slotOther > slotCurr)
                        break;
                }
                else
                {
                    // we've wrapped around.
                    if (slotPrev >= slotOther && slotOther > slotCurr)
                        break;
                }
                slotCurr = (slotCurr + 1) & mask;
            }

            if (!allocated[slotCurr]) 
                break;

            // Shift key/value pair.
            keys[slotPrev] = keys[slotCurr];
        }

        allocated[slotPrev] = false;
        
        /* removeIf:primitiveKType */ 
        keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue(); 
        /* end:removeIf */
    }
    
    /**
     * Returns the last value saved in a call to {@link #contains}.
     * 
     * @see #contains
     */
    public KType lget()
    {
        assert lastSlot >= 0 : "Call contains() first.";
        assert allocated[lastSlot] : "Last call to exists did not have any associated value.";
    
        return keys[lastSlot];
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Saves the associated value for fast access using {@link #lget()}.</p>
     * <pre>
     * if (map.contains(key))
     *   value = map.lget(); 
     * </pre>
     */
    @Override
    public boolean contains(KType key)
    {
        final int mask = allocated.length - 1;
        int slot = rehash(key) & mask;
        while (allocated[slot])
        {
            if (/* replaceIf:primitiveKType 
                (keys[slot] == key) */ 
                 key == null ? keys[slot] == null : key.equals(keys[slot]) /* end:replaceIf */)
            {
                lastSlot = slot;
                return true; 
            }
            slot = (slot + 1) & mask;
        }
        lastSlot = -1;
        return false;
    }

    /**
     * Round the capacity to the next allowed value. 
     */
    protected int roundCapacity(int requestedCapacity)
    {
        // Maximum positive integer that is a power of two.
        if (requestedCapacity > (0x80000000 >>> 1))
            return (0x80000000 >>> 1);

        int capacity = MIN_CAPACITY;
        while (capacity < requestedCapacity)
            capacity <<= 1;

        return capacity;
    }

    /**
     * Return the next possible capacity, counting from the current buffers'
     * size.
     */
    protected int nextCapacity(int current)
    {
        assert current > 0 && Long.bitCount(current) == 1
            : "Capacity must be a power of two.";
        assert ((current << 1) > 0) 
            : "Maximum capacity exceeded (" + (0x80000000 >>> 1) + ").";

        if (current < MIN_CAPACITY / 2) current = MIN_CAPACITY / 2;
        return current << 1;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Does not release internal buffers.</p>
     */
    @Override
    public void clear()
    {
        assigned = 0;

        Arrays.fill(allocated, false);
        Arrays.fill(keys, Intrinsics.<KType>defaultKTypeValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return assigned;
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
    public int hashCode()
    {
        int h = 0;

        final KType [] keys = this.keys;
        final boolean [] states = this.allocated;
        for (int i = states.length; --i >= 0;)
        {
            if (states[i])
            {
                h += rehash(keys[i]);
            }
        }

        return h;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    /* removeIf:primitive */ 
    @SuppressWarnings("unchecked") 
    /* end:removeIf */
    public boolean equals(Object obj)
    {
        if (obj != null)
        {
            if (obj == this) return true;

            if (obj instanceof ObjectSet<?>)
            {
                ObjectSet<Object> other = (ObjectSet<Object>) obj;
                if (other.size() == this.size())
                {
                    for (ObjectCursor<KType> c : this)
                    {
                        if (!other.contains(c.value))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * An iterator implementation for {@link #iterator}.
     */
    private final class EntryIterator implements Iterator<ObjectCursor<KType>>
    {
        private final static int NOT_CACHED = -1;
        private final static int AT_END = -2;

        private final ObjectCursor<KType> cursor;

        /** The next valid index or {@link #NOT_CACHED} if not available. */
        private int nextIndex = NOT_CACHED;

        public EntryIterator()
        {
            cursor = new ObjectCursor<KType>();
            cursor.index = NOT_CACHED;
        }

        public boolean hasNext()
        {
            if (nextIndex == NOT_CACHED)
            {
                // Advance from current cursor's position.
                int i = cursor.index + 1;
                while (i < keys.length && !allocated[i])
                {
                    i++;
                }
                nextIndex = (i != keys.length ? i : AT_END);
            }

            return nextIndex != AT_END;
        }

        public ObjectCursor<KType> next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            cursor.index = nextIndex;
            cursor.value = keys[nextIndex];

            nextIndex = NOT_CACHED;
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
     * {@inheritDoc}
     */
    @Override
    public Iterator<ObjectCursor<KType>> iterator()
    {
        return new EntryIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends ObjectProcedure<? super KType>> T forEach(T procedure)
    {
        final KType [] keys = this.keys;
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i])
                procedure.apply(keys[i]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /* replaceIf:primitive 
    public final KType [] toArray() */
    public final Object [] toArray()
    /* end:replaceIf */
    {
        final KType [] cloned = Intrinsics.newKTypeArray(assigned);
        for (int i = 0, j = 0; i < keys.length; i++)
            if (allocated[i])
                cloned[j++] = keys[i];

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectOpenHashSet<KType> clone()
    {
        try
        {
            @SuppressWarnings("unchecked")
            ObjectOpenHashSet<KType> cloned = (ObjectOpenHashSet<KType>) super.clone();
            cloned.keys = keys.clone();
            cloned.allocated = allocated.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends ObjectPredicate<? super KType>> T forEach(T predicate)
    {
        final KType [] keys = this.keys;
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i])
            {
                if (!predicate.apply(keys[i]))
                    break;
            }
        }

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(ObjectPredicate<? super KType> predicate)
    {
        final KType [] keys = this.keys;
        final boolean [] allocated = this.allocated;

        int before = assigned;
        for (int i = 0; i < allocated.length;)
        {
            if (allocated[i])
            {
                if (predicate.apply(keys[i]))
                {
                    assigned--;
                    shiftConflictingKeys(i);
                    // Repeat the check for the same i.
                    continue;
                }
            }
            i++;
        }

        return before - this.assigned;
    }

    /**
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static /* removeIf:primitive */<KType> /* end:removeIf */ 
      ObjectOpenHashSet<KType> from(KType... elements)
    {
        final ObjectOpenHashSet<KType> set = new ObjectOpenHashSet<KType>(
            (int) (elements.length * (1 + DEFAULT_LOAD_FACTOR)));
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static /* removeIf:primitive */<KType> /* end:removeIf */ 
        ObjectOpenHashSet<KType> from(ObjectContainer<KType> container)
    {
        return new ObjectOpenHashSet<KType>(container);
    }
}
