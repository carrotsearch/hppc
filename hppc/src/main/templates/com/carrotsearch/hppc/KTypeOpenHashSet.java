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
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenHashSet<KType>
  extends AbstractKTypeCollection<KType> 
  implements KTypeLookupContainer<KType>, 
             KTypeSet<KType>, 
             Cloneable {
  /** The array holding keys. */
  public KType [] keys;

  /**
   * Information if an entry (slot) in the {@link #keys} table is allocated * or empty.
   */
  // NOCOMMIT: http://issues.carrot2.org/browse/HPPC-97
  public boolean[] allocated;

  /**
   * The number of stored keys (assigned key slots).
   * @see #size()
   */
  protected int assigned;

  /**
   * Mask for index scans in {@link #keys}.
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
  public KTypeOpenHashSet(KTypeContainer<KType> container) {
    this(container.size());
    addAll(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(KType e) {
    final int mask = this.mask;

    int slot = hashKey(e) & mask;
    while (allocated[slot]) {
      if (Intrinsics.equalsKType(e, keys[slot])) {
        return false;
      }
      slot = (slot + 1) & mask;
    }

    if (assigned == resizeAt) {
      allocateThenInsertThenRehash(slot, e);
    } else {
      allocated[slot] = true;
      keys[slot] = e;
    }

    assigned++;
    return true;
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
  public KType[] toArray() {
    final KType[] asArray = Intrinsics.newKTypeArray(assigned);
    for (int i = 0, j = 0; i < keys.length; i++) {
      if (allocated[i]) {
        asArray[j++] = keys[i];
      }
    }
    return asArray;
  }

  /**
   * An alias for the (preferred) {@link #removeAllOccurrences}.
   */
  public boolean remove(KType key) {
    final int mask = this.mask;
    int slot = hashKey(key) & mask;
    while (allocated[slot]) {
      if (Intrinsics.equalsKType(key, keys[slot])) {
        shiftConflictingKeys(slot);
        assigned--;
        return true;
      }
      slot = (slot + 1) & mask;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAllOccurrences(KType key) {
    return remove(key) ? 1 : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
  public int removeAll(KTypePredicate<? super KType> predicate) {
    final KType[] keys = this.keys;
    final boolean[] allocated = this.allocated;

    int before = size();
    for (int i = 0; i < allocated.length;) {
      if (allocated[i]) {
        if (predicate.apply(Intrinsics.<KType> cast(keys[i]))) {
          shiftConflictingKeys(i);
          assigned--;
          continue; // Repeat the check for the same index i (shifted).
        }
      }
      i++;
    }

    return before - size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(KType key) {
    final int mask = allocated.length - 1;
    int slot = hashKey(key) & mask;
    while (allocated[slot]) {
      if (Intrinsics.equalsKType(key, keys[slot])) {
        return true;
      }
      slot = (slot + 1) & mask;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    assigned = 0;

    Arrays.fill(allocated, false);
    Arrays.fill(keys, Intrinsics.<KType> defaultKTypeValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return assigned == 0;
  }

  /**
   * Ensure the set can store the given number of elements (total, not
   * in addition to any currently stored elements) without resizing.
   */
  public void ensureCapacity(int expectedElements) {
    if (expectedElements > resizeAt || keys == null) {
      final KType[] prevKeys = this.keys;
      final boolean[] prevAllocated = this.allocated;
      allocateBuffers(minBufferSize(expectedElements, loadFactor));
      if (prevKeys != null && !isEmpty()) {
        rehash(prevKeys, prevAllocated);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return assigned;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int h = 0;
    final KType[] keys = this.keys;
    final boolean[] allocated = this.allocated;
    for (int i = allocated.length; --i >= 0;) {
      if (allocated[i]) {
        h += Internals.rehash(keys[i]);
      }
    }
    return h;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  /*! #if ($TemplateOptions.KTypeGeneric) !*/
  @SuppressWarnings("unchecked")
  /*! #end !*/
  public boolean equals(Object obj) {
    if (obj != null &&
        obj instanceof KTypeSet<?>) {
      KTypeSet<Object> other = (KTypeSet<Object>) obj;
      if (other.size() == this.size()) {
        for (KTypeCursor<KType> c : this) {
          if (!other.contains(c.value)) {
            return false;
          }
        }
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  /*! #if ($TemplateOptions.KTypeGeneric) !*/
  @SuppressWarnings("unchecked")
  /*! #end !*/
  public KTypeOpenHashSet<KType> clone() {
    try {
      KTypeOpenHashSet<KType> cloned = (KTypeOpenHashSet<KType>) super.clone();
      cloned.keys = keys.clone();
      cloned.allocated = allocated.clone();
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

    public EntryIterator() {
      cursor = new KTypeCursor<KType>();
      cursor.index = -1;
    }

    /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
    @Override
    protected KTypeCursor<KType> fetch() {
      final int max = keys.length;

      int i = cursor.index + 1;
      while (i < keys.length && !allocated[i]) {
        i++;
      }

      if (i == max) {
        return done();
      }

      cursor.index = i;
      cursor.value = Intrinsics.<KType> cast(keys[i]);
      return cursor;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
  public <T extends KTypeProcedure<? super KType>> T forEach(T procedure) {
    final KType[] keys = this.keys;
    final boolean[] allocated = this.allocated;

    for (int i = 0; i < allocated.length; i++) {
      if (allocated[i]) {
        procedure.apply(Intrinsics.<KType> cast(keys[i]));
      }
    }

    return procedure;
  }

  /**
   * {@inheritDoc}
   */
  @Override /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
  public <T extends KTypePredicate<? super KType>> T forEach(T predicate) {
    final KType[] keys = this.keys;
    final boolean[] states = this.allocated;

    for (int i = 0; i < states.length; i++) {
      if (states[i]) {
        if (!predicate.apply(Intrinsics.<KType> cast(keys[i])))
          break;
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
   * Validate load factor range and return it. Override and suppress if you need
   * insane load factors.
   */
  protected double verifyLoadFactor(double loadFactor) {
    checkLoadFactor(loadFactor, MIN_LOAD_FACTOR, MAX_LOAD_FACTOR);
    return loadFactor;
  }

  /**
   * Mix the hash of a given key with {@link #keyMixer} to differentiate hash
   * order of keys between hash containers. Helps alleviate problems resulting
   * from linear conflict resolution in open addressing.
   */
  protected int hashKey(KType key) {
    return Internals.rehash(key, this.keyMixer);
  }

  /**
   * Rehash from old buffers to new buffers. 
   */
  /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
  protected void rehash(KType[] fromKeys, boolean[] fromAllocated) {
    // Rehash all stored keys into the new buffers.
    final KType[] keys = this.keys;
    final boolean[] allocated = this.allocated;
    final int mask = this.mask;
    for (int i = fromAllocated.length; --i >= 0;) {
      if (fromAllocated[i]) {
        final KType k = Intrinsics.<KType> cast(fromKeys[i]);

        int slot = hashKey(k) & mask;
        while (allocated[slot]) {
          slot = (slot + 1) & mask;
        }
  
        allocated[slot] = true;
        keys[slot] = k;
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
    KType[] prevKeys = this.keys;
    boolean[] prevAllocated = this.allocated;
    try {
      this.keys = Intrinsics.newKTypeArray(arraySize);
      this.allocated = new boolean[arraySize];
    } catch (OutOfMemoryError e) {
      this.keys = prevKeys;
      this.allocated = prevAllocated;
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
    assert assigned == resizeAt && !allocated[slot];

    // Try to allocate new buffers first. If we OOM, we leave in a consistent state.
    final KType[] prevKeys = this.keys;
    final boolean[] prevAllocated = this.allocated;
    allocateBuffers(nextBufferSize(keys.length, assigned, loadFactor));
    assert this.keys.length > prevKeys.length;

    // We have succeeded at allocating new data so insert the pending key/value at
    // the free slot in the old arrays before rehashing.
    prevAllocated[slot] = true;
    prevKeys[slot] = pendingKey;

    // Rehash old keys, including the pending key.
    rehash(prevKeys, prevAllocated);
  }

  /**
   * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
   */
  /*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
  protected void shiftConflictingKeys(int slot) {
    final KType [] keys = this.keys;
    final int mask = this.mask;
    int slotPrev, slotOther;
    while (true) {
      slotPrev = slot;
      slot = (slot + 1) & mask;

      while (allocated[slot]) {
        slotOther = hashKey(Intrinsics.<KType> cast(keys[slot])) & mask;
        if (slotPrev <= slot) {
          // We are on the right of the original slot.
          if (slotPrev >= slotOther || slotOther > slot) {
            break;
          }
        } else {
          // We have wrapped around.
          if (slotPrev >= slotOther && slotOther > slot) {
            break;
          }
        }
        slot = (slot + 1) & mask;
      }

      if (!allocated[slot]) {
        break;
      }

      // Shift key/value pair.
      keys[slotPrev] = keys[slot];
    }

    allocated[slotPrev] = false;
    /* #if ($TemplateOptions.KTypeGeneric) */
    keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue();
    /* #end */
  }
}
