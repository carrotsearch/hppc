/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

import java.util.Arrays;
import java.util.Iterator;

import static com.carrotsearch.hppc.Containers.DEFAULT_EXPECTED_ELEMENTS;
import static com.carrotsearch.hppc.HashContainers.*;
import static com.carrotsearch.hppc.WormUtil.*;

/**
 * A hash set of <code>KType</code>s, implemented using Worm Hashing strategy.
 *
 * <p>This strategy is appropriate for a medium sized set (less than 2M keys). It takes more time
 * to put keys in the set because it maintains chains of keys having the same hash. Then the
 * lookup speed is fast even if the set is heavy loaded or hashes are clustered. On average it takes
 * slightly more memory than {@link KTypeHashSet}: heavier but the load factor is higher
 * (it varies around 80%) so it enlarges later.</p>
 *
 * @see <a href="{@docRoot}/overview-summary.html#interfaces">HPPC interfaces diagram</a>
 */
/*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeWormSet<KType>
        extends AbstractKTypeCollection<KType>
        implements /*! #if ($templateonly) !*/ Intrinsics.KeyHasher<KType>, /*! #end !*/
        KTypeLookupContainer<KType>,
        KTypeSet<KType>,
        Preallocable,
        Cloneable,
        Accountable {
  /**
   * The array holding keys.
   */
  public /*! #if ($TemplateOptions.KTypeGeneric) !*/
          Object[]
          /*! #else KType [] #end !*/
          keys;

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
   * Set size (number of entries).
   */
  protected int size;

  /**
   * Seed used to ensure the hash iteration order is different from an iteration to another.
   */
  protected int iterationSeed;

  /**
   * New instance with sane defaults.
   */
  public KTypeWormSet() {
    this(DEFAULT_EXPECTED_ELEMENTS);
  }

  /**
   * New instance with the provided defaults.
   *
   * <p>There is no load factor parameter as this set enlarges automatically. In practice the load factor
   * varies around 80% (between 75% and 90%). The load factor is 100% for tiny sets.</p>
   *
   * @param expectedElements The expected number of elements. The capacity of the set is calculated based on it.
   */
  public KTypeWormSet(int expectedElements) {
    if (expectedElements < 0) {
      throw new IllegalArgumentException("Invalid expectedElements=" + expectedElements);
    }
    iterationSeed = HashContainers.nextIterationSeed();
    ensureCapacity(expectedElements);
  }

  /**
   * Creates a new instance from all elements of another container.
   */
  public KTypeWormSet(KTypeContainer<? extends KType> container) {
    this(container.size());
    addAll(container);
  }

  /**
   * Create a set from a variable number of arguments or an array of
   * <code>KType</code>. The elements are copied from the argument to the
   * internal buffer.
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  @SafeVarargs
  /* #end */
  public static <KType> KTypeWormSet<KType> from(KType... elements) {
    KTypeWormSet<KType> set = new KTypeWormSet<KType>(elements.length);
    set.addAll(elements);
    return set;
  }

  /**
   * Clones this set. The cloning operation is efficient because it copies directly the internal arrays, without
   * having to put elements in the cloned set. The cloned set has the same elements and the same capacity as this set.
   *
   * @return A shallow copy of this set.
   */
  @Override
  public KTypeWormSet<KType> clone() {
    try {
      /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
              KTypeWormSet<KType> cloneSet = (KTypeWormSet<KType>) super.clone();
      cloneSet.keys = keys.clone();
      cloneSet.next = next.clone();
      cloneSet.iterationSeed = HashContainers.nextIterationSeed();
      return cloneSet;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return size;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  /** {@inheritDoc} */
  @Override
  public boolean contains(KType key) {
    // Compute the key hash index.
    int hashIndex = hashMod(key);
    int nextOffset = next[hashIndex];

    if (nextOffset <= 0) {
      // The bucket is either free, or only used for chaining, so no entry for the key.
      return false;
    }

    // The bucket contains a head-of-chain entry.
    // Look for the key in the chain.
    return searchInChain(key, hashIndex, nextOffset) >= 0;
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(KType key) {
    return add(key, false, true);
  }

  /**
   * Adds all elements from the given list (vararg) to this set.
   *
   * @return Returns the number of elements actually added as a result of this
   *         call (not previously present in the set).
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  @SafeVarargs
  /* #end */
  public final int addAll(KType... elements) {
    ensureCapacity(elements.length);
    int count = 0;
    for (KType e : elements) {
      if (add(e)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Adds all elements from the given {@link KTypeContainer} to this set.
   *
   * @return Returns the number of elements actually added as a result of this
   *         call (not previously present in the set).
   */
  public int addAll(KTypeContainer<? extends KType> container) {
    ensureCapacity(container.size());
    return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
  }

  /**
   * Adds all elements from the given iterable to this set.
   *
   * @return Returns the number of elements actually added as a result of this
   *         call (not previously present in the set).
   */
  public int addAll(Iterable<? extends KTypeCursor<? extends KType>> iterable) {
    int count = 0;
    for (KTypeCursor<? extends KType> cursor : iterable) {
      if (add(cursor.value)) {
        count++;
      }
    }
    return count;
  }

  /**
   * An alias for the (preferred) {@link #removeAll}.
   */
  public boolean remove(KType key) {
    final byte[] next = this.next;

    // Compute the key hash index.
    int hashIndex = hashMod(key);
    int nextOffset = next[hashIndex];
    if (nextOffset <= 0) {
      // The bucket is either free, or in tail-of-chain, so no entry for the key.
      return false;
    }
    // The bucket contains a head-of-chain entry.
    // Look for the key in the chain.
    int previousEntryIndex = searchInChainReturnPrevious(key, hashIndex, nextOffset);
    if (previousEntryIndex < 0) {
      // No entry matches the key.
      return false;
    }
    int entryToRemoveIndex = previousEntryIndex == Integer.MAX_VALUE ?
            hashIndex : addOffset(previousEntryIndex, Math.abs(next[previousEntryIndex]), next.length);
    remove(entryToRemoveIndex, previousEntryIndex);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int removeAll(KType key) {
    return remove(key) ? 1 : 0;
  }

  /**
   * Removes all keys present in a given container.
   *
   * @return Returns the number of elements actually removed as a result of this call.
   */
  public int removeAll(KTypeContainer<? super KType> other) {
    // Try to iterate over the smaller set or over the container that isn't implementing
    // efficient contains() lookup.
    int size = size();
    if (other.size() >= size && other instanceof KTypeLookupContainer<?>) {
      final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
      final byte[] next = this.next;
      final int capacity = next.length;
      int entryIndex = 0;
      while (entryIndex < capacity) {
        KType key;
        if (next[entryIndex] != 0 && other.contains(key = keys[entryIndex])) {
          this.remove(key);
        } else {
          entryIndex++;
        }
      }
    } else {
      for (KTypeCursor<?> c : other) {
        remove(Intrinsics.<KType>cast(c.value));
      }
    }
    return size - size();
  }

  /** {@inheritDoc} */
  @Override
  public int removeAll(KTypePredicate<? super KType> predicate) {
    final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
    final byte[] next = this.next;
    final int capacity = next.length;
    int size = size();
    int entryIndex = 0;
    while (entryIndex < capacity) {
      KType key;
      if (next[entryIndex] != 0 && predicate.apply(key = keys[entryIndex])) {
        this.remove(key);
      } else {
        entryIndex++;
      }
    }
    return size - size();

  }

  /** {@inheritDoc} */
  @Override
  public <T extends KTypeProcedure<? super KType>> T forEach(T procedure) {
    final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
    final byte[] next = this.next;
    int seed = nextIterationSeed();
    int inc = iterationIncrement(seed);
    for (int i = 0, mask = next.length - 1, slot = seed & mask; i <= mask; i++, slot = (slot + inc) & mask) {
      if (next[slot] != 0) {
        procedure.apply(keys[slot]);
      }
    }
    return procedure;
  }

  /** {@inheritDoc} */
  @Override
  public <T extends KTypePredicate<? super KType>> T forEach(T predicate) {
    final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
    final byte[] next = this.next;
    int seed = nextIterationSeed();
    int inc = iterationIncrement(seed);
    for (int i = 0, mask = next.length - 1, slot = seed & mask; i <= mask; i++, slot = (slot + inc) & mask) {
      if (next[slot] != 0) {
        if (!predicate.apply(keys[slot])) {
          break;
        }
      }
    }
    return predicate;
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<KTypeCursor<KType>> iterator() {
    return new EntryIterator();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    Arrays.fill(next, (byte) 0);
    size = 0;
    /* #if ($TemplateOptions.KTypeGeneric) */
    Arrays.fill(keys, Intrinsics.<KType>empty());
    /* #end */
  }

  /** {@inheritDoc} */
  @Override
  public void release() {
    keys = null;
    next = null;
    size = 0;
    ensureCapacity(DEFAULT_EXPECTED_ELEMENTS);
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    KTypeSet<KType> set = (KTypeSet<KType>) o;
    final int size = this.size;
    if (size != set.size()) {
      return false;
    }
    final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
    final byte[] next = this.next;
    // Iterate all entries.
    for (int index = 0, entryCount = 0; entryCount < size; index++) {
      if (next[index] != 0) {
        if (!set.contains(keys[index])) {
          return false;
        }
        entryCount++;
      }
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    int hashCode = 0;
    // Iterate all entries.
    final int size = this.size;
    for (int index = 0, entryCount = 0; entryCount < size; index++) {
      if (next[index] != 0) {
        hashCode += BitMixer.mixPhi(keys[index]);
        entryCount++;
      }
    }
    return hashCode;
  }

  /*! #if ($templateonly) !*/
  /** {@inheritDoc} */
  @Override
  public
  /*! #else protected #end !*/
  int hashKey(KType key) {
    return BitMixer.mixPhi(key);
  }

  private int hashMod(KType key) {
    return hashKey(key) & (next.length - 1);
  }

  /**
   * Returns a logical "index" of a given key that can be used to speed up
   * follow-up logic in certain scenarios (conditional logic).
   *
   * The semantics of "indexes" are not strictly defined. Indexes may
   * (and typically won't be) contiguous.
   *
   * The index is valid only between modifications (it will not be affected
   * by read-only operations).
   *
   * @see #indexExists
   * @see #indexGet
   * @see #indexInsert
   * @see #indexReplace
   *
   * @param key
   *          The key to locate in the set.
   * @return A non-negative value of the logical "index" of the key in the set
   *         or a negative value if the key did not exist.
   */
  public int indexOf(KType key) {
    int hashIndex = hashMod(key);
    int nextOffset = next[hashIndex];
    if (nextOffset <= 0) {
      return ~hashIndex;
    }
    return searchInChain(key, hashIndex, nextOffset);
  }

  /**
   * @see #indexOf
   *
   * @param index The index of a given key, as returned from {@link #indexOf}.
   * @return Returns <code>true</code> if the index corresponds to an existing key
   *         or false otherwise. This is equivalent to checking whether the index is
   *         a positive value (existing keys) or a negative value (non-existing keys).
   */
  public boolean indexExists(int index) {
    assert index < next.length;
    return index >= 0;
  }

  /**
   * Returns the exact value of the existing key. This method makes sense for sets
   * of objects which define custom key-equality relationship.
   *
   * @see #indexOf
   *
   * @param index The index of an existing key.
   * @return Returns the equivalent key currently stored in the set.
   * @throws AssertionError If assertions are enabled and the index does
   *         not correspond to an existing key.
   */
  public KType indexGet(int index) {
    assert checkIndex(index, next.length);
    assert next[index] != 0;
    return Intrinsics.<KType>cast(keys[index]);
  }

  /**
   * Replaces the existing equivalent key with the given one and returns any previous value
   * stored for that key.
   *
   * @see #indexOf
   *
   * @param index The index of an existing key.
   * @param equivalentKey The key to put in the set as a replacement. Must be equivalent to
   *        the key currently stored at the provided index.
   * @return Returns the previous key stored in the set.
   * @throws AssertionError If assertions are enabled and the index does
   *         not correspond to an existing key.
   */
  public KType indexReplace(int index, KType equivalentKey) {
    assert checkIndex(index, next.length);
    assert next[index] != 0;
    assert Intrinsics.equals(this, keys[index], equivalentKey);
    KType previousKey = Intrinsics.<KType>cast(keys[index]);
    keys[index] = equivalentKey;
    return previousKey;
  }

  /**
   * Inserts a key for an index that is not present in the set. This method
   * may help in avoiding double recalculation of the key's hash.
   *
   * @see #indexOf
   *
   * @param index The index of a previously non-existing key, as returned from
   *              {@link #indexOf}.
   * @throws AssertionError If assertions are enabled and the index does
   *         not correspond to an existing key.
   */
  public void indexInsert(int index, KType key) {
    assert index < 0 : "The index must not point at an existing key.";
    index = ~index;
    if (next[index] == 0) {
      keys[index] = key;
      next[index] = END_OF_CHAIN;
      size++;
    } else {
      add(key, true, true);
    }
  }

  /**
   * Removes a key at an index previously acquired from {@link #indexOf}.
   *
   * @see #indexOf
   *
   * @param index The index of the key to remove, as returned from {@link #indexOf}.
   * @throws AssertionError If assertions are enabled and the index does
   *         not correspond to an existing key.
   */
  public void indexRemove(int index) {
    assert checkIndex(index, next.length);
    assert next[index] != 0;
    remove(index, Integer.MAX_VALUE);
  }

  /** {@inheritDoc} */
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
        entryCount++;
      }
    }
    sBuilder.append(']');
    return sBuilder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public void ensureCapacity(int expectedElements) {
    allocateBuffers((int) (expectedElements / FIT_LOAD_FACTOR));
  }

  /** {@inheritDoc} */
  @Override
  public String visualizeKeyDistribution(int characters) {
    return KTypeBufferVisualizer.visualizeKeyDistribution(keys, next.length - 1, characters);
  }

  /** {@inheritDoc} */
  @Override
  public long ramBytesAllocated() {
    // int: size, iterationSeed
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES * 2
            + RamUsageEstimator.shallowSizeOfArray(keys)
            + RamUsageEstimator.shallowSizeOfArray(next);
  }

  /** {@inheritDoc} */
  @Override
  public long ramBytesUsed() {
    // int: size, iterationSeed
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES * 2
            + RamUsageEstimator.shallowUsedSizeOfArray(keys, size())
            + RamUsageEstimator.shallowUsedSizeOfArray(next, size());
  }

  protected void allocateBuffers(int capacity) {
    capacity = Math.max(capacity, size);
    capacity = Math.max(BitUtil.nextHighestPowerOfTwo(capacity), MIN_HASH_ARRAY_LENGTH);
    if (capacity > MAX_HASH_ARRAY_LENGTH) {
      throw new BufferAllocationException("Maximum array size exceeded (capacity: %d)", capacity);
    }
    if (keys != null && keys.length == capacity) {
      return;
    }

    KType[] oldKeys = Intrinsics.<KType[]>cast(keys);
    byte[] oldNext = next;
    keys = Intrinsics.<KType>newArray(capacity);
    next = new byte[capacity];

    if (oldKeys != null) {
      putOldEntries(oldKeys, oldNext, size);
    }
  }

  /**
   * Puts old entries after enlarging this set. Old entries are guaranteed not to be already contained by this set.
   * <p>This method does not modify this set {@link #size}. It may enlarge this set if it needs room to put the entry.</p>
   *
   * @param oldKeys   The old keys.
   * @param oldNext   The old next offsets.
   * @param entryNum  The number of non null old entries. It is supported to set a value larger than the real count.
   */
  private void putOldEntries(KType[] oldKeys, byte[] oldNext, int entryNum) {
    int entryCount = 0;
    // Iterate new entries.
    // The condition on index < endIndex is required because the putNewEntry() call below may need to
    // enlarge the set, which calls this method again. And in this case entryNum is larger than the real number.
    for (int index = 0, endIndex = oldNext.length; entryCount < entryNum && index < endIndex; index++) {
      if (oldNext[index] != 0) {
        // Compute the key hash index.
        KType oldKey = oldKeys[index];
        int hashIndex = hashMod(oldKey);
        putNewEntry(hashIndex, next[hashIndex], oldKey);
        entryCount++;
      }
    }
  }

  /**
   * Adds an element in this set.
   *
   * @param newGuaranteed Whether the element is guaranteed to not be already present.
   * @param sizeIncrease Whether to increment {@link #size}.
   * @return {@code true} if the element has been added; {@code false} otherwise.
   */
  private boolean add(KType key, boolean newGuaranteed, boolean sizeIncrease) {
    // Compute the key hash index.
    int hashIndex = hashMod(key);
    int nextOffset = next[hashIndex];

    boolean added = false;
    if (nextOffset > 0 && !newGuaranteed) {
      // The bucket contains a head-of-chain entry.

      // Look for the key in the chain.
      int entryIndex = searchInChain(key, hashIndex, nextOffset);
      if (entryIndex >= 0) {
        // An entry in the chain matches the key. Do not replace the existing element.
        return false;
      }

      if (enlargeIfNeeded()) {
        hashIndex = hashMod(key);
        nextOffset = next[hashIndex];
      } else {
        // No entry matches the key. Append the new entry at the tail of the chain.
        // ~entryIndex is the index of the last entry in the chain.
        if (!appendTailOfChain(~entryIndex, key)) {
          // No free bucket in the range. Enlarge the set and put again.
          enlargeAndPutNewEntry(key);
        }
        added = true;
      }
    } else if (enlargeIfNeeded()) {
      hashIndex = hashMod(key);
      nextOffset = next[hashIndex];
    }

    if (!added) {
      // No entry matches the key. Add the new entry.
      putNewEntry(hashIndex, nextOffset, key);
    }

    if (sizeIncrease) {
      size++;
    }
    return true;
  }

  private boolean enlargeIfNeeded() {
    if (size >= next.length) {
      allocateBuffers(next.length << 1);
      return true;
    }
    return false;
  }

  private void enlargeAndPutNewEntry(KType key) {
    allocateBuffers(next.length << 1);
    add(key, true, false);
  }

  /**
   * Removes the entry at the specified removal index.
   * Decrements {@link #size}.
   *
   * @param entryToRemoveIndex The index of the entry to remove.
   * @param previousEntryIndex The index of the entry in the chain preceding the entry to remove; or
 *                             {@link Integer#MAX_VALUE} if unknown or if the entry to remove is the head-of-chain.
   */
  private void remove(int entryToRemoveIndex, int previousEntryIndex) {
    assert checkIndex(entryToRemoveIndex, next.length);
    assert previousEntryIndex == Integer.MAX_VALUE || checkIndex(previousEntryIndex, next.length);

    final byte[] next = this.next;

    // Find the last entry of the chain.
    // Replace the removed entry by the last entry of the chain.
    int nextOffset = next[entryToRemoveIndex];
    int beforeLastIndex = findLastOfChain(entryToRemoveIndex, nextOffset, true, next);
    int lastIndex;
    if (beforeLastIndex == -1) {
      // The entry to remove is the last of the chain.
      lastIndex = entryToRemoveIndex;
      if (nextOffset < 0) {
        // Removing the last entry in a chain of at least two entries.
        beforeLastIndex = previousEntryIndex == Integer.MAX_VALUE ?
                findPreviousInChain(entryToRemoveIndex, next) : previousEntryIndex;
        // Unlink the last entry which replaces the removed entry.
        next[beforeLastIndex] = (byte) (next[beforeLastIndex] > 0 ? END_OF_CHAIN : -END_OF_CHAIN);
      }
    } else {
      int beforeLastNextOffset = next[beforeLastIndex];
      lastIndex = addOffset(beforeLastIndex, Math.abs(beforeLastNextOffset), next.length);
      assert entryToRemoveIndex != lastIndex;
      // The entry to remove is before the last of the chain. Replace it by the last one.
      keys[entryToRemoveIndex] = keys[lastIndex];
      // Unlink the last entry which replaces the removed entry.
      next[beforeLastIndex] = (byte) (beforeLastNextOffset > 0 ? END_OF_CHAIN : -END_OF_CHAIN);
    }
    // Free the last entry of the chain.
    keys[lastIndex] = Intrinsics.<KType>empty();
    next[lastIndex] = 0;
    size--;
  }

  /**
   * Appends a new entry at the tail of an entry chain.
   *
   * @param lastEntryIndex The index of the last entry in the chain.
   * @return <code>true</code> if the new entry is added successfully; <code>false</code> if there is no free bucket
   * in the range (so this set needs to be enlarged to make room).
   */
  private boolean appendTailOfChain(int lastEntryIndex, KType key) {
    return appendTailOfChain(lastEntryIndex, key, ExcludedIndexes.NONE, 0);
  }

  /**
   * Appends a new entry at the tail of an entry chain.
   *
   * @param lastEntryIndex     The index of the last entry in the chain.
   * @param excludedIndexes    Indexes to exclude from the search.
   * @param recursiveCallLevel Keeps track of the recursive call level (starts at 0).
   * @return <code>true</code> if the new entry is added successfully; <code>false</code> if there is no free bucket
   * in the range (so this set needs to be enlarged to make room).
   */
  private boolean appendTailOfChain(int lastEntryIndex, KType key, ExcludedIndexes excludedIndexes, int recursiveCallLevel) {
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
   * Puts a new entry that is guaranteed not to be already contained by this set. <p>This method does not modify this
   * set {@link #size}. It may enlarge this set if it needs room to put the entry.</p>
   *
   * @param hashIndex  The hash index where to put the entry (= {@link #hashMod}(key)).
   * @param nextOffset The current value of {@link #next}[hashIndex].
   */
  private void putNewEntry(int hashIndex, int nextOffset, KType key) {
    assert hashIndex == hashMod(key) : "hashIndex=" + hashIndex + ", hashReduce(key)=" + hashMod(key);
    assert checkIndex(hashIndex, next.length);
    assert Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
    assert nextOffset == next[hashIndex] : "nextOffset=" + nextOffset + ", next[hashIndex]=" + next[hashIndex];

    if (nextOffset > 0) {
      // The bucket contains a head-of-chain entry.
      // Append the new entry at the chain tail, after the last entry of the chain. If there is no free bucket in
      // the range, enlarge this set and put the new entry.
      if (!appendTailOfChain(findLastOfChain(hashIndex, nextOffset, false, next), key)) {
        enlargeAndPutNewEntry(key);
      }
    } else {
      if (nextOffset < 0) {
        // Bucket at hash index contains a movable tail-of-chain entry. Move it to free the bucket.
        if (!moveTailOfChain(hashIndex, nextOffset, ExcludedIndexes.NONE, 0)) {
          // No free bucket in the range. Enlarge the set and put again.
          enlargeAndPutNewEntry(key);
          return;
        }
      }
      // Bucket at hash index is free. Add the new head-of-chain entry.
      keys[hashIndex] = key;
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
              findLastOfChain(nextIndex, next[nextIndex], false, next), Intrinsics.<KType>cast(keys[tailIndex]), excludedIndexes, recursiveCallLevel)) {
        // The entry to move has been appended to the tail of the chain.
        // Complete the move by linking the previous entry to the next entry (which is within range).
        int previousOffset = getOffsetBetweenIndexes(previousIndex, nextIndex, capacity);
        next[previousIndex] = (byte) (next[previousIndex] > 0 ? previousOffset : -previousOffset); // Keep the offset sign.
        return true;
      } else {
        ExcludedIndexes recursiveExcludedIndexes = excludedIndexes.union(ExcludedIndexes.fromChain(previousIndex, next));
        if ((freeIndex = searchAndMoveBucket(searchFromIndex, searchRange, recursiveExcludedIndexes, recursiveCallLevel)) == -1) {
          // No free bucket after the tail of the chain, and no movable entry. No bucket available around.
          // The move fails (and this set will be enlarged by the calling method).
          return false;
        }
      }
    }
    // Move the entry to the free index.
    // No need to set keys[tailIndex] to null here because they will be set when this method returns,
    // or the set will be enlarged and rehashed.
    keys[freeIndex] = keys[tailIndex];
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
    if (Intrinsics.<KType>equals(keys[index], key)) {
      // The first head-of-chain entry matches the key. Return its index.
      return index;
    }

    // Follow the entry chain for this bucket.
    final int capacity = next.length;
    while (nextOffset != END_OF_CHAIN) {
      index = addOffset(index, nextOffset, capacity); // Jump forward.
      if (Intrinsics.<KType>equals(keys[index], key)) {
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
    if (Intrinsics.<KType>equals(keys[index], key)) {
      // The first head-of-chain entry matches the key. Return Integer.MAX_VALUE as there is no previous entry.
      return Integer.MAX_VALUE;
    }

    // Follow the entry chain for this bucket.
    final int capacity = next.length;
    while (nextOffset != END_OF_CHAIN) {
      int previousIndex = index;
      index = addOffset(index, nextOffset, capacity); // Jump forward.
      if (Intrinsics.<KType>equals(keys[index], key)) {
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
   * Provides the next iteration seed used to build the iteration starting slot and offset increment.
   * This method does not need to be synchronized, what matters is that each thread gets a sequence of varying seeds.
   */
  protected int nextIterationSeed() {
    return iterationSeed = BitMixer.mixPhi(iterationSeed);
  }

  /**
   * An iterator over the elements.
   */
  private class EntryIterator extends AbstractIterator<KTypeCursor<KType>> {
    private final KTypeCursor<KType> cursor;
    private final int increment;
    private int index;
    private int slot;

    public EntryIterator() {
      cursor = new KTypeCursor<KType>();
      int seed = nextIterationSeed();
      increment = iterationIncrement(seed);
      slot = seed & (next.length - 1);
    }

    @Override
    protected KTypeCursor<KType> fetch() {
      final int mask = next.length - 1;
      while (index <= mask) {
        index++;
        slot = (slot + increment) & mask;
        if (next[slot] != 0) {
          cursor.index = slot;
          cursor.value = Intrinsics.<KType>cast(keys[slot]);
          return cursor;
        }
      }
      return done();
    }
  }
}
