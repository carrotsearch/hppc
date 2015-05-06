package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.HashContainers.*;
import static com.carrotsearch.hppc.Containers.*;

/**
 * A hash set of <code>KType</code>s, implemented using using open addressing
 * with linear probing for collision resolution.
 * 
 * <p>
 * <strong>Note:</strong> read about 
 * <a href="{@docRoot}/overview-summary.html#scattervshash">
 * important differences between hash and scatter sets</a>.
 * </p>
 * 
 * @see KTypeScatterSet
 * @see <a href="{@docRoot}/overview-summary.html#interfaces">HPPC interfaces diagram</a>
 */
/*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeHashSet<KType>
  extends AbstractKTypeCollection<KType> 
  implements /*! #if ($templateonly) !*/ Intrinsics.KeyHasher<KType>, /*! #end !*/
             KTypeLookupContainer<KType>, 
             KTypeSet<KType>,
             Preallocable,
             Cloneable {
  /** The hash array holding keys. */
  public /*! #if ($TemplateOptions.KTypeGeneric) !*/ 
                   Object [] 
         /*! #else KType [] #end !*/ 
                   keys;

  /**
   * The number of stored keys (assigned key slots), excluding the special 
   * "empty" key, if any.
   * 
   * @see #size()
   * @see #hasEmptyKey
   */
  protected int assigned;

  /**
   * Mask for slot scans in {@link #keys}.
   */
  protected int mask;

  /**
   * We perturb hash values with a container-unique
   * seed to avoid problems with nearly-sorted-by-hash 
   * values on iterations.
   * 
   * @see #hashKey
   * @see "http://issues.carrot2.org/browse/HPPC-80"
   * @see "http://issues.carrot2.org/browse/HPPC-103"
   */
  protected int keyMixer;

  /**
   * Expand (rehash) {@link #keys} when {@link #assigned} hits this value. 
   */
  protected int resizeAt;

  /**
   * Special treatment for the "empty slot" key marker.
   */
  protected boolean hasEmptyKey;

  /**
   * The load factor for {@link #keys}.
   */
  protected double loadFactor;

  /** 
   * Per-instance hash order mixing strategy.
   * @see #keyMixer
   */
  protected HashOrderMixingStrategy orderMixer;

  /**
   * New instance with sane defaults.
   * 
   * @see #KTypeHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeHashSet() {
    this(DEFAULT_EXPECTED_ELEMENTS, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   * 
   * @see #KTypeHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeHashSet(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   * 
   * @see #KTypeHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeHashSet(int expectedElements, double loadFactor) {
    this(expectedElements, loadFactor, HashOrderMixing.defaultStrategy());
  }

  /**
   * New instance with the provided defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause a rehash (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   * @param orderMixer
   *          Hash key order mixing strategy. See {@link HashOrderMixing} for predefined
   *          implementations. Use constant mixers only if you understand the potential
   *          consequences.
   */
  public KTypeHashSet(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

  /**
   * New instance copying elements from another {@link KTypeContainer}.
   */
  public KTypeHashSet(KTypeContainer<? extends KType> container) {
    this(container.size());
    addAll(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(KType key) {
    if (Intrinsics.isEmpty(key)) {
      assert Intrinsics.isEmpty(keys[mask + 1]);
      boolean added = !hasEmptyKey;
      hasEmptyKey = true;
      return added;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      
      KType existing;
      while (!Intrinsics.isEmpty(existing = keys[slot])) {
        if (Intrinsics.equals(this, key, existing)) {
          return false;
        }
        slot = (slot + 1) & mask;
      }

      if (assigned == resizeAt) {
        allocateThenInsertThenRehash(slot, key);
      } else {
        keys[slot] = key;
      }
  
      assigned++;
      return true;
    }
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
   * {@inheritDoc}
   */
  @Override
  /*! #if ($TemplateOptions.KTypePrimitive) 
  public KType [] toArray() {
      #else !*/
  public Object[] toArray() {
  /*! #end !*/
    final KType[] cloned = Intrinsics.<KType> newArray(size());
    int j = 0;
    if (hasEmptyKey) {
      cloned[j++] = Intrinsics.empty();
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0, max = mask; slot <= max; slot++) {
      KType existing;
      if (!Intrinsics.isEmpty(existing = keys[slot])) {
        cloned[j++] = existing;
      }
    }

    return cloned;
  }

  /**
   * An alias for the (preferred) {@link #removeAll}.
   */
  public boolean remove(KType key) {
    if (Intrinsics.isEmpty(key)) {
      boolean hadEmptyKey = hasEmptyKey;
      hasEmptyKey = false;
      return hadEmptyKey;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      
      KType existing;
      while (!Intrinsics.isEmpty(existing = keys[slot])) {
        if (Intrinsics.equals(this, key, existing)) {
          shiftConflictingKeys(slot);
          return true;
        }
        slot = (slot + 1) & mask;
      }
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KType key) {
    return remove(key) ? 1 : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypePredicate<? super KType> predicate) {
    int before = size();

    if (hasEmptyKey) {
      if (predicate.apply(Intrinsics.<KType> empty())) {
        hasEmptyKey = false;
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0, max = this.mask; slot <= max;) {
      KType existing;
      if (!Intrinsics.isEmpty(existing = keys[slot])) {
        if (predicate.apply(existing)) {
          shiftConflictingKeys(slot);
          continue; // Repeat the check for the same slot i (shifted).
        }
      }
      slot++;
    }

    return before - size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(KType key) {
    if (Intrinsics.isEmpty(key)) {
      return hasEmptyKey;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      KType existing;
      while (!Intrinsics.isEmpty(existing = keys[slot])) {
        if (Intrinsics.equals(this, key, existing)) {
          return true;
        }
        slot = (slot + 1) & mask;
      }
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    assigned = 0;
    hasEmptyKey = false;
    Arrays.fill(keys, Intrinsics.<KType> empty());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
    assigned = 0;
    hasEmptyKey = false;
    keys = null;
    ensureCapacity(Containers.DEFAULT_EXPECTED_ELEMENTS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Ensure this container can hold at least the
   * given number of elements without resizing its buffers.
   * 
   * @param expectedElements The total number of elements, inclusive.
   */
  @Override
  public void ensureCapacity(int expectedElements) {
    if (expectedElements > resizeAt || keys == null) {
      final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
      allocateBuffers(minBufferSize(expectedElements, loadFactor));
      if (prevKeys != null && !isEmpty()) {
        rehash(prevKeys);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return assigned + (hasEmptyKey ? 1 : 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int h = hasEmptyKey ? 0xDEADBEEF : 0;
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = mask; slot >= 0; slot--) {
      KType existing;
      if (!Intrinsics.isEmpty(existing = keys[slot])) {
        h += BitMixer.mix(existing);
      }
    }
    return h;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    return obj != null &&
           getClass() == obj.getClass() &&
           sameKeys(getClass().cast(obj));
  }

  /**
   * Return true if all keys of some other container exist in this container.
#if ($TemplateOptions.KTypeGeneric) 
     * Equality comparison is performed with this object's {@link #equals(Object, Object)} 
     * method.
#end 
   */
  private boolean sameKeys(KTypeSet<?> other) {
    if (other.size() != size()) {
      return false;
    }

    for (KTypeCursor<?> c : other) {
      if (!contains(Intrinsics.<KType> cast(c.value))) {
        return false;
      }
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public KTypeHashSet<KType> clone() {
    try {
      /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
      KTypeHashSet<KType> cloned = (KTypeHashSet<KType>) super.clone();
      cloned.keys = keys.clone();
      cloned.hasEmptyKey = cloned.hasEmptyKey;
      cloned.orderMixer = orderMixer.clone();
      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<KTypeCursor<KType>> iterator() {
    return new EntryIterator();
  }

  /**
   * An iterator implementation for {@link #iterator}.
   */
  protected final class EntryIterator extends AbstractIterator<KTypeCursor<KType>> {
    private final KTypeCursor<KType> cursor;
    private final int max = mask + 1;
    private int slot = -1;

    public EntryIterator() {
      cursor = new KTypeCursor<KType>();
    }

    @Override
    protected KTypeCursor<KType> fetch() {
      if (slot < max) {
        KType existing;
        for (slot++; slot < max; slot++) {
          if (!Intrinsics.isEmpty(existing = Intrinsics.<KType> cast(keys[slot]))) {
            cursor.index = slot;
            cursor.value = existing;
            return cursor;
          }
        }
      }

      if (slot == max && hasEmptyKey) {
        cursor.index = slot;
        cursor.value = Intrinsics.empty();
        slot++;
        return cursor;
      }

      return done();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends KTypeProcedure<? super KType>> T forEach(T procedure) {
    if (hasEmptyKey) {
      procedure.apply(Intrinsics.<KType> empty());
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0, max = this.mask; slot <= max; slot++) {
      KType existing;
      if (!Intrinsics.isEmpty(existing = keys[slot])) {
        procedure.apply(existing);
      }
    }

    return procedure;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends KTypePredicate<? super KType>> T forEach(T predicate) {
    if (hasEmptyKey) {
      if (!predicate.apply(Intrinsics.<KType> empty())) {
        return predicate;
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0, max = this.mask; slot <= max; slot++) {
      KType existing;
      if (!Intrinsics.isEmpty(existing = keys[slot])) {
        if (!predicate.apply(existing)) {
          break;
        }
      }
    }

    return predicate;
  }

  /**
   * Create a set from a variable number of arguments or an array of
   * <code>KType</code>. The elements are copied from the argument to the
   * internal buffer.
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  @SafeVarargs
  /* #end */
  public static <KType> KTypeHashSet<KType> from(KType... elements) {
    final KTypeHashSet<KType> set = new KTypeHashSet<KType>(elements.length);
    set.addAll(elements);
    return set;
  }

  /**
   * Returns a hash code for the given key.
   * 
   * The default implementation mixes the hash of the key with {@link #keyMixer}
   * to differentiate hash order of keys between hash containers. Helps
   * alleviate problems resulting from linear conflict resolution in open
   * addressing.
   * 
   * The output from this function should evenly distribute keys across the
   * entire integer range.
   */
  /*! #if ($templateonly) !*/
  @Override
  public
  /*! #else protected #end !*/
  int hashKey(KType key) {
    assert !Intrinsics.isEmpty(key); // Handled as a special case (empty slot marker).
    return BitMixer.mix(key, this.keyMixer);
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
    final int mask = this.mask;
    if (Intrinsics.<KType> isEmpty(key)) {
      return hasEmptyKey ? mask + 1 : ~(mask + 1);
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
          return slot;
        }
        slot = (slot + 1) & mask;
      }

      return ~slot;
    }
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
    assert index < 0 || 
    (index >= 0 && index <= mask) ||
    (index == mask + 1 && hasEmptyKey);

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
    assert index >= 0 : "The index must point at an existing key.";
    assert index <= mask ||
           (index == mask + 1 && hasEmptyKey);

    return Intrinsics.<KType> cast(keys[index]);
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
    assert index >= 0 : "The index must point at an existing key.";
    assert index <= mask ||
           (index == mask + 1 && hasEmptyKey);
    assert Intrinsics.equals(this, keys[index], equivalentKey);

    KType previousValue = Intrinsics.<KType> cast(keys[index]);
    keys[index] = equivalentKey;
    return previousValue;
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
    if (Intrinsics.isEmpty(key)) {
      assert index == mask + 1;
      assert Intrinsics.isEmpty(keys[index]);
      hasEmptyKey = true;
    } else {
      assert Intrinsics.isEmpty(keys[index]);

      if (assigned == resizeAt) {
        allocateThenInsertThenRehash(index, key);
      } else {
        keys[index] = key;
      }

      assigned++;
    }
  }

  @Override
  public String visualizeKeyDistribution(int characters) {
    return KTypeBufferVisualizer.visualizeKeyDistribution(keys, mask, characters);
  }

  /**
   * Validate load factor range and return it. Override and suppress if you need
   * insane load factors.
   */
  protected double verifyLoadFactor(double loadFactor) {
    checkLoadFactor(loadFactor, MIN_LOAD_FACTOR, MAX_LOAD_FACTOR);
    return loadFactor;
  }

  /**
   * Rehash from old buffers to new buffers. 
   */
  protected void rehash(KType[] fromKeys) {
    assert HashContainers.checkPowerOfTwo(fromKeys.length - 1);

    // Rehash all stored keys into the new buffers.
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final int mask = this.mask;
    KType existing;
    for (int i = fromKeys.length - 1; --i >= 0;) {
      if (!Intrinsics.isEmpty(existing = fromKeys[i])) {
        int slot = hashKey(existing) & mask;
        while (!Intrinsics.isEmpty(keys[slot])) {
          slot = (slot + 1) & mask;
        }
        keys[slot] = existing;
      }
    }
  }

  /**
   * Allocate new internal buffers. This method attempts to allocate
   * and assign internal buffers atomically (either allocations succeed or not).
   */
  protected void allocateBuffers(int arraySize) {
    assert Integer.bitCount(arraySize) == 1;

    // Compute new hash mixer candidate before expanding.
    final int newKeyMixer = this.orderMixer.newKeyMixer(arraySize);

    // Ensure no change is done if we hit an OOM.
    KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
    try {
      int emptyElementSlot = 1;
      this.keys = Intrinsics.<KType> newArray(arraySize + emptyElementSlot);
    } catch (OutOfMemoryError e) {
      this.keys = prevKeys;
      throw new BufferAllocationException(
          "Not enough memory to allocate buffers for rehashing: %,d -> %,d", 
          e,
          this.keys == null ? 0 : size(), 
          arraySize);
    }

    this.resizeAt = expandAtCount(arraySize, loadFactor);
    this.keyMixer = newKeyMixer;
    this.mask = arraySize - 1;
  }

  /**
   * This method is invoked when there is a new key to be inserted into
   * the buffer but there is not enough empty slots to do so.
   * 
   * New buffers are allocated. If this succeeds, we know we can proceed
   * with rehashing so we assign the pending element to the previous buffer
   * (possibly violating the invariant of having at least one empty slot)
   * and rehash all keys, substituting new buffers at the end.  
   */
  protected void allocateThenInsertThenRehash(int slot, KType pendingKey) {
    assert assigned == resizeAt 
           && Intrinsics.isEmpty(Intrinsics.<KType> cast(keys[slot]))
           && !Intrinsics.isEmpty(pendingKey);

    // Try to allocate new buffers first. If we OOM, we leave in a consistent state.
    final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
    allocateBuffers(nextBufferSize(mask + 1, size(), loadFactor));
    assert this.keys.length > prevKeys.length;

    // We have succeeded at allocating new data so insert the pending key/value at
    // the free slot in the old arrays before rehashing.
    prevKeys[slot] = pendingKey;

    // Rehash old keys, including the pending key.
    rehash(prevKeys);
  }

  /**
   * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
   */
  protected void shiftConflictingKeys(int gapSlot) {
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final int mask = this.mask;

    // Perform shifts of conflicting keys to fill in the gap.
    int distance = 0;
    while (true) {
      final int slot = (gapSlot + (++distance)) & mask;
      final KType existing = keys[slot];
      if (Intrinsics.isEmpty(existing)) {
        break;
      }

      final int idealSlot = hashKey(existing);
      final int shift = (slot - idealSlot) & mask;
      if (shift >= distance) {
        // Entry at this position was originally at or before the gap slot.
        // Move the conflict-shifted entry to the gap's position and repeat the procedure
        // for any entries to the right of the current position, treating it
        // as the new gap.
        keys[gapSlot] = existing;
        gapSlot = slot;
        distance = 0;
      }
    }

    // Mark the last found gap slot without a conflict as empty.
    keys[gapSlot] = Intrinsics.empty();
    assigned--;
  }
}