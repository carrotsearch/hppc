package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.hash.HashFunctionObject;
import com.carrotsearch.hppc.predicates.ObjectPredicate;
import com.carrotsearch.hppc.procedures.ObjectProcedure;

/**
 * A hash set of <code>KType</code>s, implemented using open addressing with 
 * quadratic collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}), {@link #states})
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
 */
public class ObjectOpenHashSet<KType>
    extends AbstractObjectCollection<KType> 
    implements ObjectLookupContainer<KType>, ObjectSet<KType>
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
     * A marker for an empty slot in {@link #keys}, stored in {@link #states}. 
     */
    public final static byte EMPTY = 0;

    /** 
     * A marker for a deleted slot in {@link #keys}, stored in {@link #states}. 
     */
    public final static byte DELETED = 1;

    /** 
     * A marker for an assigned slot in {@link #keys}, stored in {@link #states}. 
     */
    public final static byte ASSIGNED = 2;  

    /**
     * Hash-indexed array holding all set entries.
     * 
     * @see #states
     */
    public KType [] keys;

    /**
     * Each entry (slot) in the {@link #keys} table has an associated state 
     * information ({@link #EMPTY}, {@link #ASSIGNED} or {@link #DELETED}).
     * 
     * @see #deleted
     * @see #assigned
     */
    public byte [] states;

    /**
     * Cached number of deleted slots in {@link #states}.
     */
    public int deleted;

    /**
     * Cached number of assigned slots in {@link #states}.
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
     * Hash function for entries.
     */
    public final HashFunctionObject hashFunction;
    
    /**
     * Creates a hash set with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR} and hash function
     * from {@link HashFunctionObject}.
     */
    public ObjectOpenHashSet()
    {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, new HashFunctionObject());
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value #DEFAULT_LOAD_FACTOR} and hash function
     * from {@link HashFunctionObject}.
     */
    public ObjectOpenHashSet(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, new HashFunctionObject());
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor and hash function
     * from {@link HashFunctionObject}.
     */
    public ObjectOpenHashSet(int initialCapacity, float loadFactor)
    {
        this(initialCapacity, loadFactor, new HashFunctionObject());
    }

    /**
     * Creates a hash set with the given predefined capacity. The actual allocated
     * capacity is always rounded to the next power of two.
     */
    public ObjectOpenHashSet(
        int initialCapacity, float loadFactor, HashFunctionObject hashFunction)
    {
        assert initialCapacity > 0 && initialCapacity <= Integer.MAX_VALUE
            : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";
        assert loadFactor > 0 && loadFactor < 1
            : "Load factor must be between (0, 1).";

        this.hashFunction = hashFunction;
        this.loadFactor = loadFactor;
        allocateBuffers(roundCapacity(initialCapacity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(KType e)
    {
        if (assigned + deleted >= resizeThreshold)
            expandAndRehash();

        final int slot = slotFor(e);
        final byte state = states[slot];

        // If EMPTY or DELETED, we increase the assigned count.
        if (state != ASSIGNED) assigned++;
        // If DELETED, we decrease the deleted count.
        if (state == DELETED) deleted--;

        keys[slot] = e;
        states[slot] = ASSIGNED;

        return state != ASSIGNED;
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
     * Adds all elements from an iterable cursor to this set.
     * 
     * @param iterator An iterator returning a cursor over a collection of KType elements. 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public final int addAll(Iterator<? extends ObjectCursor<? extends KType>> iterator)
    {
        int count = 0;
        while (iterator.hasNext())
        {
            if (add(iterator.next().value)) count++;
        }

        return count;
    }

    /**
     * Adds all elements from an iterable cursor to this set.
     *
     * @see #addAll(Iterator)
     */
    public final int addAll(Iterable<? extends ObjectCursor<? extends KType>> iterable)
    {
        return addAll(iterable.iterator());
    }

    /**
     * Expand the internal storage buffers (capacity) or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    private void expandAndRehash()
    {
        final KType [] oldKeys = this.keys;
        final byte [] oldStates = this.states;

        if (assigned >= resizeThreshold)
        {
            allocateBuffers(nextCapacity(keys.length));
        }
        else
        {
            allocateBuffers(this.keys.length);
        }

        /*
         * Rehash all assigned slots from the old hash table. Deleted
         * slots are discarded.
         */
        for (int i = 0; i < oldStates.length; i++)
        {
            if (oldStates[i] == ASSIGNED)
            {
                final int slot = slotFor(oldKeys[i]);
                keys[slot] = oldKeys[i];
                states[slot] = ASSIGNED;

                /* removeIf:primitiveKType */
                oldKeys[i] = null; 
                /* end:removeIf */
            }
        }

        /*
         * The number of assigned items does not change, the number of deleted
         * items is zero since we have resized.
         */
        deleted = 0;
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
        this.states = new byte [capacity];

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
     * An alias for the (preferred) {@link #removeAllOccurrences(Object)}.
     */
    public boolean remove(KType key)
    {
        final int slot = slotFor(key);

        boolean hadEntry = false;
        if (states[slot] == ASSIGNED)
        {
            deleted++;
            assigned--;

            keys[slot] = Intrinsics.<KType>defaultKTypeValue();
            states[slot] = DELETED;

            hadEntry = true;
        }

        return hadEntry;
    }

    /**
     * Returns the last value saved in a call to {@link #contains}.
     * 
     * @see #contains
     */
    public KType lget()
    {
        assert lastSlot >= 0 : "Call contains() first.";
        assert states[lastSlot] == ASSIGNED 
            : "Last call to exists did not have any associated value.";
    
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
        final int slot = (lastSlot = slotFor(key));
        return states[slot] == ASSIGNED;
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
     * Lookup the slot index for <code>key</code> inside
     * {@link ObjectOpenHashSet#keys}. This method implements quadratic slot
     * lookup under the assumption that the number of slots (
     * <code>{@link ObjectOpenHashSet#keys}.length</code>) is a power of two.
     * Given this, the following formula yields a sequence of numbers with distinct values
     * between [0, slots - 1]. For a hash <code>h(k)</code> and the <code>i</code>-th
     * probe, where <code>i</code> is in <code>[0, slots - 1]</code>:
     * <pre>
     * h(k, i) = h(k) + (i + i^2) / 2   (mod slots)
     * </pre>
     * which can be rewritten recursively as:
     * <pre>
     * h(k, 0) = h(k),                (mod slots)
     * h(k, i + 1) = h(k, i) + i.     (mod slots)
     * </pre>
     * 
     * @see "http://en.wikipedia.org/wiki/Quadratic_probing"
     */
    public int slotFor(KType key)
    {
        final int slots = states.length;
        final int bucketMask = (slots - 1);

        // This is already verified when reallocating.
        // assert slots > 0 && Integer.bitCount(slots) == 1 : "Bucket count must be a power of 2.";

        int slot = hashFunction.hash(key) & bucketMask;
        int i = 0;
        int deletedSlot = -1;

        while (true)
        {
            final int state = states[slot];
            
            if (state == ObjectOpenHashSet.EMPTY)
                return deletedSlot != -1 ? deletedSlot : slot;

            if (state == ObjectOpenHashSet.ASSIGNED && Intrinsics.equals(keys[slot], key))
                return slot;

            if (state == ObjectOpenHashSet.DELETED && deletedSlot < 0)
                deletedSlot = slot;

            slot = (slot + (++i)) & bucketMask;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Does not release internal buffers.</p>
     */
    @Override
    public void clear()
    {
        assigned = deleted = 0;

        Arrays.fill(states, EMPTY);
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
            cursor.index = -1;
        }

        public boolean hasNext()
        {
            if (nextIndex == NOT_CACHED)
            {
                // Advance from current cursor's position.
                int i = cursor.index + 1;
                for (; i < keys.length; i++)
                {
                    if (states[i] == ASSIGNED)
                        break;
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
    public void forEach(ObjectProcedure<? super KType> procedure)
    {
        final KType [] keys = this.keys;
        final byte [] states = this.states;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == ASSIGNED)
                procedure.apply(keys[i]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final KType [] toArray()
    {
        final KType [] cloned = Intrinsics.newKTypeArray(assigned);
        for (int i = 0, j = 0; i < keys.length; i++)
            if (states[i] == ASSIGNED)
                cloned[j++] = keys[i];

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(ObjectPredicate<? super KType> predicate)
    {
        final KType [] keys = this.keys;
        final byte [] states = this.states;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == ASSIGNED)
            {
                if (!predicate.apply(keys[i]))
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(ObjectPredicate<? super KType> predicate)
    {
        final KType [] keys = this.keys;
        final byte [] states = this.states;

        int deleted = 0;
        try
        {
            for (int i = 0; i < states.length; i++)
            {
                if (states[i] == ASSIGNED)
                {
                    if (predicate.apply(keys[i]))
                    {
                        states[i] = DELETED;
                        deleted++;
                    }
                }
            }

            return deleted;
        }
        finally
        {
            this.assigned -= deleted;
            this.deleted += deleted;
        }
    }
}
