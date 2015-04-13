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
 */
/*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenHashSet<KType>
  extends AbstractKTypeCollection<KType> 
  implements KTypeLookupContainer<KType>, 
             KTypeSet<KType>, 
             Cloneable {

  protected static final 
      /*! #if ($TemplateOptions.KTypeGeneric) !*/ Object /*! #else KType #end !*/
          EMPTY_KEY =
      /*! #if ($TemplateOptions.KTypeGeneric) !*/ null   /*! #else 0     #end !*/;

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
   * @see #KTypeOpenHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeOpenHashSet() {
    this(DEFAULT_EXPECTED_ELEMENTS, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   * 
   * @see #KTypeOpenHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeOpenHashSet(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   * 
   * @see #KTypeOpenHashSet(int, double, HashOrderMixingStrategy)
   */
  public KTypeOpenHashSet(int expectedElements, double loadFactor) {
    this(expectedElements, loadFactor, HashOrderMixing.randomized());
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
  public KTypeOpenHashSet(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

  /**
   * New instance copying elements from another {@link KTypeContainer}.
   */
  public KTypeOpenHashSet(KTypeContainer<? extends KType> container) {
    this(container.size());
    addAll(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(KType key) {
    if (Intrinsics.isEmptyKey(key)) {
      boolean hadEmptyKey = hasEmptyKey;
      hasEmptyKey = true;
      return hadEmptyKey;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      
      KType existing;
      while (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        if (Intrinsics.equalsKType(key, existing)) {
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
      cloned[j++] = Intrinsics.<KType> cast(EMPTY_KEY);
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0; slot < keys.length; slot++) {
      KType existing;
      if (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        cloned[j++] = existing;
      }
    }

    return cloned;
  }

  /**
   * An alias for the (preferred) {@link #removeAll(KType)}.
   */
  public boolean remove(KType key) {
    if (Intrinsics.isEmptyKey(key)) {
      boolean hadEmptyKey = hasEmptyKey;
      hasEmptyKey = false;
      return hadEmptyKey;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      
      KType existing;
      while (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        if (Intrinsics.equalsKType(key, existing)) {
          shiftConflictingKeys(slot);
          assigned--;
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
      if (predicate.apply(Intrinsics.<KType> cast(EMPTY_KEY))) {
        hasEmptyKey = false;
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0; slot < keys.length;) {
      KType existing;
      if (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        if (predicate.apply(existing)) {
          shiftConflictingKeys(slot);
          assigned--;
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
    if (Intrinsics.isEmptyKey(key)) {
      return hasEmptyKey;
    } else {
      final KType [] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;
      KType existing;
      while (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        if (Intrinsics.equalsKType(key, existing)) {
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
    Arrays.fill(keys, Intrinsics.<KType> cast(EMPTY_KEY));
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
    for (int slot = keys.length; --slot >= 0;) {
      KType existing;
      if (!Intrinsics.isEmptyKey(existing = keys[slot])) {
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
     * Equality comparison is performed with this object's {@link #sameKeys} 
     * method.
#end 
   */
  private boolean sameKeys(KTypeSet<?> other) {
    if (other.size() != size()) {
      return false;
    }

    Iterator<? extends KTypeCursor<?>> i = other.iterator();
    while (i.hasNext()) {
      KTypeCursor<?> c = i.next();
      KType key = Intrinsics.<KType> cast(c.value);
      if (contains(key)) {
        continue;
      }
      return false;
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public KTypeOpenHashSet<KType> clone() {
    try {
      /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
      KTypeOpenHashSet<KType> cloned = (KTypeOpenHashSet<KType>) super.clone();
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
    private final int max = keys.length;
    private int slot = -1;

    public EntryIterator() {
      cursor = new KTypeCursor<KType>();
    }

    @Override
    protected KTypeCursor<KType> fetch() {
      if (slot < max) {
        KType existing;
        for (slot++; slot < max; slot++) {
          if (!Intrinsics.isEmptyKey(existing = Intrinsics.<KType> cast(keys[slot]))) {
            cursor.index = slot;
            cursor.value = existing;
            return cursor;
          }
        }
      }

      if (slot == max && hasEmptyKey) {
        cursor.index = slot;
        cursor.value = Intrinsics.<KType> cast(EMPTY_KEY);
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
      procedure.apply(Intrinsics.<KType> cast(EMPTY_KEY));
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0; slot < keys.length;) {
      KType existing;
      if (!Intrinsics.isEmptyKey(existing = keys[slot])) {
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
      if (!predicate.apply(Intrinsics.<KType> cast(EMPTY_KEY))) {
        return predicate;
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0; slot < keys.length;) {
      KType existing;
      if (!Intrinsics.isEmptyKey(existing = keys[slot])) {
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
  public static <KType> KTypeOpenHashSet<KType> from(KType... elements) {
    final KTypeOpenHashSet<KType> set = new KTypeOpenHashSet<KType>(elements.length);
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
  protected int hashKey(KType key) {
    assert !Intrinsics.isEmptyKey(key); // Handled as a special case (empty slot marker).
    return BitMixer.mix(key, this.keyMixer);
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
    // Rehash all stored keys into the new buffers.
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final int mask = this.mask;
    KType existing;
    for (int i = fromKeys.length; --i >= 0;) {
      if (!Intrinsics.isEmptyKey(existing = fromKeys[i])) {
        int slot = hashKey(existing) & mask;
        while (!Intrinsics.isEmptyKey(keys[slot])) {
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
      this.keys = Intrinsics.<KType> newArray(arraySize);
    } catch (OutOfMemoryError e) {
      this.keys = prevKeys;
      throw new BufferAllocationException(
          "Not enough memory to allocate buffers for rehashing: %,d -> %,d", 
          e,
          this.keys == null ? 0 : this.keys.length, 
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
           && Intrinsics.isEmptyKey(Intrinsics.<KType> cast(keys[slot]))
           && !Intrinsics.isEmptyKey(pendingKey);

    // Try to allocate new buffers first. If we OOM, we leave in a consistent state.
    final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
    allocateBuffers(nextBufferSize(keys.length, assigned, loadFactor));
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
      if (Intrinsics.isEmptyKey(existing)) {
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
    keys[gapSlot] = Intrinsics.<KType> cast(EMPTY_KEY);
  }
}