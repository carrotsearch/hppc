package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.hash.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

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
 *                                               keySet, pseudo-closures</td></tr>
 * </tbody>
 * </table>
 * 
 * <p>This implementation supports <code>null</code> keys and values in generic 
 * versions.</p>
 * 
 * <p><b>Important node.</b> The implementation uses power-of-two tables, which may
 * cause poor performance (many collisions) if hash values differ in higher bits only.
 * If unsure about the input data distribution, use a well-mixing hash function. 
 * This implementation uses {@link ObjectMurmurHash} for keys by 
 * default (primitive derivatives are provided in HPPC for convenience). For the needs
 * of {@link #hashCode()} and {@link #equals}, a separate hash function for values is provided.
 * The default hash function for values is {@link ObjectHashFunction}, which is consistent
 * with Java types default {@link #hashCode()}.</p>
 * 
 * @author This code is partially inspired by the implementation found in the <a
 *         href="http://code.google.com/p/google-sparsehash/">Google sparsehash</a>
 *         project.
 */
public class ObjectObjectOpenHashMap<KType, VType>
    implements ObjectObjectMap<KType, VType>
{
    /* removeIf:primitiveKType */
    /**
     * Static key comparator for generic key sets.
     */
    private final static Comparator<Object> EQUALS_COMPARATOR = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            return Intrinsics.equals(o1, o2) ? 0 : 1;
        }
    };
    /* end:removeIf */

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
    public final /* replaceIf:primitiveKType UKTypeHashFunction */ ObjectHashFunction<? super KType> /* end:replaceIf */ keyHashFunction;

    /**
     * Hash function for values.
     */
    public final /* replaceIf:primitiveVType UVTypeHashFunction */ ObjectHashFunction<? super VType> /* end:replaceIf */ valueHashFunction;

    /* removeIf:primitiveKType */
    /**
     * Key comparator function. We're only interested in comparator returning 0 (equals) or
     * non zero (not equals).
     */
    public final Comparator<? super KType> keyComparator;
    /* end:removeIf */

    /**
     * Lazily initialized view of the keys.
     */
    private KeySet keySetView;

    /**
     * Creates a hash map with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR} and the default hash function
     * {@link ObjectMurmurHash}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public ObjectObjectOpenHashMap()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a hash map with the given initial capacity, default load factor of
     * {@value #DEFAULT_LOAD_FACTOR} and hash function from {@link ObjectMurmurHash}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     */
    public ObjectObjectOpenHashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash map with the given initial capacity,
     * load factor and hash function {@link ObjectMurmurHash}.
     * 
     * <p>See class notes about hash distribution importance.</p>
     * 
     * @param initialCapacity Initial capacity (greater than zero and automatically
     *            rounded to the next power of two).
     *
     * @param loadFactor The load factor (greater than zero and smaller than 1).
     */
    public ObjectObjectOpenHashMap(int initialCapacity, float loadFactor)
    {
        this(initialCapacity, loadFactor, 
            /* replaceIf:primitiveKType new UKTypeMurmurHash() */ new ObjectMurmurHash() /* end:replaceIf */);
    }

    /**
     * Creates a hash map with the given predefined capacity. The actual allocated
     * capacity is always rounded to the next power of two.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public ObjectObjectOpenHashMap(
        int initialCapacity, float loadFactor, 
        /* replaceIf:primitiveKType UKTypeHashFunction */ ObjectHashFunction<? super KType> /* end:replaceIf */ keyHashFunction)
    {
        this(initialCapacity, loadFactor, keyHashFunction, 
            /* replaceIf:primitiveVType new UVTypeHashFunction() */ new ObjectHashFunction<VType>() /* end:replaceIf */
            /* removeIf:primitiveKType */, EQUALS_COMPARATOR /* end:removeIf */);
    }

    /**
     * Creates a hash map with the given predefined capacity. The actual allocated
     * capacity is always rounded to the next power of two.
     * 
     * <p>See class notes about hash distribution importance.</p>
     */
    public ObjectObjectOpenHashMap(
        int initialCapacity, float loadFactor,
        /* replaceIf:primitiveKType UKTypeHashFunction */ ObjectHashFunction<? super KType> /* end:replaceIf */ keyHashFunction,
        /* replaceIf:primitiveVType UVTypeHashFunction */ ObjectHashFunction<? super VType> /* end:replaceIf */ valueHashFunction
        /* removeIf:primitiveKType */, Comparator<? super KType> keyComparator /* end:removeIf */
        )
    {
        initialCapacity = Math.max(initialCapacity, MIN_CAPACITY);

        assert initialCapacity > 0
            : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";
        assert loadFactor > 0 && loadFactor <= 1
            : "Load factor must be between (0, 1].";

        /* removeIf:primitiveKType */
        assert keyComparator != null : "Key comparator must not be null.";
        this.keyComparator = keyComparator;
        /* end:removeIf */

        this.valueHashFunction = valueHashFunction;
        this.keyHashFunction = keyHashFunction;
        this.loadFactor = loadFactor;
        allocateBuffers(roundCapacity(initialCapacity));
    }
    
    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public ObjectObjectOpenHashMap(ObjectObjectAssociativeContainer<KType, VType> container)
    {
        this((int)(container.size() * (1 + DEFAULT_LOAD_FACTOR)));
        putAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public final int putAll(
        ObjectObjectAssociativeContainer<? extends KType, ? extends VType> container)
    {
        int count = this.assigned;
        
        for (ObjectObjectCursor<? extends KType, ? extends VType> c : container)
        {
            put(c.key, c.value);
        }

        return this.assigned - count;
    }

    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code:
     * <pre>
     * if (!map.hasKey(key)) map.put(value);
     * </pre>
     * 
     * @param key The key of the value to check.
     * @param value The value to put if <code>key</code> does not exist.
     * @return <code>true</code> if <code>key</code> did not exist and <code>value</code>
     * was placed in the map.
     */
    public final boolean putIfAbsent(KType key, VType value)
    {
        if (!containsKey(key))
        {
            put(key, value);
            return true;
        }
        return false;
    }

    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code (although a bit faster):
     * <pre>
     * if (map.containsKey(key)) 
     *    map.lset(map.lget() + additionValue);
     * else
     *    map.put(key, putValue);
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param putValue The value to put if <code>key</code> does not exist.
     * @param additionValue The value to add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    /* replaceIf:primitiveVType
    public final VType putOrAdd(KType key, VType putValue, VType additionValue)
    {
        // Eagerly check and make sure there is enough room for an update, if needed.
        if (assigned + deleted >= resizeThreshold)
            expandAndRehash();

        final int slot = slotFor(key);
        final byte state = states[slot];

        if (state == ASSIGNED)
        {
            return values[slot] += additionValue; 
        }
        else
        {
            // Must have been EMPTY or DELETED, we increase the assigned count.
            assigned++;
            // If DELETED, we decrease the deleted count.
            if (state == DELETED) deleted--;

            states[slot] = ASSIGNED;
            keys[slot] = key;

            return values[slot] = putValue;
        }
    }
    *//* end:replaceIf */

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

                /* Nullify while accessing these memory regions to use cpu cache. */

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

        this.resizeThreshold = (int) (capacity * loadFactor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType remove(KType key)
    {
        final int slot = slotFor(key);

        final VType value = values[slot];
        final byte state = states[slot]; 
        if (state == ASSIGNED)
        {
            deleted++;
            assigned--;

            keys[slot] = Intrinsics.<KType>defaultKTypeValue();
            values[slot] = Intrinsics.<VType>defaultVTypeValue();
            states[slot] = DELETED;
        }
        else
        {
            assert (Intrinsics.defaultVTypeValue() == value) : "Default value expected.";
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int removeAll(ObjectContainer<? extends KType> container)
    {
        final int before = this.deleted;

        for (ObjectCursor<? extends KType> cursor : container)
        {
            remove(cursor.value);
        }

        return this.deleted - before;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int removeAll(ObjectPredicate<? super KType> predicate)
    {
        final int before = this.deleted;

        final KType [] keys = this.keys;
        final VType [] values = this.values;
        final byte [] states = this.states;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == ASSIGNED && predicate.apply(keys[i]))
            {
                deleted++;
                assigned--;

                keys[i] = Intrinsics.<KType>defaultKTypeValue();
                values[i] = Intrinsics.<VType>defaultVTypeValue();
                states[i] = DELETED;
            }
        }

        return this.deleted - before;
    }

    /**
     * {@inheritDoc}
     * 
     * <p> Use the following snippet of code to check for key existence
     * first and then retrieve the value if it exists.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget(); 
     * </pre>
     */
    @Override
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
     * {@inheritDoc}
     * 
     * <p>Saves the associated value for fast access using {@link #lget}
     * or {@link #lset}.</p>
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
    @Override
    public boolean containsKey(KType key)
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

        int slot = keyHashFunction.hash(key) & bucketMask;
        int i = 0;
        int deletedSlot = -1;

        while (true)
        {
            final int state = states[slot];
            
            if (state == ObjectObjectOpenHashMap.EMPTY)
                return deletedSlot != -1 ? deletedSlot : slot;

            if (state == ObjectObjectOpenHashMap.ASSIGNED && 
                /* replaceIf:primitiveKType (keys[slot] == key) */ 
                keyComparator.compare(keys[slot], key) == 0 /* end:replaceIf */ )
            {
                return slot;
            }

            if (state == ObjectObjectOpenHashMap.DELETED && deletedSlot < 0)
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
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return assigned;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Note that an empty container may still contain many deleted keys (that occupy buffer
     * space). Adding even a single element to such a container may cause rehashing.</p>
     */
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
        for (ObjectObjectCursor<KType, VType> c : this)
        {
            h += keyHashFunction.hash(c.key) + valueHashFunction.hash(c.value);
        }
        return h;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    /* replaceIf:allprimitives */
    @SuppressWarnings("unchecked")
    /* end:replaceIf */
    public boolean equals(Object obj)
    {
        if (obj != null)
        {
            if (obj == this) return true;

            if (obj instanceof ObjectObjectMap)
            {
                ObjectObjectMap<KType, VType> other = (ObjectObjectMap<KType, VType>) obj;
                if (other.size() == this.size())
                {
                    for (ObjectObjectCursor<KType, VType> c : this)
                    {
                        if (other.containsKey(c.key))
                        {
                            VType v = other.get(c.key);
                            if (Intrinsics.equals(c.value, v))
                            {
                                continue;
                            }
                        }
                        return false;
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
     * {@inheritDoc}
     */
    @Override
    public Iterator<ObjectObjectCursor<KType, VType>> iterator()
    {
        return new EntryIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends ObjectObjectProcedure<? super KType, ? super VType>> T forEach(T procedure)
    {
        final KType [] keys = this.keys;
        final VType [] values = this.values;
        final byte [] states = this.states;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == ASSIGNED)
                procedure.apply(keys[i], values[i]);
        }
        
        return procedure;
    }

    /**
     * Returns a specialized view of the keys of this associated container. The view additionally
     * implements {@link ObjectLookupContainer}.
     */
    public KeySet keySet()
    {
        if (keySetView == null)
            keySetView = new KeySet();
        return keySetView;
    }

    /**
     * A view of the keys inside this hash map.
     */
    public final class KeySet 
        extends AbstractObjectCollection<KType> implements ObjectLookupContainer<KType>
    {
        private final ObjectObjectOpenHashMap<KType, VType> owner = 
            ObjectObjectOpenHashMap.this;
        
        @Override
        public boolean contains(KType e)
        {
            return containsKey(e);
        }
        
        @Override
        public <T extends ObjectProcedure<? super KType>> T forEach(T procedure)
        {
            final KType [] localKeys = owner.keys;
            final byte [] localStates = owner.states;

            for (int i = 0; i < localStates.length; i++)
            {
                if (localStates[i] == ASSIGNED)
                    procedure.apply(localKeys[i]);
            }

            return procedure;
        }

        @Override
        public <T extends ObjectPredicate<? super KType>> T forEach(T predicate)
        {
            final KType [] localKeys = owner.keys;
            final byte [] localStates = owner.states;

            for (int i = 0; i < localStates.length; i++)
            {
                if (localStates[i] == ASSIGNED)
                {
                    if (!predicate.apply(localKeys[i]))
                        break;
                }
            }

            return predicate;
        }

        @Override
        public boolean isEmpty()
        {
            return owner.isEmpty();
        }

        @Override
        public Iterator<ObjectCursor<KType>> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public int size()
        {
            return owner.size();
        }

        @Override
        public void clear()
        {
            owner.clear();
        }

        @Override
        public int removeAll(ObjectPredicate<? super KType> predicate)
        {
            return owner.removeAll(predicate);
        }

        @Override
        public int removeAllOccurrences(final KType e)
        {
            final boolean hasKey = owner.containsKey(e);
            int result = 0;
            if (hasKey)
            {
                owner.remove(e);
                result = 1;
            }
            return result;
        }
    };
    
    /**
     * An iterator over the set of assigned keys.
     */
    private final class KeySetIterator implements Iterator<ObjectCursor<KType>>
    {
        private final static int NOT_CACHED = -1;
        private final static int AT_END = -2;

        private final ObjectCursor<KType> cursor;

        /** The next valid index or {@link #NOT_CACHED} if not available. */
        private int nextIndex = NOT_CACHED;

        public KeySetIterator()
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
             * Use closures and other more efficient alternatives.
             */
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Convert the contents of this map to a human-friendly string. 
     */
    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[");

        boolean first = true;
        for (ObjectObjectCursor<KType, VType> cursor : this)
        {
            if (!first) buffer.append(", ");
            buffer.append(cursor.key);
            buffer.append("=>");
            buffer.append(cursor.value);
            first = false;
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Creates a hash map from two index-aligned arrays of key-value pairs. 
     */
    public static
        /* replaceWith:genericSignature */ <KType, VType> /* end:replaceWith */
        ObjectObjectOpenHashMap<KType, VType> from(KType [] keys, VType [] values)
    {
        if (keys.length != values.length) 
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length."); 

        ObjectObjectOpenHashMap<KType, VType> map = new ObjectObjectOpenHashMap<KType, VType>();
        for (int i = 0; i < keys.length; i++)
        {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container.
     */
    public static 
        /* replaceWith:genericSignature */ <KType, VType> /* end:replaceWith */
        ObjectObjectOpenHashMap<KType, VType> from(
            ObjectObjectAssociativeContainer<KType, VType> container)
    {
        return new ObjectObjectOpenHashMap<KType, VType>(container);
    }
}
