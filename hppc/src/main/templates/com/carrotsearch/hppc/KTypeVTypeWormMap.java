/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.Containers.DEFAULT_EXPECTED_ELEMENTS;
import static com.carrotsearch.hppc.HashContainers.MAX_HASH_ARRAY_LENGTH;
import static com.carrotsearch.hppc.HashContainers.MIN_HASH_ARRAY_LENGTH;
import static com.carrotsearch.hppc.WormUtil.*;

/**
 * A hash map of <code>KType</code> to <code>VType</code>, implemented using Worm Hashing strategy.
 *
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
     * {@code abs(next[i])=offset} to next chained entry index. <p>{@code next[i]=0} for free bucket.</p> <p>The
     * offset is always forward, and the array is considered circular, meaning that an entry at the end of the
     * array may point to an entry at the beginning with a positive offset.</p> <p>The offset is always forward, but the
     * sign of the offset encodes head/tail of chain. {@link #next}[i] &gt; 0 for the first head-of-chain entry (within
     * [1,{@link WormUtil#maxOffset}]), {@link #next}[i] &lt; 0 for the subsequent tail-of-chain entries (within [-{@link
     * WormUtil#maxOffset},-1]. For the last entry in the chain, {@code abs(next[i])=}{@link WormUtil#END_OF_CHAIN}.</p>
     */
    public byte[] next;

    /**
     * Map size (number of entries).
     */
    protected int size;

    protected int keyMixer;

    /**
     * Constructs a {@link KTypeVTypeWormMap} with the default initial capacity.
     */
    public KTypeVTypeWormMap() {
        this(DEFAULT_EXPECTED_ELEMENTS);
    }

    /**
     * Constructs a {@link KTypeVTypeWormMap}.
     *
     * @param expectedElements The expected number of elements. Based on it the capacity of the map is calculated.
     */
    public KTypeVTypeWormMap(int expectedElements) {
        if (expectedElements < 0) {
            throw new IllegalArgumentException("Invalid expectedElements=" + expectedElements);
        }
        ensureCapacity(expectedElements);
    }

    /**
     * Create a hash map from all key-value pairs of another container.
     */
    public KTypeVTypeWormMap(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
        this(container.size());
        putAll(container);
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

    /**
     * Clones this map. The cloning operation is efficient because it copies directly the internal arrays, without
     * having to put entries in the cloned map. The cloned map has the same entries and the same capacity as this map.
     *
     * @return A shallow copy of this map.
     */
    @Override
    public KTypeVTypeWormMap<KType, VType> clone() {
        try {
            /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
            KTypeVTypeWormMap<KType, VType> cloneMap = (KTypeVTypeWormMap<KType, VType>) super.clone();
            cloneMap.keys = Arrays.copyOf(keys, keys.length);
            cloneMap.values = Arrays.copyOf(values, values.length);
            cloneMap.next = Arrays.copyOf(next, next.length);
            return cloneMap;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public VType get(KType key) {
        // Compute the key hash index.
        int hashIndex = hashReduce(key);
        int nextOffset = next[hashIndex];

        if (nextOffset <= 0)
        {
            // The bucket is either free, or only used for chaining, so no entry for the key.
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
        return put(key, value, PutPolicy.NEW_OR_REPLACE, true);
    }

    @Override
    public int putAll(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
        final int initialSize = size();
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : container) {
            put(c.key, c.value);
        }
        return size() - initialSize;
    }

    @Override
    public int putAll(Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable) {
        final int initialSize = size();
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable) {
            put(c.key, c.value);
        }
        return size() - initialSize;
    }

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
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

    /**
     * @param key The key of the value to check.
     * @param value The value to put if <code>key</code> does not exist.
     * @return <code>true</code> if <code>key</code> did not exist and <code>value</code> was placed in the map.
     */
    public boolean putIfAbsent(KType key, VType value) {
        return noValue() == put(key, value, PutPolicy.NEW_ONLY_IF_ABSENT, true);
    }

    @Override
    public VType remove(KType key) {
        final byte[] next = this.next;

        // Compute the key hash index.
        int hashIndex = hashReduce(key);
        int nextOffset = next[hashIndex];
        if (nextOffset <= 0) {
            // The bucket is either free, or in tail-of-chain, so no entry for the key.
            return noValue();
        }
        // The bucket contains a head-of-chain entry.
        // Look for the key in the chain.
        int previousEntryIndex = searchInChainReturnPrevious(key, hashIndex, nextOffset);
        if (previousEntryIndex < 0) {
            // No entry matches the key.
            return noValue();
        }
        int entryToRemoveIndex = previousEntryIndex == Integer.MAX_VALUE ?
                hashIndex : addOffset(previousEntryIndex, Math.abs(next[previousEntryIndex]), next.length);
        return remove(hashIndex, previousEntryIndex, entryToRemoveIndex);
    }

    @Override
    public int removeAll(KTypeContainer<? super KType> other) {
        int before = this.size();
        // Checks if container is bigger than number of keys
        if (other.size() >= this.size() && other instanceof KTypeLookupContainer<?>) {
            final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
            int slot = 0;
            int max = this.keys.length;

            // Going over all the keys they bound their complexity by capacity of the map instead of container size
            while (slot < max) {
                KType existing = keys[slot];
                if (next[slot] != 0 && other.contains(existing)) {
                    this.remove(existing);
                } else {
                    slot++;
                }
            }
        } else {
            for (KTypeCursor<?> c : other) {
                this.remove(Intrinsics.<KType> cast(c.value));
            }
        }
        // returns number of removed elements
        return before - this.size();
    }

    @Override
    public int removeAll(KTypePredicate<? super KType> predicate) {
        int before = this.size();

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        int slot = 0;
        int max = this.keys.length;

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

    @Override
    public int removeAll(KTypeVTypePredicate<? super KType, ? super VType> predicate) {
        int before = this.size();
        int max = this.keys.length;

        final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
        final VType[] values = Intrinsics.<VType[]> cast(this.values);
        int slot = 0;

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
        for (int slot = 0, max = this.keys.length; slot < max && (next[slot] == 0 || predicate.apply(keys[slot], values[slot])); ++slot);
        return predicate;
    }

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

    @Override
    public boolean containsKey(KType key) {
        int hashIndex = hashReduce(key);
        int nextOffset = next[hashIndex];
        if (nextOffset <= 0) {
            return false;
        }
        return searchInChain(key, hashIndex, nextOffset) >= 0;
    }

    @Override
    public void clear() {
        Arrays.fill(next, (byte) 0);
        size = 0;

        /* #if ($TemplateOptions.KTypeGeneric) */
        Arrays.fill(keys, Intrinsics.<KType> empty());
        /* #end */

        /* #if ($TemplateOptions.VTypeGeneric) */
        Arrays.fill(values, noValue());
        /* #end */
    }

    @Override
    public void release() {
        keys = null;
        values = null;
        next = null;
        size = 0;
        ensureCapacity(DEFAULT_EXPECTED_ELEMENTS);
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
        return Objects.equals(v1, v2);
    }
    /*! #end !*/

    @Override
    public int hashCode() {
        int hashCode = 0;
        // Iterate all entries.
        final int size = this.size;
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                hashCode += WormUtil.hash(keys[index]) ^ WormUtil.hash(values[index]);
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
        return BitMixer.mixPhi(key, keyMixer);
    }

    private int hashReduce(KType key) {
        return hashKey(key) & (next.length - 1);
    }

    @Override
    public int indexOf(KType key) {
        int hashIndex = hashReduce(key);
        int nextOffset = next[hashIndex];
        if (nextOffset <= 0) {
            return ~hashIndex;
        }
        return searchInChain(key, hashIndex, nextOffset);
    }

    @Override
    public boolean indexExists(int index) {
        assert index < next.length;
        return index >= 0;
    }

    @Override
    public VType indexGet(int index) {
        assert checkIndex(index, next.length);
        return Intrinsics.<VType>cast(values[index]);
    }

    @Override
    public VType indexReplace(int index, VType newValue) {
        assert checkIndex(index, next.length);
        VType previousValue = Intrinsics.<VType>cast(values[index]);
        values[index] = newValue;
        return previousValue;
    }

    @Override
    public void indexInsert(int index, KType key, VType value) {
        assert index < 0 : "The index must not point at an existing key.";
        index = ~index;
        if (next[index] == 0) {
            keys[index] = key;
            values[index] = value;
            next[index] = END_OF_CHAIN;
            size++;
        } else {
            put(key, value, PutPolicy.NEW_GUARANTEED, true);
        }
    }

    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append('[');
        // Iterate all entries.
        for (int index = 0, entryCount = 0; entryCount < size; index++) {
            if (next[index] != 0) {
                if (entryCount > 0) {
                    sBuilder.append(", ");
                }
                sBuilder.append(keys[index]);
                sBuilder.append("=>");
                sBuilder.append(values[index]);
                entryCount++;
            }
        }
        sBuilder.append(']');
        return sBuilder.toString();
    }

    @Override
    public void ensureCapacity(int expectedElements) {
        allocateBuffers((int) (expectedElements / FIT_LOAD_FACTOR));
    }

    @Override
    public String visualizeKeyDistribution(int characters) {
        throw new UnsupportedOperationException("Visualization is not supported yet");
    }

    @Override
    public long ramBytesAllocated() {
        // int: size
        return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES
                + RamUsageEstimator.shallowSizeOf(keys)
                + RamUsageEstimator.shallowSizeOf(values)
                + RamUsageEstimator.shallowSizeOf(next);
    }

    @Override
    public long ramBytesUsed() {
        // int: size
        return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES
                + RamUsageEstimator.shallowUsedSizeOfArray(keys, size())
                + RamUsageEstimator.shallowUsedSizeOfArray(values, size())
                + RamUsageEstimator.shallowUsedSizeOfArray(next, size());
    }

    private void allocateBuffers(int capacity) {
        if (capacity < size) {
            throw new IllegalArgumentException("Illegal capacity=" + capacity + " (size=" + size + ")");
        }
        capacity = Math.max(BitUtil.nextHighestPowerOfTwo(capacity), MIN_HASH_ARRAY_LENGTH);
        if (capacity > MAX_HASH_ARRAY_LENGTH) {
            throw new BufferAllocationException("Maximum array size exceeded (capacity: %d)", capacity);
        }
        if (keys != null && keys.length == capacity) {
            return;
        }

        KType[] oldKeys = Intrinsics.<KType[]>cast(keys);
        VType[] oldValues = Intrinsics.<VType[]>cast(values);
        byte[] oldNext = next;
        keys = Intrinsics.<KType>newArray(capacity);
        values = Intrinsics.<VType>newArray(capacity);
        next = new byte[capacity];
        keyMixer = HashOrderMixing.randomized().newKeyMixer(0);

        if (oldKeys != null) {
            putOldEntries(oldKeys, oldValues, oldNext, size);
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
     */
    private void putOldEntries(KType[] oldKeys, VType[] oldValues, byte[] oldNext, int entryNum) {
        int entryCount = 0;
        // Iterate new entries.
        // The condition on index < endIndex is required because the putNewEntry() call below may need to
        // enlarge the map, which calls this method again. And in this case entryNum is larger than the real number.
        for (int index = 0, endIndex = oldKeys.length; entryCount < entryNum && index < endIndex; index++) {
            if (oldNext[index] != 0) {
                // Compute the key hash index.
                KType oldKey = oldKeys[index];
                int hashIndex = hashReduce(oldKey);
                putNewEntry(hashIndex, next[hashIndex], oldKey, oldValues[index]);
                entryCount++;
            }
        }
    }

    /**
     * Puts an entry in this map.
     *
     * @param sizeIncrease Whether to increment {@link #size}.
     * @return The previous entry value (exact {@code requiredPreviousValue} reference if it matches);
     * or {@link #noValue()} if there was no previous entry.
     * @see #put(KType, VType)
     */
    private VType put(KType key, VType value, PutPolicy policy, boolean sizeIncrease) {
        // Compute the key hash index.
        int hashIndex = hashReduce(key);
        int nextOffset = next[hashIndex];

        boolean added = false;
        if (nextOffset > 0 && policy != PutPolicy.NEW_GUARANTEED) {
            // The bucket contains a head-of-chain entry.

            // Look for the key in the chain.
            int entryIndex = searchInChain(key, hashIndex, nextOffset);
            if (entryIndex >= 0) {
                // An entry in the chain matches the key. Replace the value and return the previous one.
                VType previousValue = Intrinsics.<VType>cast(values[entryIndex]);
                if (policy != PutPolicy.NEW_ONLY_IF_ABSENT) {
                    values[entryIndex] = value;
                }
                return previousValue;
            }

            if (enlargeIfNeeded()) {
                hashIndex = hashReduce(key);
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
        } else if (enlargeIfNeeded()) {
            hashIndex = hashReduce(key);
            nextOffset = next[hashIndex];
        }

        if (!added) {
            // No entry matches the key. Add the new entry.
            putNewEntry(hashIndex, nextOffset, key, value);
        }

        if (sizeIncrease) {
            size++;
        }
        return noValue();
    }

    private boolean enlargeIfNeeded() {
        if (size >= next.length) {
            allocateBuffers(next.length << 1);
            return true;
        }
        return false;
    }

    private void enlargeAndPutNewEntry(KType key, VType value) {
        allocateBuffers(next.length << 1);
        put(key, value, PutPolicy.NEW_GUARANTEED, false);
    }

    /**
     * Removes the entry at the specified removal index.
     * Decrements {@link #size}.
     *
     * @param headIndex          The head-of-chain index.
     * @param previousEntryIndex The index of the entry in the chain preceding the entry to remove; or
     *                           {@link Integer#MAX_VALUE} if the entry to remove is the head-of-chain.
     * @param entryToRemoveIndex The index of the entry to remove.
     * @return The value of the removed entry.
     */
    private VType remove(int headIndex, int previousEntryIndex, int entryToRemoveIndex) {
        assert checkIndex(headIndex, next.length);
        assert next[headIndex] > 0;
        assert previousEntryIndex == Integer.MAX_VALUE || checkIndex(previousEntryIndex, next.length);
        assert checkIndex(entryToRemoveIndex, next.length);

        final byte[] next = this.next;
        VType previousValue = Intrinsics.<VType>cast(values[entryToRemoveIndex]);

        // Find the last entry of the chain.
        int beforeLastIndex = findLastOfChain(entryToRemoveIndex, next[entryToRemoveIndex], true, next);
        int lastIndex;
        if (beforeLastIndex == Integer.MAX_VALUE) {
            beforeLastIndex = previousEntryIndex;
            lastIndex = entryToRemoveIndex;
        } else {
            lastIndex = addOffset(beforeLastIndex, Math.abs(next[beforeLastIndex]), next.length);
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
        size--;
        return previousValue;
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
        final int capacity = next.length;
        int searchFromIndex = addOffset(lastEntryIndex, 1, capacity);
        int freeIndex = searchFreeBucket(searchFromIndex, maxOffset(capacity), -1, next);
        if (freeIndex == -1) {
            freeIndex = searchAndMoveBucket(searchFromIndex, maxOffset(capacity), excludedIndexes, recursiveCallLevel);
            if (freeIndex == -1)
                return false;
        }
        keys[freeIndex] = key;
        values[freeIndex] = value;
        next[freeIndex] = -END_OF_CHAIN;
        int nextOffset = getOffsetBetweenIndexes(lastEntryIndex, freeIndex, next.length);
        next[lastEntryIndex] = (byte) (next[lastEntryIndex] > 0 ? nextOffset : -nextOffset); // Keep the offset sign.
        return true;
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
    private int searchAndMoveBucket(int fromIndex, int range, ExcludedIndexes excludedIndexes, int recursiveCallLevel) {
        assert checkIndex(fromIndex, next.length);
        assert range >= 0 && range <= maxOffset(next.length) : "range=" + range + ", maxOffset=" + maxOffset(next.length);
        int remainingAttempts = RECURSIVE_MOVE_ATTEMPTS[recursiveCallLevel];
        if (remainingAttempts <= 0 || range <= 0) {
            return -1;
        }
        final byte[] next = this.next;
        final int capacity = next.length;
        int nextRecursiveCallLevel = recursiveCallLevel + 1;
        for (int index = fromIndex + range - 1; index >= fromIndex; index--) {
            int rolledIndex = index & (capacity - 1);
            if (excludedIndexes.isIndexExcluded(rolledIndex)) {
                continue;
            }
            int nextOffset = next[rolledIndex];
            if (nextOffset < 0) {
                // Attempt to move the tail of chain.
                if (moveTailOfChain(rolledIndex, nextOffset, excludedIndexes, nextRecursiveCallLevel))
                    return rolledIndex;
                if (--remainingAttempts <= 0)
                    return -1;
            }
        }
        return -1;
    }

    /**
     * Puts a new entry that is guaranteed not to be already contained by this map. <p>This method does not modify this
     * map {@link #size}. It may enlarge this map if it needs room to put the entry.</p>
     *
     * @param hashIndex          The hash index where to put the entry (= {@link #hashReduce}(key)).
     * @param nextOffset         The current value of {@link #next}[hashIndex].
     */
    private void putNewEntry(int hashIndex, int nextOffset, KType key, VType value) {
        assert hashIndex == hashReduce(key) : "hashIndex=" + hashIndex + ", hashReduce(key)=" + hashReduce(key);
        assert checkIndex(hashIndex, next.length);
        assert Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
        assert nextOffset == next[hashIndex] : "nextOffset=" + nextOffset + ", next[hashIndex]=" + next[hashIndex];

        if (nextOffset > 0) {
            // The bucket contains a head-of-chain entry.
            // Append the new entry at the chain tail, after the last entry of the chain. If there is no free bucket in
            // the range, enlarge this map and put the new entry.
            if (!appendTailOfChain(findLastOfChain(hashIndex, nextOffset, false, next), key, value)) {
                enlargeAndPutNewEntry(key, value);
            }
        } else {
            if (nextOffset < 0) {
                // Bucket at hash index contains a movable tail-of-chain entry. Move it to free the bucket.
                if (!moveTailOfChain(hashIndex, nextOffset, ExcludedIndexes.NONE, 0)) {
                    // No free bucket in the range. Enlarge the map and put again.
                    enlargeAndPutNewEntry(key, value);
                    return;
                }
            }
            // Bucket at hash index is free. Add the new head-of-chain entry.
            keys[hashIndex] = key;
            values[hashIndex] = value;
            next[hashIndex] = END_OF_CHAIN;
        }
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
        assert checkIndex(tailIndex, next.length);
        assert nextOffset < 0 && nextOffset >= -END_OF_CHAIN : "nextOffset=" + nextOffset;
        assert nextOffset == next[tailIndex] : "nextOffset=" + nextOffset + ", next[tailIndex]=" + next[tailIndex];

        // Find the next free bucket by linear probing.
        // It must be within a range of maxOffset of the previous entry in the chain,
        // and not beyond the next entry in the chain.
        final byte[] next = this.next;
        final int capacity = next.length;
        final int maxOffset = maxOffset(capacity);
        int previousIndex = findPreviousInChain(tailIndex, next);
        int absPreviousOffset = Math.abs(next[previousIndex]);
        int nextIndex = nextOffset == -END_OF_CHAIN ? -1 : addOffset(tailIndex, -nextOffset, capacity);
        int offsetFromPreviousToNext = absPreviousOffset - nextOffset;
        int searchFromIndex;
        int searchRange;
        boolean nextIndexWithinRange;
        // Compare [the offset from previous entry to next entry] to [maxOffset].
        if (offsetFromPreviousToNext <= maxOffset) {
            // The next entry in the chain is inside the maximum offset range.
            // Prepare to search for a free bucket starting from the tail-of-chain entry, up to the next entry in the chain.
            searchFromIndex = addOffset(previousIndex, 1, capacity);
            searchRange = offsetFromPreviousToNext - 1;
            nextIndexWithinRange = true;
        } else {
            // The next entry is not inside the maximum range. It is always the case if nextOffset is -END_OF_CHAIN.
            // Prepare to search for a free bucket starting from the tail-of-chain entry, up to maxOffset from the
            // previous entry.
            if (nextIndex == -1) {
                searchFromIndex = addOffset(previousIndex, 1, capacity);
                searchRange = maxOffset;
            } else {
                searchFromIndex = addOffset(nextIndex, -maxOffset, capacity);
                int searchToIndex = addOffset(previousIndex, maxOffset, capacity);
                searchRange = getOffsetBetweenIndexes(searchFromIndex, searchToIndex, capacity) + 1;
            }
            nextIndexWithinRange = false;
        }
        int freeIndex = searchFreeBucket(searchFromIndex, searchRange, tailIndex, next);
        if (freeIndex == -1) {
            // No free bucket in the range.
            if (nextIndexWithinRange && appendTailOfChain(
                    findLastOfChain(nextIndex, next[nextIndex], false, next), Intrinsics.<KType>cast(keys[tailIndex]), Intrinsics.<VType>cast(values[tailIndex]), excludedIndexes, recursiveCallLevel)) {
                // The entry to move has been appended to the tail of the chain.
                // Complete the move by linking the previous entry to the next entry (which is within range).
                int previousOffset = getOffsetBetweenIndexes(previousIndex, nextIndex, capacity);
                next[previousIndex] = (byte) (next[previousIndex] > 0 ? previousOffset : -previousOffset); // Keep the offset sign.
                return true;
            } else {
                ExcludedIndexes recursiveExcludedIndexes = excludedIndexes.union(ExcludedIndexes.fromChain(previousIndex, next));
                if ((freeIndex = searchAndMoveBucket(searchFromIndex, searchRange, recursiveExcludedIndexes, recursiveCallLevel)) == -1) {
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
        next[freeIndex] = (byte) (nextOffset == -END_OF_CHAIN ? nextOffset : -getOffsetBetweenIndexes(freeIndex, nextIndex, capacity));
        int previousOffset = getOffsetBetweenIndexes(previousIndex, freeIndex, capacity);
        next[previousIndex] = (byte) (next[previousIndex] > 0 ? previousOffset : -previousOffset); // Keep the offset sign.
        assert next[freeIndex] < 0 : "freeIndex=" + freeIndex + ", next[freeIndex]=" + next[freeIndex];
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
        assert checkIndex(index, next.length);
        assert nextOffset > 0 && nextOffset <= END_OF_CHAIN : "nextOffset=" + nextOffset;
        assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];

        // There is at least one entry at this bucket. Check the first head-of-chain.
        if (Intrinsics.<KType> equals(keys[index], key)) {
            // The first head-of-chain entry matches the key. Return its index.
            return index;
        }

        // Follow the entry chain for this bucket.
        final int capacity = next.length;
        while (nextOffset != END_OF_CHAIN) {
            index = addOffset(index, nextOffset, capacity); // Jump forward.
            if (Intrinsics.<KType> equals(keys[index], key)) {
                // An entry in the chain matches the key. Return its index.
                return index;
            }
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            assert nextOffset > 0 : "nextOffset=" + nextOffset;
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
        assert checkIndex(index, next.length);
        assert nextOffset > 0 && nextOffset <= END_OF_CHAIN : "nextOffset=" + nextOffset;
        assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];

        // There is at least one entry at this bucket. Check the first head-of-chain.
        if (Intrinsics.<KType> equals(keys[index], key)) {
            // The first head-of-chain entry matches the key. Return Integer.MAX_VALUE as there is no previous entry.
            return Integer.MAX_VALUE;
        }

        // Follow the entry chain for this bucket.
        final int capacity = next.length;
        while (nextOffset != END_OF_CHAIN) {
            int previousIndex = index;
            index = addOffset(index, nextOffset, capacity); // Jump forward.
            if (Intrinsics.<KType> equals(keys[index], key)) {
                // An entry in the chain matches the key. Return the previous entry index.
                return previousIndex;
            }
            nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
            assert nextOffset > 0 : "nextOffset=" + nextOffset;
        }

        // No entry matches the key. Return the last entry index as 2's complement.
        return ~index;
    }

    /**
     * A view of the keys inside this hash map.
     */
    final class KeysContainer extends AbstractKTypeCollection<KType>
                                    implements KTypeLookupContainer<KType> {
        @Override
        public boolean contains(KType e) {
            return KTypeVTypeWormMap.this.containsKey(e);
        }

        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
            KTypeVTypeWormMap.this.forEach(
                (KTypeVTypeProcedure<KType, VType>) (key, value) -> procedure.apply(key));
            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
            KTypeVTypeWormMap.this.forEach(
                (KTypeVTypePredicate<KType, VType>) (key, value) -> predicate.apply(key));
            return predicate;
        }

        @Override
        public boolean isEmpty() {
            return KTypeVTypeWormMap.this.isEmpty();
        }

        @Override
        public Iterator<KTypeCursor<KType>> iterator() {
            return new KeysIterator();
        }

        @Override
        public int size() {
            return KTypeVTypeWormMap.this.size();
        }

        @Override
        public void clear() {
            KTypeVTypeWormMap.this.clear();
        }

        @Override
        public void release() {
            KTypeVTypeWormMap.this.release();
        }

        @Override
        public int removeAll(KTypePredicate<? super KType> predicate) {
            return KTypeVTypeWormMap.this.removeAll(predicate);
        }

        @Override
        public int removeAll(final KType e) {
            return KTypeVTypeWormMap.this.remove(e) == noValue() ? 0 : 1;
        }
    }

    /**
     * An iterator over the set of assigned keys.
     */
    private class KeysIterator extends AbstractIterator<KTypeCursor<KType>> {
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
    private class ValuesContainer extends AbstractKTypeCollection<VType> {
        @Override
        public int size() {
            return KTypeVTypeWormMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return KTypeVTypeWormMap.this.isEmpty();
        }

        @Override
        public boolean contains(VType value) {
            for (KTypeVTypeCursor<KType, VType> c : KTypeVTypeWormMap.this) {
                if (Intrinsics.<VType> equals(value, c.value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T extends KTypeProcedure<? super VType>> T forEach(T procedure) {
            for (KTypeVTypeCursor<KType, VType> c : KTypeVTypeWormMap.this) {
                procedure.apply(c.value);
            }
            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super VType>> T forEach(T predicate) {
            for (KTypeVTypeCursor<KType, VType> c : KTypeVTypeWormMap.this) {
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
            return KTypeVTypeWormMap.this.removeAll((key, value) -> Intrinsics.<VType> equals(e, value));
        }

        @Override
        public int removeAll(final KTypePredicate<? super VType> predicate) {
            return KTypeVTypeWormMap.this.removeAll((key, value) -> predicate.apply(value));
        }

        @Override
        public void clear() {
            KTypeVTypeWormMap.this.clear();
        }

        @Override
        public void release() {
            KTypeVTypeWormMap.this.release();
        }
    }

    /**
     * An iterator over the set of assigned values.
     */
    private class ValuesIterator extends AbstractIterator<KTypeCursor<VType>> {
        private final KTypeCursor<VType> cursor;
        private final int max = keys.length;
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
    private class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>> {
        private final KTypeVTypeCursor<KType, VType> cursor;
        private final int max = keys.length;
        private int slot = -1;

        public EntryIterator() {
            cursor = new KTypeVTypeCursor<KType, VType>();
        }

        @Override
        protected KTypeVTypeCursor<KType, VType> fetch() {
            if (slot < max) {
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
