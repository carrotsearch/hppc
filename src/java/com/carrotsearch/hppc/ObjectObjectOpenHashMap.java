package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.hash.HashFunctionObject;

/**
 * A hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with quadratic collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}, {@link #values},
 * {@link #states}) are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 *
 * <p>
 * A brief comparison of the API against the Java Collections framework:
 * </p>
 * <table class="nice" summary="Java Collections HashMap and HPPC ObjectObjectOpenHashMap, related methods.">
 * <caption>Java Collections HashMap and HPPC {@link ObjectObjectOpenHashMap}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain HashMap java.util.HashMap}</th>
 *         <th scope="col">{@link ObjectObjectOpenHashMap}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>V put(K)       </td><td>V put(K)      </td></tr>
 * <tr class="odd"><td>V get(K)       </td><td>V get(K)      </td></tr>
 * <tr            ><td>V remove(K)    </td><td>V remove(K)   </td></tr>
 * <tr class="odd"><td>size, clear, 
 *                     isEmpty</td><td>size, clear, isEmpty</td></tr>                     
 * <tr            ><td>containsKey(K) </td><td>containsKey(K), lget()</td></tr>
 * <tr class="odd"><td>containsValue(K) </td><td>(no efficient equivalent)</td></tr>
 * <tr            ><td>keySet, entrySet </td><td>{@linkplain #iterator() iterator} over map entries,
 *                                               pseudo-closures</td></tr>
 * </tbody>
 * </table>
 * 
 * <p>This implementation supports <code>null</code> keys and values in generic 
 * versions.</p>
 * 
 * @author This code is partially inspired by the implementation found in the <a
 *         href="http://code.google.com/p/google-sparsehash/">Google sparsehash</a>
 *         project.
 */
public class ObjectObjectOpenHashMap<KType, VType>
    implements Iterable<ObjectObjectCursor<KType, VType>>
{
    /**
     * Default capacity.
     */
    public final static int DEFAULT_CAPACITY = 16;

    /**
     * Minimum capacity for the map.
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
     * Hash-indexed array holding all keys.
     * 
     * @see #states
     * @see #values
     */
    public KType [] keys;

    /**
     * Hash-indexed array holding all values associated to the keys
     * stored in {@link #keys}.
     * 
     * @see #states
     * @see #keys
     */
    public VType [] values;

    /**
     * Each entry (slot) in the {@link #values} table has an associated state 
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
     * The load factor for this map (fraction of allocated or deleted slots
     * before the buffers must be rehashed or reallocated).
     */
    public final float loadFactor;

    /**
     * Cached capacity threshold at which we must resize the buffers. 
     */
    private int resizeThreshold;

    /**
     * The most recent slot accessed in {@link #containsKey} (required for
     * {@link #lget}).
     * 
     * @see #containsKey
     * @see #lget
     */
    private int lastSlot;

    /**
     * Hash function for keys.
     */
    public final HashFunctionObject hashFunction;
    
    /**
     * Creates a hash map with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR} and hash function
     * from {@link HashFunctionObject}.
     */
    public ObjectObjectOpenHashMap()
    {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, new HashFunctionObject());
    }

    /**
     * Creates a hash map with the given initial capacity, default
     * load factor of {@value #DEFAULT_LOAD_FACTOR} and hash function
     * from {@link HashFunctionObject}.
     */
    public ObjectObjectOpenHashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, new HashFunctionObject());
    }

    /**
     * Creates a hash map with the given initial capacity,
     * load factor and hash function from {@link HashFunctionObject}.
     */
    public ObjectObjectOpenHashMap(int initialCapacity, float loadFactor)
    {
        this(initialCapacity, loadFactor, new HashFunctionObject());
    }

    /**
     * Creates a hash map with the given predefined capacity. The actual allocated
     * capacity is always rounded to the next power of two.
     */
    public ObjectObjectOpenHashMap(
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
     * Place a given key and value in the hash map. The value previously
     * stored under the given key in the hash map is returned.
     */
    public VType put(KType key, VType value)
    {
        if (assigned + deleted >= resizeThreshold)
            expandAndRehash();

        final int slot = slotFor(key);
        final byte state = states[slot];

        // If EMPTY or DELETED, we increase the assigned count.
        if (state != ASSIGNED) assigned++;
        // If DELETED, we decrease the deleted count.
        if (state == DELETED) deleted--;

        final VType oldValue = values[slot]; 
        keys[slot] = key;
        values[slot] = value;
        states[slot] = ASSIGNED;

        return oldValue;
    }

    /**
     * Puts all keys from an iterable cursor to this map, replacing the values
     * of existing keys, if such keys are present.   
     * 
     * @param iterator An iterator returning a cursor over KType keys and VType values. 
     * @return Returns the number of keys added to the map as a result of this
     * call (not previously present in the map). Values of existing keys are overwritten.
     */
    public final int putAll(
        Iterator<? extends ObjectObjectCursor<? extends KType, ? extends VType>> iterator)
    {
        int count = this.assigned;
        while (iterator.hasNext())
        {
            final ObjectObjectCursor<? extends KType, ? extends VType> c 
                = iterator.next();
            put(c.key, c.value);
        }

        return this.assigned - count;
    }

    /**
     * Puts all keys from an iterable cursor to this map, replacing the values
     * of existing keys, if such keys are present.
     *    
     * @see #putAll(Iterator)
     */
    public final int putAll(
        Iterable<? extends ObjectObjectCursor<? extends KType, ? extends VType>> iterable)
    {
        return putAll(iterable.iterator());
    }

    /**
     * Expand the internal storage buffers (capacity) or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    private void expandAndRehash()
    {
        final KType [] oldKeys = this.keys;
        final VType [] oldValues = this.values;
        final byte [] oldStates = this.states;

        if (assigned >= resizeThreshold)
        {
            allocateBuffers(nextCapacity(keys.length));
        }
        else
        {
            allocateBuffers(this.values.length);
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
                values[slot] = oldValues[i];
                states[slot] = ASSIGNED;

                /* removeIf:primitiveKType */
                oldKeys[i] = null; 
                /* end:removeIf */
                /* removeIf:primitiveVType */
                oldValues[i] = null;
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
        this.values = Intrinsics.newVTypeArray(capacity);
        this.states = new byte [capacity];

        this.resizeThreshold = (int) (capacity * DEFAULT_LOAD_FACTOR);
    }

    /**
     * Remove the value at the given key, if it exists.
     */
    public VType remove(KType key)
    {
        final int slot = slotFor(key);

        final VType value = values[slot];
        if (states[slot] == ASSIGNED)
        {
            deleted++;
            assigned--;

            keys[slot] = Intrinsics.<KType>defaultKTypeValue();
            values[slot] = Intrinsics.<VType>defaultVTypeValue();
            states[slot] = DELETED;
        }

        return value;
    }

    /**
     * Removes all keys present in a given iterator.
     * 
     * @param iterator An iterator returning a cursor over a collection of KType elements. 
     * @return Returns the number of elements actually removed as a result of this
     * call.
     */
    public final int removeAllKeysIn(Iterator<? extends ObjectCursor<? extends KType>> iterator)
    {
        int before = this.deleted;
        while (iterator.hasNext())
        {
            remove(iterator.next().value);
        }

        return this.deleted - before;
    }

    /**
     * Removes all keys present in an iterable.
     * 
     * @see #removeAllKeysIn(Iterator)
     */
    public final int removeAllKeysIn(Iterable<? extends ObjectCursor<? extends KType>> iterable)
    {
        return removeAllKeysIn(iterable.iterator());
    }

    /**
     * Return the value at the given slot or the default value
     * for a given value type, if the key does not exist. Use 
     * the following snippet of code to check for key existence
     * first and then retrieve the value if it exists.
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget(); 
     * </pre>
     */
    public VType get(KType key)
    {
        return values[slotFor(key)];
    }

    /**
     * Returns the last value saved in a call to {@link #containsKey}.
     * 
     * @see #containsKey
     */
    public VType lget()
    {
        assert lastSlot >= 0 : "Call containsKey() first.";
        assert states[lastSlot] == ASSIGNED 
            : "Last call to exists did not have any associated value.";
    
        return values[lastSlot];
    }

    /**
     * Sets the value corresponding to the key saved in the last
     * call to {@link #containsKey}, if and only if the key exists
     * in the map already.
     * 
     * @see #containsKey
     * @return Returns the previous value stored under the given key.
     */
    public VType lset(VType key)
    {
        assert lastSlot >= 0 : "Call containsKey() first.";
        assert states[lastSlot] == ASSIGNED 
            : "Last call to exists did not have any associated value.";

        final VType previous = values[lastSlot];
        values[lastSlot] = key;
        return previous;
    }

    /**
     * Return <code>true</code> if the key exists in the map and
     * save the associated value for fast access using {@link #lget}
     * or {@link #lset}.
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget();
     * </pre>
     * or, for example to modify the value at the given key without looking up
     * its slot twice:
     * <pre>
     * if (map.containsKey(key))
     *   map.lset(map.lget() + 1);
     * </pre>
     */
    public boolean containsKey(KType key)
    {
        final int slot = (lastSlot = slotFor(key));
        return states[slot] == ASSIGNED;
    }

    /**
     * An alias for {@link #containsKey}. 
     */
    public final boolean hasKey(KType key)
    {
        return containsKey(key);
    }

    /**
     * Round the capacity to the next allowed value. 
     */
    protected int roundCapacity(int requestedCapacity)
    {
        // Maximum positive integer that is a power of two.
        if (requestedCapacity > (0x80000000 >>> 1))
            return (0x80000000 >>> 1);

        return Math.max(MIN_CAPACITY, BitUtil.nextHighestPowerOfTwo(requestedCapacity));
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
     * {@link ObjectObjectOpenHashMap#values}. This method implements quadratic slot
     * lookup under the assumption that the number of slots (
     * <code>{@link ObjectObjectOpenHashMap#values}.length</code>) is a power of two.
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
            
            if (state == ObjectObjectOpenHashMap.EMPTY)
                return deletedSlot != -1 ? deletedSlot : slot;

            if (state == ObjectObjectOpenHashMap.ASSIGNED && Intrinsics.equals(keys[slot], key))
                return slot;

            if (state == ObjectObjectOpenHashMap.DELETED && deletedSlot < 0)
                deletedSlot = slot;

            slot = (slot + (++i)) & bucketMask;
        }
    }

    /**
     * Clear all keys and values in the hash map, without releasing the 
     * current buffers.
     */
    public void clear()
    {
        assigned = deleted = 0;

        // States are always cleared.
        Arrays.fill(states, EMPTY);

        /* removeIf:primitiveVType */
        Arrays.fill(values, null); // Help the GC.
        /* end:removeIf */

        /* removeIf:primitiveKType */
        Arrays.fill(keys, null); // Help the GC.
        /* end:removeIf */
    }

    /**
     * @return Return the current size (number of assigned keys) in the hash map.
     */
    public int size()
    {
        return assigned;
    }

    /**
     * @return Return <code>true</code> if this hash map contains no assigned keys.
     * Note that an empty hash map may still contain many deleted keys (that keep buffer
     * space).
     */
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
    private final class EntryIterator implements Iterator<ObjectObjectCursor<KType, VType>>
    {
        private final static int NOT_CACHED = -1;
        private final static int AT_END = -2;

        private final ObjectObjectCursor<KType, VType> cursor;

        /** The next valid index or {@link #NOT_CACHED} if not available. */
        private int nextIndex = NOT_CACHED;

        public EntryIterator()
        {
            cursor = new ObjectObjectCursor<KType, VType>();
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

        public ObjectObjectCursor<KType, VType> next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            cursor.index = nextIndex;
            cursor.key = keys[nextIndex];
            cursor.value = values[nextIndex];

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
     * Returns a cursor over the entries (key-value pairs) in this map. The iterator is
     * implemented as a cursor and it returns <b>the same cursor instance</b> on every
     * call to {@link Iterator#next()}. To read the current key and value (or index inside
     * internal buffers in the map) use the cursor's public fields. An example is shown below.
     * 
     * <pre>
     * for (IntShortCursor c : intShortMap)
     * {
     *     System.out.println(&quot;index=&quot; + c.index 
     *       + &quot; key=&quot; + c.key
     *       + &quot; value=&quot; + c.value);
     * }
     * </pre>
     * 
     * @see #values()
     */
    public Iterator<ObjectObjectCursor<KType, VType>> iterator()
    {
        return new EntryIterator();
    }

    /**
     * Returns an iterable view of the entries (key-value pairs) in this map. Effectively
     * an alias for <code>this</code> because {@link ObjectObjectOpenHashMap} is already
     * iterable over the stored entries.
     * 
     * @see #iterator()
     */
    public Iterable<ObjectObjectCursor<KType, VType>> values()
    {
        return this;
    }

    /**
     * Applies <code>procedure</code> to all entries (key, value pairs) in this map.
     *
     * @see "HPPC benchmarks." 
     */
    public void forEach(ObjectObjectProcedure<? super KType, ? super VType> procedure)
    {
        final KType [] keys = this.keys;
        final VType [] values = this.values;
        final byte [] states = this.states;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == ASSIGNED)
                procedure.apply(keys[i], values[i]);
        }
    }
}
