/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/**
 * A hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * <p><strong>Note:</strong> read about <a href="{@docRoot}/overview-summary.html#scattervshash">important differences
 * between hash and scatter sets</a>.</p>
 *
 * @see KTypeVTypeScatterMap
 * @see <a href="{@docRoot}/overview-summary.html#interfaces">HPPC interfaces diagram</a>
 */
/*! #if ($TemplateOptions.anyGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeWormMap<KType, VType>
        implements /*! #if ($templateonly) !*/ Intrinsics.EqualityFunction, /*! #end !*/
        /*! #if ($templateonly) !*/ Intrinsics.KeyHasher<KType>, /*! #end !*/
        KTypeVTypeMap<KType, VType>,
        Preallocable,
        Cloneable,
        Accountable
{
    /**
     * No {@link KTypeVTypeWormMap} can have higher capacity. Same value as the {@link HashMap} maximum capacity.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Default initial capacity used when none is provided in the constructor. Same value as the
     * {@link HashMap} default capacity.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * Target load factor for the {@link #trimToSize()} method. The method sets the map capacity according to the map
     * size and this load factor. If the map cannot fit within this capacity, it is enlarged and consequently the
     * obtained load factor is low. So this {@link #FIT_LOAD_FACTOR} must be chosen carefully to work in most cases.
     */
    private static final float FIT_LOAD_FACTOR = 0.85f;

    /**
     * The number of recursive move attempts per recursive call level. {@link #RECURSIVE_MOVE_ATTEMPTS}[i] = max number
     * of attempts at recursive level i. It must always end with 0 attempts at the last level. Used by
     * {@link #searchAndMoveMovableBucket} when trying to move entries recursively to
     * free a bucket instead of enlarging the map. The more attempts are allowed, the more the load factor increases,
     * but the performance decreases. It is a compromise between memory reduction and performance.
     */
    // Suppose to be final, but we have to remove it to be able to change values for tests.
    public static int[] RECURSIVE_MOVE_ATTEMPTS = {10, 1, 0};
//    private static final int[] RECURSIVE_MOVE_ATTEMPTS = {10, 1, 0};

    /**
     * Marks an entry at the end of a chain. This value is stored at {@link #next}[entryIndex].
     */
    private static final int END_OF_CHAIN = 127;

    /**
     * Enables costly assertions and integrity checks. Very useful to debug, but it must not be enabled otherwise.
     */
    private static final boolean DEBUG_ENABLED = false;

    /**
     * Print the load factor when resizing the map. Useful to determine a good value for {@link #FIT_LOAD_FACTOR}, but
     * it must not be enabled otherwise.
     */
    private static final boolean PRINT_LOAD_FACTOR = false;

    static {
        if (DEBUG_ENABLED)
            System.err.println(KTypeVTypeWormMap.class.getSimpleName() + " DEBUG ENABLED");
        if (PRINT_LOAD_FACTOR)
            System.err.println(KTypeVTypeWormMap.class.getSimpleName() + " PRINT LOAD FACTOR");
        assert RECURSIVE_MOVE_ATTEMPTS[RECURSIVE_MOVE_ATTEMPTS.length - 1] == 0 : "Recursive move attempts must be 0 for the last level";
    }

    private enum PutPolicy {
        /**
         * Always put. Create new key if absent, or replace existing value if present.
         */
        NEW_OR_REPLACE,
        /**
         * Always put and it is guaranteed that the key is absent.
         */
        NEW_GUARANTEED,
        /**
         * Put only if the key is absent. Don't replace existing value.
         */
        NEW_ONLY_IF_ABSENT,
        /**
         * Put only if the key is present. Only replace existing value.
         */
        REPLACE_ONLY_IF_PRESENT,
    }

    private enum NewEntryAdditionResult {
        SUCCESS, SUCCESS_MAP_ENLARGED, FAILURE
    }

    /**
     * The array holding keys.
     */
    public /*! #if ($TemplateOptions.KTypeGeneric) !*/
            Object[]
            /*! #else KType [] #end !*/
            keys;

    /**
     * The array holding values.
     */
    public /*! #if ($TemplateOptions.VTypeGeneric) !*/
            Object[]
            /*! #else VType [] #end !*/
            values;

    /**
     * abs({@link #next}[i])=offset to next chained entry index. <p>{@link #next}[i]=0 for free bucket.</p> <p>The
     * offset is always forward, and the array is considered circular, meaning that an entry at the end of the
     * array may point to an entry at the beginning with a positive offset.</p> <p>The offset is always forward, but the
     * sign of the offset encodes head/tail of chain. {@link #next}[i] &gt; 0 for the first head-of-chain entry (within
     * [1,{@link #maxOffset}]), {@link #next}[i] &lt; 0 for the subsequent tail-of-chain entries (within [-{@link
     * #maxOffset},-1]. For the last entry in the chain, abs({@link #next}[i])={@link #END_OF_CHAIN}.</p>
     */
    public byte[] next;

    /**
     * Map size (number of entries).
     */
    private int size;


    /**
     * Constructs a {@link KTypeVTypeWormMap} with the default initial capacity.
     */
    public KTypeVTypeWormMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs a {@link KTypeVTypeWormMap}.
     *
     * @param initialCapacity The initial capacity. It becomes internally the next power of 2.
     */
    public KTypeVTypeWormMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Invalid initialCapacity=" + initialCapacity);
        }
        if (initialCapacity == 0) {
            initialCapacity = DEFAULT_INITIAL_CAPACITY;
        }
        allocateBuffers(initialCapacity, false);
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeWormMap(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
        this(container.size());
        putAll(container);
    }

    /**
     * Creates a new {@link KTypeVTypeWormMap} with capacity adapted to store the given expected size without rehashing.
     *
     * @param expectedSize The expected number of entries the map will hold.
     */
    public static <KType, VType>KTypeVTypeWormMap<KType, VType> withExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Invalid expectedSize=" + expectedSize);
        }
        return new KTypeVTypeWormMap<KType, VType>(computeCapacityForSize(expectedSize));
    }

    /**
     * Creates a {@link KTypeVTypeWormMap} by copying another map. <p>If the other map is a {@link KTypeVTypeWormMap}, prefer
     * {@link #clone()} as it is more efficient.</p>
     *
     * @param map The other map to copy.
     * @see #clone()
     */
    public static <KType, VType>KTypeVTypeWormMap<KType, VType> copyOf(KTypeVTypeMap<KType, VType> map) {
        KTypeVTypeWormMap<KType, VType> wormMap = withExpectedSize(map.size());
        wormMap.putAll(map);
        return wormMap;
    }

    /**
     * Clones this map. The cloning operation is efficient because it copies directly the internal arrays, without
     * having to put entries in the cloned map. The cloned map has the same entries and the same {@link #getCapacity()
     * capacity} as this map.
     *
     * @return A shallow copy of this map.
     */
    @Override
    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "unchecked"})
    public KTypeVTypeWormMap<KType, VType> clone() {
        try {
            KTypeVTypeWormMap<KType, VType> cloneMap = (KTypeVTypeWormMap<KType, VType>) super.clone();
            cloneMap.keys = Arrays.copyOf(keys, keys.length);
            cloneMap.values = Arrays.copyOf(values, values.length);
            cloneMap.next = Arrays.copyOf(next, next.length);
            return cloneMap;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone must be supported", e);
        }
    }

    public static <KType, VType> KTypeVTypeWormMap<KType, VType> from(KType[] keys, VType[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
        }

        KTypeVTypeWormMap<KType, VType> map = new KTypeVTypeWormMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }

        return map;
    }

    public VType noValue() {
        /*! #if ($TemplateOptions.VTypeGeneric) !*/
        return null;
        /*! #else return 0; #end !*/
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    // Public methods start

    @Override
    public VType get(KType key) {
        // Compute the key hash index.
        int hashIndex = hash(key, next.length);
        int nextOffset = next[hashIndex];

        if (nextOffset <= 0)
        // The bucket is either free, or only used for chaining, so no entry for the key.
        {
            return noValue();
        }

        // The bucket contains a head-of-chain entry.
        // Look for the key in the chain.
        int entryIndex = searchInChain(key, hashIndex, nextOffset);

        // Return the value if an entry in the chain matches the key.
        return entryIndex < 0 ? noValue() : Intrinsics.<VType> cast(values[entryIndex]);
    }

    @Override
    public VType getOrDefault(KType key, VType defaultValue) {
        VType value;
        return (value = get(key)) == noValue() ? defaultValue : value;
    }

    @Override
    public VType put(KType key, VType value) {
        return put(key, value, PutPolicy.NEW_OR_REPLACE, noValue(), true);
    }

    @Override
    public int putAll(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
        final int count = size();
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : container) {
            put(c.key, c.value);
        }
        return size() - count;
    }

    @Override
    public int putAll(Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable) {
        final int count = size();
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable) {
            put(c.key, c.value);
        }
        return size() - count;
    }

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    // Can be overwritten using index system in case we implement support of them
    @Override
    public VType putOrAdd(KType key, VType putValue, VType incrementValue) {
        int keyIndex = indexOf(key);
        if (indexExists(keyIndex)) {
            putValue = Intrinsics.<VType> add(Intrinsics.<VType> cast(values[keyIndex]), incrementValue);
            indexReplace(keyIndex, putValue);
        } else {
            indexInsert(keyIndex, key, putValue);
        }
        return putValue;
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    @Override
    public VType addTo(KType key, VType additionValue) {
        return putOrAdd(key, additionValue, additionValue);
    }
    /*! #end !*/

//    public VType putIfAbsent(KType key, VType value) {
//        return put(key, value, PutPolicy.NEW_ONLY_IF_ABSENT, noValue(), true);
//    }

    public boolean putIfAbsent(KType key, VType value) {
        return noValue() == put(key, value, PutPolicy.NEW_ONLY_IF_ABSENT, noValue(), true);
    }

    @Override
    public VType remove(KType key) {
        return removeInner(key, noValue());
    }

    public boolean remove(KType key, VType value) {
        return value != noValue() && removeInner(key, value) != noValue();
    }

    public boolean removeValue(VType value) {
        final VType[] values = Intrinsics.<VType[]> cast(this.values);
        final byte[] next = this.next;
        final int size = this.size;

        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                if (Intrinsics.<VType> equals(values[index], value)) {
                    // An entry matches the value. Remove it.
                    remove(Intrinsics.<KType> cast(keys[index]));
                    return true;
                }
                entryCount++;
            }
        }
        return false;
    }

    @Override
    public int removeAll(KTypeContainer<? super KType> other) {
        int before = this.size();
        // Checks if container is bigger than number of keys
        if (other.size() >= this.size() && other instanceof KTypeLookupContainer<?>) {

            final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
            int slot = 0;
            int max = this.keys.length;

            // This outer loop was in original method, don't know if we still need it
            while (true) {
                // Going over all the keys they bound their complexity by capacity of the map instead of container size
                while (slot < max) {
                    KType existing = keys[slot];
                    if (next[slot] != 0 && other.contains(existing)) {
                        this.remove(existing);
                    } else {
                        slot++;
                    }
                }

                // returns number of removed elements
                return before - this.size();
            }
        } else {
            for (KTypeCursor<?> c : other) {
                this.remove(Intrinsics.<KType> cast(c.value));
            }

            // returns number of removed elements
            return before - this.size();
        }
    }

    @Override
    public int removeAll(KTypePredicate<? super KType>  predicate) {
        int before = this.size();

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        int slot = 0;
        int max = this.keys.length;

        // Again going over all the keys with using this weird shift function
        while (true) {
            while (slot < max) {
                KType existing = keys[slot];
                if (next[slot] != 0 && predicate.apply(existing)) {
                    this.remove(existing);
                } else {
                    ++slot;
                }
            }

            return before - this.size();
        }
    }

    @Override
    public int removeAll(KTypeVTypePredicate<? super KType, ? super VType> predicate) {
        // Same as above
        int before = this.size();
        int max = this.keys.length;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);
        int slot = 0;

        while (true) {
            while (slot < max) {
                KType existing = keys[slot];
                if (next[slot] != 0 && predicate.apply(existing, values[slot])) {
                    this.remove(existing);
                } else {
                    ++slot;
                }
            }

            return before - this.size();
        }
    }

    @Override
    public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(T procedure) {
        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        int max = this.keys.length;

        for (int slot = 0; slot < max; slot++) {
            if (next[slot] != 0) {
                procedure.apply(keys[slot], values[slot]);
            }
        }

        return procedure;
    }

    @Override
    public <T extends KTypeVTypePredicate<? super KType, ? super VType>> T forEach(T predicate) {
        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);

        int max = this.keys.length;

        for (int slot = 0; slot < max && (next[slot] == 0 || predicate.apply(keys[slot], values[slot])); ++slot) {
        }

        return predicate;
    }

    // Start of not finished
    @Override
    public KeysContainer keys() {
        return new KeysContainer();
    }

    @Override
    public KTypeCollection<VType> values() {
        return new ValuesContainer();
    }

    @Override
    public Iterator<KTypeVTypeCursor<KType, VType>> iterator() {
        return new EntryIterator();
    }

    // End of not finished

    @Override
    public boolean containsKey(KType key) {
        int hashIndex = hashKey(key);
        int nextOffset = next[hashIndex];
        if (nextOffset <= 0) {
            return false;
        }
        int entryIndex = searchInChain(key, hashIndex, nextOffset);

        return entryIndex >= 0;
    }

    public boolean containsValue(VType value) {
        final VType[] values = Intrinsics.<VType[]>cast(this.values);
        final byte[] next = this.next;
        final int size = this.size;

        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                if (Intrinsics.<VType> equals(values[index], value)) {
                    // An entry matches the value.
                    return true;
                }
                entryCount++;
            }
        }
        return false;
    }

    //Gotta check if it is enough
    @Override
    public void clear() {
        Arrays.fill(next, (byte) 0);
        size = 0;

        Arrays.fill(keys, Intrinsics.<KType> empty());

        /* #if ($TemplateOptions.VTypeGeneric) */
        Arrays.fill(values, noValue());
        /* #end */
    }

    @Override
    public void release() {
        if (!isEmpty()) {
            // If release() is called, it's probably to continue using this map and put again, so keep an initial capacity.
            int newCapacity = Math.max(getCapacity() / 4, DEFAULT_INITIAL_CAPACITY);
            keys = null;
            values = null;
            next = null;
            size = 0;
            allocateBuffers(newCapacity, false);
        }
    }

    public boolean trimToSize() {
        int fitCapacity = computeCapacityForSize(size);
        return fitCapacity < next.length && allocateBuffers(fitCapacity, false, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (!(o instanceof KTypeVTypeMap)) {
            return false;
        }

        KTypeVTypeMap map = (KTypeVTypeMap) o;
        final int size = this.size;
        if (size != map.size()) {
            return false;
        }
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        final VType[] values = Intrinsics.<VType[]>cast(this.values);
        final byte[] next = this.next;
        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                if (!Intrinsics.<VType> equals(values[index], map.get(keys[index]))) {
                    return false;
                }
                entryCount++;
            }
        }
        return true;
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /*! #if ($templateonly) !*/
    @Override
    public
    /*! #else protected #end !*/ boolean equals(Object v1, Object v2) {
        return (v1 == v2) || (v1 != null && v1.equals(v2));
    }
    /*! #end !*/

    @Override
    public int hashCode() {
        // Hash code must be computed with the same algorithm as all standard Java maps.
        // See Map.hashCode() javadoc.

        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        final VType[] values = Intrinsics.<VType[]>cast(this.values);
        final byte[] next = this.next;
        final int size = this.size;
        int hashCode = 0;
        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                hashCode +=  WormUtil.stdHash(keys[index]) ^ WormUtil.stdHash(values[index]);
                entryCount++;
            }
        }
        return hashCode;
    }

    /**
     * Returns a hash code for the given key.
     */
    /*! #if ($templateonly) !*/
    @Override
    public
    /*! #else protected #end !*/
    int hashKey(KType key) {
        return hash(key, next.length);
    }

    @Override
    public int indexOf(KType key) {
        int hashIndex = hash(key, next.length);
        int nextOffset = next[hashIndex];
        //Maybe I should return something else than noValue()
        if (nextOffset <= 0) {
            return ~hashIndex;
        }
        int entryIndex = searchInChain(key, hashIndex, nextOffset);

        return entryIndex < 0 ? ~entryIndex : entryIndex;
    }

    @Override
    public boolean indexExists(int index) {
        assert (index < 0 || (index >= 0 && index < keys.length));

        // Don't check upper bound just as HPPC do
        return index >= 0;
    }

    @Override
    public VType indexGet(int index) {
        assert (index >= 0 && index < keys.length);

        return Intrinsics.<VType>cast(values[index]);
    }

    @Override
    public VType indexReplace(int index, VType newValue) {
        assert index >= 0 : "The index must point at an existing key.";
        assert index < keys.length;

        VType previousValue = Intrinsics.<VType>cast(values[index]);
        values[index] = newValue;
        return previousValue;
    }

    @Override
    public void indexInsert(int index, KType key, VType value) {
        assert index < 0 : "The index must not point at an existing key.";

        index = ~index;
        assert ((next[index]) == 0);

        keys[index] = key;
        values[index] = value;
        next[index] = END_OF_CHAIN;

        size++;
    }

    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append('{');
        boolean firstEntry = true;
        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                if (firstEntry) {
                    firstEntry = false;
                } else {
                    sBuilder.append(", ");
                }
                KType key = Intrinsics.<KType>cast(keys[index]);
                sBuilder.append(key);
                sBuilder.append('=');
                VType value = Intrinsics.<VType>cast(values[index]);
                sBuilder.append(value);
                entryCount++;
            }
        }
        sBuilder.append('}');
        return sBuilder.toString();
    }

    public int getCapacity() {
        return next.length;
    }

    @Override
    public void ensureCapacity(int expectedElements) {
        allocateBuffers((int)(expectedElements * 1.0 / 0.76));
    }

    public boolean allocateBuffers(int capacity) {
        return allocateBuffers(capacity, false);
    }

    public boolean allocateBuffers(int capacity, boolean onlyIfEnlarged) {
        return allocateBuffers(capacity, onlyIfEnlarged, true);
    }

    public int getLoad() {
        return (int) (100.0 * size / keys.length);
    }

    @Override
    public String visualizeKeyDistribution(int characters) {
        throw new UnsupportedOperationException("Visualization is not supported in Worm Map");
//        return null;
    }

    @Override
    public long ramBytesAllocated() {
        return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + 4 * Integer.BYTES + Float.BYTES + 2 +
                RamUsageEstimator.shallowSizeOf(RECURSIVE_MOVE_ATTEMPTS) + RamUsageEstimator.shallowSizeOf(keys) +
                RamUsageEstimator.shallowSizeOf(values) + RamUsageEstimator.shallowSizeOf(next);
    }

    @Override
    public long ramBytesUsed() {
        return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + 4 * Integer.BYTES + Float.BYTES + 2 +
                RamUsageEstimator.shallowSizeOf(RECURSIVE_MOVE_ATTEMPTS) + RamUsageEstimator.shallowUsedSizeOfArray(keys, size) +
                RamUsageEstimator.shallowUsedSizeOfArray(values, size) + RamUsageEstimator.shallowUsedSizeOfArray(next, size);
    }


    @SuppressWarnings("unchecked")
    protected boolean allocateBuffers(int capacity, boolean onlyIfEnlarged, boolean autoEnlargeIfNeeded) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Illegal capacity=" + capacity);
        if (capacity < size && !onlyIfEnlarged) {
            throw new IllegalArgumentException("Illegal capacity=" + capacity + "; it cannot be less than size=" + size);
        }

        capacity = adjustCapacity(capacity);

        // Do nothing if the capacity does not change, or is not enlarged as requested.
        if (keys != null && (keys.length == capacity || onlyIfEnlarged && capacity < keys.length)) {
            return false;
        }

        if (PRINT_LOAD_FACTOR) {
            System.out.println("allocateBuffers() size=" + size + ", currentCapacity=" + (next == null ? "none" : next.length) + ", currentLoadFactor=" + (next == null ? "none" : ((float) size / next.length)) + ", requiredCapacity=" + capacity);
        }

        KType[] oldKeys = Intrinsics.<KType[]>cast(keys);
        VType[] oldValues = Intrinsics.<VType[]>cast(values);
        byte[] oldNext = next;
        keys = Intrinsics.<KType>newArray(capacity);
        values = Intrinsics.<VType>newArray(capacity);
        next = new byte[capacity];

        if (oldKeys != null) {
            int numAddedEntries = putOldEntries(oldKeys, oldValues, oldNext, size, autoEnlargeIfNeeded);
            if (numAddedEntries < 0) {
                // The addition failed because the map needs to be enlarged but it is not allowed.
                // Restore the old entries.
                keys = oldKeys;
                values = oldValues;
                next = oldNext;
                if (PRINT_LOAD_FACTOR) {
                    System.out.println("allocateBuffers() aborted, auto-enlarge needed but not allowed");
                }
                return false;
            }
            if (DEBUG_ENABLED) {
                assert checkIntegrity(numAddedEntries == size);
            }
        }
        if (PRINT_LOAD_FACTOR) {
            System.out.println("allocateBuffers() newCapacity=" + next.length + ", newLoadFactor=" + ((float) size / next.length));
        }

        return true;
    }

    /**
     * Adjusts the capacity just before setting it, by taking the next power-of-two. <p>This method can be overridden by
     * a sub-class to change the adjustment. In this case, also override the {@link #hash(KType, int)} method.</p>
     *
     * @param capacity The required capacity.
     * @return The adjusted capacity (next power-of-two of the input parameter capacity, or keep the provided capacity
     * if it is already a power of 2).
     */
    protected int adjustCapacity(int capacity) {
        return capacity >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : BitUtil.nextHighestPowerOfTwo(capacity);
    }

    /**
     * Gets the hash index corresponding to a key, according to the provided power-of-2 capacity. <p>This method can be
     * overridden by a sub-class to change the computation of the hash code, or the mapping to the hash table (e.g.
     * change the hashing algorithm for String).</p>
     *
     * @return An index within [0, capacity[ (0 included, capacity excluded).
     */
    protected int hash(KType key, int capacity) {
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        if (key == null)
            return 0;
        /*! #end !*/
        // Improves hash distribution. Reduces average get time by 30%.
        return WormUtil.hash(Intrinsics.<KType>cast(key)) & (capacity - 1);
    }

    /**
     * Computes sufficient capacity to hold <code>size</code> entries.
     */
    protected static int computeCapacityForSize(int size) {
        if (DEBUG_ENABLED) {
            assert size >= 0 : "size=" + size;
        }
        return (int) (size / FIT_LOAD_FACTOR) + 1;
    }

    /**
     * Returns the number of recursive move attempts at a specific recursive call level.
     */
    protected int getRecursiveMoveAttempts(int recursiveCallLevel) {
        return RECURSIVE_MOVE_ATTEMPTS[recursiveCallLevel];
    }

    /**
     * Maximum offset value for {@link #next}[i]. It is equal to min({@link #next}.length, {@link #END_OF_CHAIN}) - 1.
     */
    private int maxOffset() {
        return Math.min(next.length, END_OF_CHAIN) - 1;
    }

    /**
     * Puts an entry in this map.
     *
     * @param requiredPreviousValue Replace only if the previous value is equal to this specified value;
     *                              or {@code null} to replace without requirement.
     * @param sizeIncrease          Whether to increment {@link #size}.
     * @return The previous entry value (exact {@code requiredPreviousValue} reference if it matches);
     * or {@code null} if there was no previous entry.
     * @see #put(KType, VType)
     */
    private VType put(KType key, VType value, PutPolicy policy, VType requiredPreviousValue, boolean sizeIncrease) {
        // Compute the key hash index.
        int hashIndex = hash(key, next.length);
        int nextOffset = next[hashIndex];

        // We don't need to verify key != 0 as it would have stopped before with a NPE.

        boolean added = false;
        if (nextOffset > 0 && policy != PutPolicy.NEW_GUARANTEED) {
            // The bucket contains a head-of-chain entry.

            // Look for the key in the chain.
            int entryIndex = searchInChain(key, hashIndex, nextOffset);
            if (entryIndex >= 0) {
                // An entry in the chain matches the key. Replace the value and return the previous one.
                VType previousValue = Intrinsics.<VType>cast(values[entryIndex]);
                if (policy != PutPolicy.NEW_ONLY_IF_ABSENT
                        && (requiredPreviousValue == noValue() || Intrinsics.<VType> equals(requiredPreviousValue, previousValue))) {
                    values[entryIndex] = value;
                    if (requiredPreviousValue != noValue()) {
                        // Return the exact required previous value reference for fast check.
                        previousValue = requiredPreviousValue;
                    }
                }
                return previousValue;
            } else if (policy == PutPolicy.REPLACE_ONLY_IF_PRESENT) {
                return noValue();
            }

            if (enlargeIfNeeded()) {
                hashIndex = hash(key, next.length);
                nextOffset = next[hashIndex];
            } else {
                // No entry matches the key. Append the new entry at the tail of the chain.
                // ~entryIndex is the index of the last entry in the chain.
                if (!appendTailOfChain(~entryIndex, key, value)) {
                    // No free bucket in the range. Enlarge the map and put again.
                    enlargeAndPutNewEntry(key, value);
                }
                added = true;
            }
        } else if (policy == PutPolicy.REPLACE_ONLY_IF_PRESENT) {
            return noValue();
        } else if (enlargeIfNeeded()) {
            hashIndex = hash(key, next.length);
            nextOffset = next[hashIndex];
        }

        if (!added) {
            // No entry matches the key. Add the new entry.
            putNewEntry(hashIndex, nextOffset, key, value, true);
        }

        // Increment size.
        if (sizeIncrease) {
            size++;
        }
        if (DEBUG_ENABLED) {
            assert checkIntegrity(sizeIncrease);
        }
        return noValue();
    }

    /**
     * Removes an entry from this map.
     *
     * @param requiredValue The required value to remove only if the entry value matches it; or {@code null} for no constraint.
     * @return The removed value; or {@code null} if none.
     */
    private VType removeInner(KType key, VType requiredValue) {
        final byte[] next = this.next;

        // Compute the key hash index.
        int hashIndex = hash(key, next.length);
        int nextOffset = next[hashIndex];

        if (nextOffset <= 0)
        // The bucket is either free, or in tail-of-chain, so no entry for the key.
        {
            return noValue();
        }

        // The bucket contains a head-of-chain entry.
        // Look for the key in the chain.
        int previousEntryIndex = searchInChainReturnPrevious(key, hashIndex, nextOffset);
        if (previousEntryIndex < 0)
        // No entry matches the key.
        {
            return noValue();
        }
        int entryToRemoveIndex = previousEntryIndex == Integer.MAX_VALUE ?
                hashIndex : addOffset(previousEntryIndex, Math.abs(next[previousEntryIndex]), next);
        if (requiredValue != noValue() && !Intrinsics.<VType> equals(requiredValue, values[entryToRemoveIndex])) {
            return noValue();
        }
        return remove(hashIndex, previousEntryIndex, entryToRemoveIndex);
    }

    /**
     * Removes the entry at the specified removal index.
     *
     * @param headIndex          The head-of-chain index.
     * @param previousEntryIndex The index of the entry in the chain preceding the entry to remove; or
     *                           {@link Integer#MAX_VALUE} if the entry to remove is the head-of-chain.
     * @param entryToRemoveIndex The index of the entry to remove.
     * @return The value of the removed entry.
     */
    private VType remove(int headIndex, int previousEntryIndex, int entryToRemoveIndex) {
        if (DEBUG_ENABLED) {
            assert checkIndex(headIndex);
            assert next[headIndex] > 0;
            assert previousEntryIndex == Integer.MAX_VALUE || checkIndex(previousEntryIndex);
            assert checkIndex(entryToRemoveIndex);
        }

        final byte[] next = this.next;
        VType previousValue = Intrinsics.<VType>cast(values[entryToRemoveIndex]);

        // Find the last entry of the chain.
        int beforeLastIndex = findLastOfChainReturnPrevious(entryToRemoveIndex, next[entryToRemoveIndex]);
        int lastIndex;
        if (beforeLastIndex == Integer.MAX_VALUE) {
            beforeLastIndex = previousEntryIndex;
            lastIndex = entryToRemoveIndex;
        } else {
            lastIndex = addOffset(beforeLastIndex, Math.abs(next[beforeLastIndex]), next);
        }

        // Replace the removed entry by the last entry of the chain.
        if (entryToRemoveIndex != lastIndex) {
            // Removing an entry before the last of the chain. Replace it by the last one.
            keys[entryToRemoveIndex] = keys[lastIndex];
            values[entryToRemoveIndex] = values[lastIndex];
        }
        if (lastIndex != headIndex) {
            // Removing an entry in a chain of at least two entries. Unlink the last entry which replaces the removed entry.
            next[beforeLastIndex] = (byte) (beforeLastIndex == headIndex ? END_OF_CHAIN : -END_OF_CHAIN);
        }
        // Free the last entry of the chain.
        keys[lastIndex] = Intrinsics.<KType> empty();
        values[lastIndex] = noValue();
        next[lastIndex] = 0;

        // Decrement size
        size--;

        if (DEBUG_ENABLED) {
            assert checkIntegrity(true);
        }
        return previousValue;
    }

    /**
     * Enlarges this map if needed.
     *
     * @return Whether this map has been enlarged (and rehashed).
     */
    private boolean enlargeIfNeeded() {
        if (size >= next.length) {
            enlarge();
            return true;
        }
        return false;
    }

    /**
     * Enlarges and rehashes this map. Increases this map capacity without modifying the current size.
     */
    protected void enlarge() {
        allocateBuffers(next.length << 1, true);
    }

    /**
     * Enlarges this map and then puts a new entry.
     */
    private void enlargeAndPutNewEntry(KType key, VType value) {
        enlarge();
        put(key, value, PutPolicy.NEW_GUARANTEED, noValue(), false);
    }

    /**
     * Appends a new entry at the tail of an entry chain.
     *
     * @param lastEntryIndex The index of the last entry in the chain.
     * @return <code>true</code> if the new entry is added successfully; <code>false</code> if there is no free bucket
     * in the range (so this map needs to be enlarged to make room).
     */
    private boolean appendTailOfChain(int lastEntryIndex, KType key, VType value) {
        return appendTailOfChain(lastEntryIndex, key, value, ExcludedIndexes.NONE, 0);
    }

    /**
     * Appends a new entry at the tail of an entry chain.
     *
     * @param lastEntryIndex     The index of the last entry in the chain.
     * @param excludedIndexes    Indexes to exclude from the search.
     * @param recursiveCallLevel Keeps track of the recursive call level (starts at 0).
     * @return <code>true</code> if the new entry is added successfully; <code>false</code> if there is no free bucket
     * in the range (so this map needs to be enlarged to make room).
     */
    private boolean appendTailOfChain(int lastEntryIndex, KType key, VType value, ExcludedIndexes excludedIndexes, int recursiveCallLevel) {
        // Find the next free bucket by linear probing.
        int searchFromIndex = addOffset(lastEntryIndex, 1, next);
        int freeIndex = searchFreeBucket(searchFromIndex, maxOffset(), -1);
        if (freeIndex == -1) {
            freeIndex = searchAndMoveMovableBucket(searchFromIndex, maxOffset(), excludedIndexes, recursiveCallLevel);
            if (freeIndex == -1)
                return false;
        }
        keys[freeIndex] = key;
        values[freeIndex] = value;
        next[freeIndex] = -END_OF_CHAIN;
        int nextOffset = getOffsetBetweenIndexes(lastEntryIndex, freeIndex);
        next[lastEntryIndex] = (byte) (next[lastEntryIndex] > 0 ? nextOffset : -nextOffset); // Keep the offset sign.
        return true;
    }

    /**
     * Searches a free bucket by linear probing to the right.
     *
     * @param fromIndex     The index to start searching from, inclusive.
     * @param range         The maximum number of buckets to search, starting from index (included), up to index +
     *                      range (excluded).
     * @param excludedIndex Optional index to exclude from the search; -1 if none.
     * @return The index of the next free bucket; or -1 if not found within the range.
     */
    private int searchFreeBucket(int fromIndex, int range, int excludedIndex) {
        if (DEBUG_ENABLED) {
            assert range >= 0 && range <= maxOffset() : "range=" + range + ", maxOffset=" + maxOffset();
        }
        if (range == 0) {
            return -1;
        }
        final byte[] next = this.next;
        final int capacity = next.length;
        int toIndex = fromIndex + range;
        if (toIndex <= capacity) {
            // A single scan in the array is sufficient.
            for (int index = fromIndex; index < toIndex; index++) {
                if (next[index] == 0 && index != excludedIndex)
                    return index;
            }
        } else {
            // The array is circular, scan up to the end, and then continue scanning from the beginning.
            for (int index = fromIndex; index < capacity; index++) {
                if (next[index] == 0 && index != excludedIndex)
                    return index;
            }
            toIndex -= capacity;
            for (int index = 0; index < toIndex; index++) {
                if (next[index] == 0 && index != excludedIndex)
                    return index;
            }
        }
        return -1;
    }

    /**
     * Searches a movable tail-of-chain bucket by linear probing to the right. If a movable tail-of-chain is found, this
     * method attempts to move it.
     *
     * @param fromIndex          The index of the entry to start searching from.
     * @param range              The maximum number of buckets to search, starting from index (included), up to index +
     *                           range (excluded).
     * @param excludedIndexes    Indexes to exclude from the search.
     * @param recursiveCallLevel Keeps track of the recursive call level (starts at 0).
     * @return The index of the freed bucket; or -1 if no bucket could be freed within the range.
     */
    private int searchAndMoveMovableBucket(int fromIndex, int range, ExcludedIndexes excludedIndexes, int recursiveCallLevel) {

        if (DEBUG_ENABLED) {
            assert range >= 0 && range <= maxOffset() : "range=" + range + ", maxOffset=" + maxOffset();
        }
        int remainingAttempts = getRecursiveMoveAttempts(recursiveCallLevel);
        if (remainingAttempts <= 0 || range <= 0)
            return -1;
        final byte[] next = this.next;
        final int capacity = next.length;
        int lastIndex = fromIndex + range - 1;
        int nextRecursiveCallLevel = recursiveCallLevel + 1;
        if (lastIndex < capacity) {
            // A single scan in the array is sufficient.
            for (int index = lastIndex; index >= fromIndex; index--) {
                if (excludedIndexes.isIndexExcluded(index))
                    continue;
                int nextOffset = next[index];
                if (nextOffset < 0) {
                    // Attempt to move the tail of chain.
                    if (moveTailOfChain(index, nextOffset, excludedIndexes, nextRecursiveCallLevel))
                        return index;
                    if (--remainingAttempts <= 0)
                        return -1;
                }
            }
        } else {
            // The array is circular, scan in two steps.
            for (int lastIndexRolled = lastIndex - capacity, index = lastIndexRolled; index >= 0; index--) {
                if (excludedIndexes.isIndexExcluded(index))
                    continue;
                int nextOffset = next[index];
                if (nextOffset < 0) {
                    // Attempt to move the tail of chain.
                    if (moveTailOfChain(index, nextOffset, excludedIndexes, nextRecursiveCallLevel))
                        return index;
                    if (--remainingAttempts <= 0)
                        return -1;
                }
            }
            for (int index = capacity - 1; index >= fromIndex; index--) {
                if (excludedIndexes.isIndexExcluded(index))
                    continue;
                int nextOffset = next[index];
                if (nextOffset < 0) {
                    // Attempt to move the tail of chain.
                    if (moveTailOfChain(index, nextOffset, excludedIndexes, nextRecursiveCallLevel))
                        return index;
                    if (--remainingAttempts <= 0)
                        return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Finds the previous entry in the chain by linear probing to the left.
     * <p>Note: Alternatively we could compute the hash index of the key, jump directly to the head of the chain, and then
     * follow the chain until we find the entry which next entry in the chain is the provided one. But this alternative
     * is slightly slower on average, even if the key hash code is cached.</p>
     *
     * @param entryIndex The index of the entry to start searching from. This method starts scanning at this index - 1,
     *                   modulo array length since the array is circular.
     * @return The index of the previous entry in the chain.
     * @throws NoSuchElementException If the previous entry is not found (never happens if entryIndex is the index of a
     *                                tail-of-chain entry, with next[entryIndex] &lt; 0).
     */
    private int findPreviousInChain(int entryIndex) {
        if (DEBUG_ENABLED) {
            assert next[entryIndex] < 0;
        }
        final byte[] next = this.next;
        final int capacity = next.length;
        int fromIndex = subtractOffset(entryIndex, 1, next);
        int toIndex = fromIndex - maxOffset();
        if (toIndex >= -1) {
            // A single scan in the array is sufficient.
            for (int index = fromIndex; index > toIndex; index--) {
                if (chainsTo(index, entryIndex, next)) {
                    return index;
                }
            }
        } else {
            // The array is circular, scan down to the beginning, and then continue scanning from the end.
            for (int index = fromIndex; index > -1; index--) {
                if (chainsTo(index, entryIndex, next)) {
                    return index;
                }
            }
            toIndex += capacity;
            for (int index = capacity - 1; index > toIndex; index--) {
                if (chainsTo(index, entryIndex, next)) {
                    return index;
                }
            }
        }
        throw new NoSuchElementException("Previous entry not found (entryIndex=" + entryIndex + ", next[entryIndex]=" + next[entryIndex] + ")");
    }

    /**
     * Indicates whether the entry at the specified index chains to <code>nextIndex</code>.
     */
    private static boolean chainsTo(int index, int nextIndex, byte[] next) {
        int nextOffset = next[index];
        if (nextOffset == 0)
            return false;
        nextOffset = Math.abs(nextOffset);
        return nextOffset != END_OF_CHAIN && addOffset(index, nextOffset, next) == nextIndex;
    }

    /**
     * Puts a new entry that is guaranteed not to be already contained by this map. <p>This method does not modify this
     * map {@link #size}. It may enlarge this map if it needs room to put the entry.</p>
     *
     * @param hashIndex          The hash index where to put the entry (= {@link #hash}(key, next.length)).
     * @param nextOffset         The current value of {@link #next}[hashIndex].
     * @param enlargeMapIfNeeded Whether the map can be enlarged automatically if needed.
     * @return The result of the new entry addition.
     */
    private NewEntryAdditionResult putNewEntry(int hashIndex, int nextOffset, KType key, VType value, boolean enlargeMapIfNeeded) {
        if (DEBUG_ENABLED) {
            assert hashIndex == hash(key, next.length) : "hashIndex=" + hashIndex + ", hash(key, next.length)=" + hash(key, next.length);
            assert checkIndex(hashIndex);
            assert Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[hashIndex] : "nextOffset=" + nextOffset + ", next[hashIndex]=" + next[hashIndex];
        }

        final byte[] next = this.next;

        if (nextOffset > 0) {
            // The bucket contains a head-of-chain entry.
            // Append the new entry at the chain tail, after the last entry of the chain. If there is no free bucket in
            // the range, enlarge this map and put the new entry.
            if (appendTailOfChain(findLastOfChain(hashIndex, nextOffset), key, value))
                return NewEntryAdditionResult.SUCCESS;
            if (enlargeMapIfNeeded) {
                enlargeAndPutNewEntry(key, value);
                return NewEntryAdditionResult.SUCCESS_MAP_ENLARGED;
            }
            return NewEntryAdditionResult.FAILURE;
        } else {
            if (nextOffset < 0) {
                // Bucket at hash index contains a movable tail-of-chain entry. Move it to free the bucket.
                if (!moveTailOfChain(hashIndex, nextOffset)) {
                    // No free bucket in the range. Enlarge the map and put again.
                    if (enlargeMapIfNeeded) {
                        enlargeAndPutNewEntry(key, value);
                        return NewEntryAdditionResult.SUCCESS_MAP_ENLARGED;
                    }
                    return NewEntryAdditionResult.FAILURE;
                }
            }

            // Bucket at hash index is free. Add the new head-of-chain entry.
            keys[hashIndex] = key;
            values[hashIndex] = value;
            next[hashIndex] = END_OF_CHAIN;
            return NewEntryAdditionResult.SUCCESS;
        }
    }

    /**
     * Puts old entries after enlarging this map. Old entries are guaranteed not to be already contained by this map.
     * <p>This method does not modify this map {@link #size}. It may enlarge this map if it needs room to put the entry.</p>
     *
     * @param oldKeys   The old keys.
     * @param oldValues The old values.
     * @param oldNext   The old next offsets.
     * @param entryNum  The number of non null old entries. It is supported to set a value larger than the real count.
     * @return The actual number of old entries put; or -1 if the addition failed because the map needs to be enlarged
     * but it is not allowed.
     */
    private int putOldEntries(KType[] oldKeys, VType[] oldValues, byte[] oldNext, int entryNum, boolean enlargeMapIfNeeded) {
        byte[] next = this.next;
        int nextLength = next.length;
        int entryCount = 0;
        // Iterate new entries.
        // The condition on index < endIndex is required because the putNewEntry() method below may need to
        // enlarge the map, which calls this method again. And in this case entryNum is larger than the real number.
        for (int index = 0, endIndex = oldKeys.length; entryCount < entryNum && index < endIndex; index++) {
            if (oldNext[index] != 0) {
                // Compute the key hash index.
                KType oldKey = oldKeys[index];
                int hashIndex = hash(oldKey, nextLength);

                // Add the new entry.
                if (DEBUG_ENABLED)
                    assert next == this.next;
                switch (putNewEntry(hashIndex, next[hashIndex], oldKey, oldValues[index], enlargeMapIfNeeded)) {
                    case SUCCESS_MAP_ENLARGED:
                        next = this.next;
                        nextLength = next.length;
                        break;
                    case FAILURE:
                        return -1;
                }

                entryCount++;
            }
        }
        return entryCount;
    }

    /**
     * Moves a tail-of-chain entry to another free bucket.
     *
     * @param tailIndex  The index of the tail-of-chain entry.
     * @param nextOffset The value of {@link #next}[tailIndex]. It is always &lt; 0.
     * @return Whether the entry has been successfully moved; or if it could not because there is no free bucket in the
     * range.
     */
    private boolean moveTailOfChain(int tailIndex, int nextOffset) {
        return moveTailOfChain(tailIndex, nextOffset, ExcludedIndexes.NONE, 0);
    }

    /**
     * Moves a tail-of-chain entry to another free bucket.
     *
     * @param tailIndex          The index of the tail-of-chain entry.
     * @param nextOffset         The value of {@link #next}[tailIndex]. It is always &lt; 0.
     * @param excludedIndexes    Indexes to exclude from the search.
     * @param recursiveCallLevel Keeps track of the recursive call level (starts at 0).
     * @return Whether the entry has been successfully moved; or if it could not because there is no free bucket in the
     * range.
     */
    private boolean moveTailOfChain(int tailIndex, int nextOffset, ExcludedIndexes excludedIndexes, int recursiveCallLevel) {
        if (DEBUG_ENABLED) {
            assert checkIndex(tailIndex);
            assert nextOffset < 0 && nextOffset >= -END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[tailIndex] : "nextOffset=" + nextOffset + ", next[tailIndex]=" + next[tailIndex];
        }

        // Find the next free bucket by linear probing.
        // It must be within a range of maxOffset of the previous entry in the chain,
        // and not beyond the next entry in the chain.
        final byte[] next = this.next;
        final int maxOffset = maxOffset();
        int previousIndex = findPreviousInChain(tailIndex);
        int absPreviousOffset = Math.abs(next[previousIndex]);
        int nextIndex = nextOffset == -END_OF_CHAIN ? -1 : addOffset(tailIndex, -nextOffset, next);
        int offsetFromPreviousToNext = absPreviousOffset - nextOffset;
        int searchFromIndex;
        int searchRange;
        boolean nextIndexWithinRange;
        // Compare [the offset from previous entry to next entry] to [maxOffset].
        if (offsetFromPreviousToNext <= maxOffset) {
            // The next entry in the chain is inside the maximum offset range.
            // Prepare to search for a free bucket starting from the tail-of-chain entry, up to the next entry in the chain.
            searchFromIndex = addOffset(previousIndex, 1, next);
            searchRange = offsetFromPreviousToNext - 1;
            nextIndexWithinRange = true;
        } else {
            // The next entry is not inside the maximum range. It is always the case if nextOffset is -END_OF_CHAIN.
            // Prepare to search for a free bucket starting from the tail-of-chain entry, up to maxOffset from the
            // previous entry.
            if (nextIndex == -1) {
                searchFromIndex = addOffset(previousIndex, 1, next);
                searchRange = maxOffset;
            } else {
                searchFromIndex = subtractOffset(nextIndex, maxOffset, next);
                int searchToIndex = addOffset(previousIndex, maxOffset, next);
                searchRange = getOffsetBetweenIndexes(searchFromIndex, searchToIndex) + 1;
            }
            nextIndexWithinRange = false;
        }
        int freeIndex = searchFreeBucket(searchFromIndex, searchRange, tailIndex);
        if (freeIndex == -1) {
            // No free bucket in the range.
            if (nextIndexWithinRange && appendTailOfChain(
                    findLastOfChain(nextIndex, next[nextIndex]), Intrinsics.<KType>cast(keys[tailIndex]), Intrinsics.<VType>cast(values[tailIndex]), excludedIndexes, recursiveCallLevel)) {
                // The entry to move has been appended to the tail of the chain.
                // Complete the move by linking the previous entry to the next entry (which is within range).
                int previousOffset = getOffsetBetweenIndexes(previousIndex, nextIndex);
                next[previousIndex] = (byte) (next[previousIndex] > 0 ? previousOffset : -previousOffset); // Keep the offset sign.
                return true;
            } else {
                ExcludedIndexes recursiveExcludedIndexes = excludedIndexes.union(ExcludedIndexes.fromChain(previousIndex, next));
                if ((freeIndex = searchAndMoveMovableBucket(searchFromIndex, searchRange, recursiveExcludedIndexes, recursiveCallLevel)) == -1) {
                    // No free bucket after the tail of the chain, and no movable entry. No bucket available around.
                    // The move fails (and this map will be enlarged by the calling method).
                    return false;
                }
            }
        }
        // Move the entry to the free index.
        // No need to set keys[tailIndex] and values[tailIndex] to null here because they will be set when this method returns,
        // or the map will be enlarged and rehashed.
        keys[freeIndex] = keys[tailIndex];
        values[freeIndex] = values[tailIndex];
        next[freeIndex] = (byte) (nextOffset == -END_OF_CHAIN ? nextOffset : -getOffsetBetweenIndexes(freeIndex, nextIndex));
        int previousOffset = getOffsetBetweenIndexes(previousIndex, freeIndex);
        next[previousIndex] = (byte) (next[previousIndex] > 0 ? previousOffset : -previousOffset); // Keep the offset sign.

        if (DEBUG_ENABLED) {
            assert next[freeIndex] < 0 : "freeIndex=" + freeIndex + ", next[freeIndex]=" + next[freeIndex];
        }
        return true;
    }

    /**
     * Searches an entry in a chain.
     *
     * @param key        The searched entry key.
     * @param index      The head-of-chain index.
     * @param nextOffset next[index]. It must be &gt; 0.
     * @return The matched entry index; or 2's complement ~index if not found, index of the last entry in the chain.
     */
    private int searchInChain(KType key, int index, int nextOffset) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index);
            assert nextOffset > 0 && nextOffset <= END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];
        }

        // There is at least one entry at this bucket. Check the first head-of-chain.
        if (Intrinsics.<KType> equals(keys[index], key)) {
            // The first head-of-chain entry matches the key. Return its index.
            return index;
        }

        // Follow the entry chain for this bucket.
        while (nextOffset != END_OF_CHAIN) {
            index = addOffset(index, nextOffset, next); // Jump forward.
            if (Intrinsics.<KType> equals(keys[index], key)) {
                // An entry in the chain matches the key. Return its index.
                return index;
            }
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            if (DEBUG_ENABLED) {
                assert nextOffset > 0 : "nextOffset=" + nextOffset;
            }
        }

        // No entry matches the key. Return the last entry index as 2's complement.
        return ~index;
    }

    /**
     * Searches an entry in a chain and returns its previous entry in the chain.
     *
     * @param key        The searched entry key.
     * @param index      The head-of-chain index.
     * @param nextOffset next[index]. It must be &gt; 0.
     * @return The index of the entry preceding the matched entry; or {@link Integer#MAX_VALUE} if the head-of-chain
     * matches; or 2's complement ~index if not found, index of the last entry in the chain.
     */
    private int searchInChainReturnPrevious(KType key, int index, int nextOffset) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index);
            assert nextOffset > 0 && nextOffset <= END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];
        }

        // There is at least one entry at this bucket. Check the first head-of-chain.
        if (Intrinsics.<KType> equals(keys[index], key)) {
            // The first head-of-chain entry matches the key. Return Integer.MAX_VALUE as there is no previous entry.
            return Integer.MAX_VALUE;
        }

        // Follow the entry chain for this bucket.
        while (nextOffset != END_OF_CHAIN) {
            int previousIndex = index;
            index = addOffset(index, nextOffset, next); // Jump forward.
            if (Intrinsics.<KType> equals(keys[index], key)) {
                // An entry in the chain matches the key. Return the previous entry index.
                return previousIndex;
            }
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            if (DEBUG_ENABLED) {
                assert nextOffset > 0 : "nextOffset=" + nextOffset;
            }
        }

        // No entry matches the key. Return the last entry index as 2's complement.
        return ~index;
    }

    /**
     * Finds the last entry of a chain.
     *
     * @param index      The index of an entry in the chain.
     * @param nextOffset next[index].
     * @return The index of the last entry in the chain.
     */
    private int findLastOfChain(int index, int nextOffset) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index);
            assert nextOffset != 0 && Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];
        }

        // Follow the entry chain for this bucket.
        if (nextOffset < 0) {
            nextOffset = -nextOffset;
        }
        while (nextOffset != END_OF_CHAIN) {
            index = addOffset(index, nextOffset, next); // Jump forward.
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            if (DEBUG_ENABLED) {
                assert nextOffset > 0 : "nextOffset=" + nextOffset;
            }
        }
        return index;
    }

    /**
     * Finds the last entry of a chain and returns its previous entry in the chain.
     *
     * @param index      The index of an entry in the chain.
     * @param nextOffset next[index].
     * @return The index of the entry preceding the last entry in the chain; or {@link Integer#MAX_VALUE} if the entry
     * at the provided index is the last one of the chain.
     */
    private int findLastOfChainReturnPrevious(int index, int nextOffset) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index);
            assert nextOffset != 0 && Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
            assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];
        }

        // Follow the entry chain for this bucket.
        if (nextOffset < 0) {
            nextOffset = -nextOffset;
        }
        int previousIndex = Integer.MAX_VALUE;
        while (nextOffset != END_OF_CHAIN) {
            previousIndex = index;
            index = addOffset(index, nextOffset, next); // Jump forward.
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            if (DEBUG_ENABLED) {
                assert nextOffset > 0 : "nextOffset=" + nextOffset;
            }
        }
        return previousIndex;
    }

    /**
     * Adds a positive offset to the provided index, handling rotation around the circular array.
     *
     * @return The new index after addition.
     */
    private static int addOffset(int index, int offset, byte[] next) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
            assert offset > 0 && offset < END_OF_CHAIN : "offset=" + offset;
        }
        index += offset;
        while (index >= next.length) {
            index -= next.length;
        }
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
        }
        return index;
    }

    /**
     * Subtracts a positive offset to the provided index, handling rotation around the circular array.
     *
     * @return The new index after subtraction.
     */
    private static int subtractOffset(int index, int offset, byte[] next) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
            assert offset > 0 && offset < END_OF_CHAIN : "offset=" + offset;
        }
        index -= offset;
        while (index < 0) {
            index += next.length;
        }
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
        }
        return index;
    }

    /**
     * Gets the offset between two indexes, handling rotation around the circular array.
     *
     * @return The positive offset between the two indexes.
     */
    private int getOffsetBetweenIndexes(int fromIndex, int toIndex) {
        if (DEBUG_ENABLED) {
            assert checkIndex(fromIndex);
            assert checkIndex(toIndex);
        }
        int offset = toIndex - fromIndex;
        if (offset < 0) {
            offset += next.length;
        }
        if (DEBUG_ENABLED) {
            assert offset >= 0 : "offset=" + offset + ", fromIndex=" + fromIndex + ", toIndex=" + toIndex;
        }
        return offset;
    }

    /**
     * Check the map integrity.
     */
    private boolean checkIntegrity(boolean checkSize) {
        //noinspection ConstantConditions
        assert DEBUG_ENABLED;
        int entryCount = 0;

        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        final VType[] values = Intrinsics.<VType[]>cast(this.values);

        for (int index = 0; index < next.length; index++) {
            // Check the distinction between empty and occupied bucket.
            assert next[index] == 0 || (!Intrinsics.<KType>isEmpty(keys[index]) && values[index] != noValue());
            // Check next[index] points to an occupied bucked.
            assert next[index] == 0 || Math.abs(next[index]) == END_OF_CHAIN || next[addOffset(index, Math.abs(next[index]), next)] != 0;
            // Check entry chains.
            if (next[index] > 0) {
                int i = index;
                while (true) {
                    // Check the hash of each entry in the chain is index.
                    assert hash(keys[i], next.length) == index;
                    if (Math.abs(next[i]) == END_OF_CHAIN) {
                        break;
                    }
                    i = addOffset(i, Math.abs(next[i]), next); // Jump to the next entry in the chain.
                    // Check tail-of-chain entries have negative next offset.
                    assert next[i] < 0;
                }
            }
            // Count occupied buckets.
            if (next[index] != 0) {
                entryCount++;
            }
        }
        // Check map size.
        assert !checkSize || entryCount == size;
        return true;
    }

    private boolean checkIndex(int index) {
        return checkIndex(index, next);
    }

    private static boolean checkIndex(int index, byte[] next) {
        assert index >= 0 && index < next.length : "index=" + index + ", array length=" + next.length;
        return true;
    }

    /**
     * Efficient immutable set of excluded indexes (immutable int set of expected small size).
     * <p/>Used when searching for a free bucket and attempting to move tail-of-chain entries
     * recursively. We must not move the entry chains for which we want to find a free bucket.
     * So {@link ExcludedIndexes} is immutable and can be stacked with {@link #union(ExcludedIndexes)}
     * during recursive calls. In addition the initial {@link #NONE} is a constant and does not stack
     * as it overrides {@link #union(ExcludedIndexes)}.
     */
    private static abstract class ExcludedIndexes {

        static final ExcludedIndexes NONE = new ExcludedIndexes() {
            @Override
            ExcludedIndexes union(ExcludedIndexes excludedIndexes) {
                return excludedIndexes;
            }

            @Override
            boolean isIndexExcluded(int index) {
                return false;
            }
        };

        static ExcludedIndexes fromChain(int index, byte[] next) {
            int nextOffset = Math.abs(next[index]);
            if (DEBUG_ENABLED) {
                assert nextOffset != 0 : "nextOffset=0";
            }
            return nextOffset == END_OF_CHAIN ? new SingletonExcludedIndex(index)
                    : new MultipleExcludedIndexes(index, nextOffset, next);
        }

        ExcludedIndexes union(ExcludedIndexes excludedIndexes) {
            return new UnionExcludedIndexes(this, excludedIndexes);
        }

        abstract boolean isIndexExcluded(int index);
    }

    private static class SingletonExcludedIndex extends ExcludedIndexes {

        final int excludedIndex;

        SingletonExcludedIndex(int excludedIndex) {
            this.excludedIndex = excludedIndex;
        }

        @Override
        boolean isIndexExcluded(int index) {
            return index == excludedIndex;
        }
    }

    private static class MultipleExcludedIndexes extends ExcludedIndexes {

        final int[] excludedIndexes;
        final int size;

        MultipleExcludedIndexes(int index, int nextOffset, byte[] next) {
            if (DEBUG_ENABLED) {
                assert index >= 0 && index < next.length : "index=" + index + ", next.length=" + next.length;
                assert nextOffset > 0 && nextOffset < END_OF_CHAIN : "nextOffset=" + nextOffset;
            }
            int[] excludedIndexes = new int[8];
            int size = 0;
            boolean shouldSort = false;
            excludedIndexes[size++] = index;
            do {
                int nextIndex = addOffset(index, nextOffset, next);
                if (nextIndex < index) {
                    // Rolling on the circular buffer. We will need to sort to keep a sorted list of indexes.
                    shouldSort = true;
                }
                if (DEBUG_ENABLED) {
                    assert nextIndex >= 0 && nextIndex < next.length : "nextIndex=" + index + ", next.length=" + next.length;
                }
                if (size == excludedIndexes.length) {
                    excludedIndexes = Arrays.copyOf(excludedIndexes, size * 2);
                }
                excludedIndexes[size++] = index = nextIndex;
                nextOffset = Math.abs(next[index]);
                if (DEBUG_ENABLED) {
                    assert nextOffset > 0 : "nextOffset=" + nextOffset;
                }
            } while (nextOffset != END_OF_CHAIN);
            if (shouldSort) {
                Arrays.sort(excludedIndexes, 0, size);
            }
            this.excludedIndexes = excludedIndexes;
            this.size = size;
        }

        @Override
        boolean isIndexExcluded(int index) {
            return Arrays.binarySearch(excludedIndexes, 0, size, index) >= 0;
        }
    }

    private static class UnionExcludedIndexes extends ExcludedIndexes {

        final ExcludedIndexes left;
        final ExcludedIndexes right;

        UnionExcludedIndexes(ExcludedIndexes left, ExcludedIndexes right) {
            this.left = left;
            this.right = right;
        }

        @Override
        boolean isIndexExcluded(int index) {
            return left.isIndexExcluded(index) || right.isIndexExcluded(index);
        }
    }

    /**
     * A view of the keys inside this hash map.
     */
    public final class KeysContainer extends AbstractKTypeCollection<KType>
                                    implements KTypeLookupContainer<KType> {
        private final KTypeVTypeWormMap<KType, VType> owner = KTypeVTypeWormMap.this;

        @Override
        public boolean contains(KType e) {
            return owner.containsKey(e);
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
            owner.forEach(new KTypeVTypeProcedure<KType, VType>() {
                @Override
                public void apply(KType key, VType value) {
                    procedure.apply(key);
                }
            });

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
            owner.forEach(new KTypeVTypePredicate<KType, VType>() {
                @Override
                public boolean apply(KType key, VType value) {
                    return predicate.apply(key);
                }
            });

            return predicate;
        }

        @Override
        public boolean isEmpty() {
            return owner.isEmpty();
        }

        @Override
        public Iterator<KTypeCursor<KType>> iterator() {
            return new KeysIterator();
        }

        @Override
        public int size() {
            return owner.size();
        }

        @Override
        public void clear() {
            owner.clear();
        }

        @Override
        public void release() {
            owner.release();
        }

        @Override
        public int removeAll(KTypePredicate<? super KType> predicate) {
            return owner.removeAll(predicate);
        }

        @Override
        public int removeAll(final KType e) {
            final boolean hasKey = owner.containsKey(e);
            if (hasKey) {
                owner.remove(e);
                return 1;
            } else {
                return 0;
            }
        }
    };

    /**
     * An iterator over the set of assigned keys.
     */
    private final class KeysIterator extends AbstractIterator<KTypeCursor<KType>> {
        private final KTypeCursor<KType> cursor;
        private final int max = keys.length;
        private int slot = -1;

        public KeysIterator() {
            cursor = new KTypeCursor<KType>();
        }

        @Override
        protected KTypeCursor<KType> fetch() {
            if (slot < max) {
                for (slot++; slot < max; slot++) {
                    if (next[slot] != 0) {
                        cursor.index = slot;
                        cursor.value = Intrinsics.<KType>cast(keys[slot]);
                        return cursor;
                    }
                }
            }

            return done();
        }
    }

    /**
     * A view over the set of values of this map.
     */
    private final class ValuesContainer extends AbstractKTypeCollection<VType> {
        private final KTypeVTypeWormMap<KType, VType> owner = KTypeVTypeWormMap.this;

        @Override
        public int size() {
            return owner.size();
        }

        @Override
        public boolean isEmpty() {
            return owner.isEmpty();
        }

        @Override
        public boolean contains(VType value) {
            for (KTypeVTypeCursor<KType, VType> c : owner) {
                if (Intrinsics.<VType> equals(value, c.value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super VType>> T forEach(T procedure) {
            for (KTypeVTypeCursor<KType, VType> c : owner) {
                procedure.apply(c.value);
            }
            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super VType>> T forEach(T predicate) {
            for (KTypeVTypeCursor<KType, VType> c : owner) {
                if (!predicate.apply(c.value)) {
                    break;
                }
            }
            return predicate;
        }

        @Override
        public Iterator<KTypeCursor<VType>> iterator() {
            return new ValuesIterator();
        }

        @Override
        public int removeAll(final VType e) {
            return owner.removeAll(new KTypeVTypePredicate<KType, VType>() {
                @Override
                public boolean apply(KType key, VType value) {
                    return Intrinsics.<VType> equals(e, value);
                }
            });
        }

        @Override
        public int removeAll(final KTypePredicate<? super VType> predicate) {
            return owner.removeAll(new KTypeVTypePredicate<KType, VType>()  {
                @Override
                public boolean apply(KType key, VType value) {
                    return predicate.apply(value);
                }
            });
        }

        @Override
        public void clear() {
            owner.clear();
        }

        @Override
        public void release() {
            owner.release();
        }
    }

    /**
     * An iterator over the set of assigned values.
     */
    private final class ValuesIterator extends AbstractIterator<KTypeCursor<VType>> {
        private final KTypeCursor<VType> cursor;
        private int max = keys.length;
        private int slot = -1;

        public ValuesIterator() {
            cursor = new KTypeCursor<VType>();
        }

        @Override
        protected KTypeCursor<VType> fetch() {
            if (slot < max) {
                for (slot++; slot < max; slot++) {
                    if (next[slot] != 0) {
                        cursor.index = slot;
                        cursor.value = Intrinsics.<VType> cast(values[slot]);
                        return cursor;
                    }
                }
            }

            return done();
        }
    }

    /**
     * An iterator implementation for {@link #iterator}.
     */
    private final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>> {
        private final KTypeVTypeCursor<KType, VType> cursor;
        private final int max = keys.length;
        private int slot = -1;

        public EntryIterator() {
            cursor = new KTypeVTypeCursor<KType, VType>();
        }

        @Override
        protected KTypeVTypeCursor<KType, VType> fetch() {
            if (slot < max) {
                int existing;
                for (slot++; slot < max; slot++) {
                    if (next[slot] != 0) {
                        cursor.index = slot;
                        cursor.key = Intrinsics.<KType> cast(keys[slot]);
                        cursor.value = Intrinsics.<VType> cast(values[slot]);
                        return cursor;
                    }
                }
            }

            return done();
        }
    }
}
