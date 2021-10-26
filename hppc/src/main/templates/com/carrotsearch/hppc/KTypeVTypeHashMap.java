/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.cursors.KTypeVTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.predicates.KTypeVTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;
import com.carrotsearch.hppc.procedures.KTypeVTypeProcedure;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.carrotsearch.hppc.Containers.DEFAULT_EXPECTED_ELEMENTS;
import static com.carrotsearch.hppc.HashContainers.*;

/**
 * A hash map of <code>KType</code> to <code>VType</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * @see <a href="{@docRoot}/overview-summary.html#interfaces">HPPC interfaces diagram</a>
 */
/*! #if ($TemplateOptions.anyGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeHashMap<KType, VType>
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
         Object []
         /*! #else KType [] #end !*/
         keys;

  /**
   * The array holding values.
   */
  public /*! #if ($TemplateOptions.VTypeGeneric) !*/
         Object []
         /*! #else VType [] #end !*/
         values;

  /**
   * This lock is used to make operations on the map thread safe,
   * when {@link #concurrent} is true.
   */
  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Whether to make map operations thread safe or not.
   */
  protected boolean concurrent;

  /**
   * The number of stored keys (assigned key slots), excluding the special
   * "empty" key, if any (use {@link #size()} instead).
   *
   * @see #size()
   */
  protected int assigned;

  /**
   * Mask for slot scans in {@link #keys}.
   */
  protected int mask;

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
   * Seed used to ensure the hash iteration order is different from an iteration to another.
   */
  protected int iterationSeed;

  /**
   * New instance with sane defaults.
   */
  public KTypeVTypeHashMap() {
    this(DEFAULT_EXPECTED_ELEMENTS);
  }

  /**
   * New instance with sane defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   */
  public KTypeVTypeHashMap(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with the provided defaults.
   *
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause a rehash (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   */
  public KTypeVTypeHashMap(int expectedElements, double loadFactor) {
    this(expectedElements, loadFactor, false);
  }

  /**
   * New instance with the provided defaults.
   *
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause a rehash (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   * @param concurrent
   *          Whether to make map operations thread safe or not. Also see {@link #lock}.
   */
  public KTypeVTypeHashMap(int expectedElements, double loadFactor, boolean concurrent) {
    this.loadFactor = verifyLoadFactor(loadFactor);
    this.concurrent = concurrent;
    iterationSeed = HashContainers.nextIterationSeed();
    ensureCapacity(expectedElements);
  }

  /**
   * Create a hash map from all key-value pairs of another container.
   */
  public KTypeVTypeHashMap(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
    this(container.size());
    putAll(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType put(KType key, VType value) {
    if (concurrent) {
      lock.writeLock().lock();
    }

    try {
      assert assigned < mask + 1;

      final int mask = this.mask;
      if (Intrinsics.<KType>isEmpty(key)) {
        hasEmptyKey = true;
        VType previousValue = Intrinsics.<VType>cast(values[mask + 1]);
        values[mask + 1] = value;
        return previousValue;
      } else {
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        int slot = hashKey(key) & mask;

        KType existing;
        while (!Intrinsics.<KType>isEmpty(existing = keys[slot])) {
          if (Intrinsics.<KType>equals(this, key, existing)) {
            final VType previousValue = Intrinsics.<VType>cast(values[slot]);
            values[slot] = value;
            return previousValue;
          }
          slot = (slot + 1) & mask;
        }

        if (assigned == resizeAt) {
          allocateThenInsertThenRehash(slot, key, value);
        } else {
          keys[slot] = key;
          values[slot] = value;
        }

        assigned++;
        return Intrinsics.<VType>empty();
      }
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int putAll(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
    if (concurrent) {
      lock.writeLock().lock();
    }

    try {
      final int count = size();
      for (KTypeVTypeCursor<? extends KType, ? extends VType> c : container) {
        put(c.key, c.value);
      }
      return size() - count;
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * Puts all key/value pairs from a given iterable into this map.
   */
  @Override
  public int putAll(Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable){
    final int count = size();
    for (KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable) {
      put(c.key, c.value);
    }
    return size() - count;
  }

  /**
   * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
   * of the following code:
   * <pre>
   * if (!map.containsKey(key)) map.put(value);
   * </pre>
   * 
   * @param key The key of the value to check.
   * @param value The value to put if <code>key</code> does not exist.
   * @return <code>true</code> if <code>key</code> did not exist and <code>value</code>
   * was placed in the map.
   */
  public boolean putIfAbsent(KType key, VType value) {
    int keyIndex = indexOf(key);
    if (!indexExists(keyIndex)) {
      indexInsert(keyIndex, key, value);
      return true;
    } else {
      return false;
    }
  }

  /*! #if ($TemplateOptions.VTypePrimitive) !*/
  /**
   * If <code>key</code> exists, <code>putValue</code> is inserted into the map,
   * otherwise any existing value is incremented by <code>additionValue</code>.
   * 
   * @param key
   *          The key of the value to adjust.
   * @param putValue
   *          The value to put if <code>key</code> does not exist.
   * @param incrementValue
   *          The value to add to the existing value if <code>key</code> exists.
   * @return Returns the current value associated with <code>key</code> (after
   *         changes).
   */
  @Override
  public VType putOrAdd(KType key, VType putValue, VType incrementValue) {
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      assert assigned < mask + 1;

      int keyIndex = indexOf(key);
      if (indexExists(keyIndex)) {
        putValue = Intrinsics.<VType>add(Intrinsics.<VType>cast(values[keyIndex]), incrementValue);
        indexReplace(keyIndex, putValue);
      } else {
        indexInsert(keyIndex, key, putValue);
      }
      return putValue;
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }
  /*! #end !*/

  /*! #if ($TemplateOptions.VTypePrimitive) !*/ 
  /**
   * Adds <code>incrementValue</code> to any existing value for the given <code>key</code>
   * or inserts <code>incrementValue</code> if <code>key</code> did not previously exist.
   * 
   * @param key The key of the value to adjust.
   * @param incrementValue The value to put or add to the existing value if <code>key</code> exists.
   * @return Returns the current value associated with <code>key</code> (after changes).
   */
  @Override
  public VType addTo(KType key, VType incrementValue)
  {
    return putOrAdd(key, incrementValue, incrementValue);
  }
  /*! #end !*/

  /**
   * {@inheritDoc}
   */
  @Override
  public VType remove(KType key) {
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      final int mask = this.mask;
      if (Intrinsics.<KType>isEmpty(key)) {
        hasEmptyKey = false;
        VType previousValue = Intrinsics.<VType>cast(values[mask + 1]);
        values[mask + 1] = Intrinsics.<VType>empty();
        return previousValue;
      } else {
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        int slot = hashKey(key) & mask;

        KType existing;
        while (!Intrinsics.<KType>isEmpty(existing = keys[slot])) {
          if (Intrinsics.<KType>equals(this, key, existing)) {
            final VType previousValue = Intrinsics.<VType>cast(values[slot]);
            shiftConflictingKeys(slot);
            return previousValue;
          }
          slot = (slot + 1) & mask;
        }

        return Intrinsics.<VType>empty();
      }
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypeContainer<? super KType> other) {
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      final int before = size();

      // Try to iterate over the smaller set of values or
      // over the container that isn't implementing
      // efficient contains() lookup.

      if (other.size() >= size() &&
          other instanceof KTypeLookupContainer<?>) {
        if (hasEmptyKey && other.contains(Intrinsics.<KType>empty())) {
          hasEmptyKey = false;
          values[mask + 1] = Intrinsics.<VType>empty();
        }

        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        for (int slot = 0, max = this.mask; slot <= max; ) {
          KType existing;
          if (!Intrinsics.<KType>isEmpty(existing = keys[slot]) && other.contains(existing)) {
            // Shift, do not increment slot.
            shiftConflictingKeys(slot);
          } else {
            slot++;
          }
        }
      } else {
        for (KTypeCursor<?> c : other) {
          remove(Intrinsics.<KType>cast(c.value));
        }
      }

      return before - size();
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypeVTypePredicate<? super KType, ? super VType> predicate) {
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      final int before = size();

      final int mask = this.mask;

      if (hasEmptyKey) {
        if (predicate.apply(Intrinsics.<KType>empty(), Intrinsics.<VType>cast(values[mask + 1]))) {
          hasEmptyKey = false;
          values[mask + 1] = Intrinsics.<VType>empty();
        }
      }

      final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
      final VType[] values = Intrinsics.<VType[]>cast(this.values);
      for (int slot = 0; slot <= mask; ) {
        KType existing;
        if (!Intrinsics.<KType>isEmpty(existing = keys[slot]) &&
            predicate.apply(existing, values[slot])) {
          // Shift, do not increment slot.
          shiftConflictingKeys(slot);
        } else {
          slot++;
        }
      }

      return before - size();
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypePredicate<? super KType> predicate) {
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      final int before = size();

      if (hasEmptyKey) {
        if (predicate.apply(Intrinsics.<KType>empty())) {
          hasEmptyKey = false;
          values[mask + 1] = Intrinsics.<VType>empty();
        }
      }

      final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
      for (int slot = 0, max = this.mask; slot <= max; ) {
        KType existing;
        if (!Intrinsics.<KType>isEmpty(existing = keys[slot]) &&
            predicate.apply(existing)) {
          // Shift, do not increment slot.
          shiftConflictingKeys(slot);
        } else {
          slot++;
        }
      }

      return before - size();
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType get(KType key) {
    if (concurrent) {
      lock.readLock().lock();
    }
    try {
      if (Intrinsics.<KType>isEmpty(key)) {
        return hasEmptyKey ? Intrinsics.<VType>cast(values[mask + 1]) : Intrinsics.<VType>empty();
      } else {
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        final int mask = this.mask;
        int slot = hashKey(key) & mask;

        KType existing;
        while (!Intrinsics.<KType>isEmpty(existing = keys[slot])) {
          if (Intrinsics.<KType>equals(this, key, existing)) {
            return Intrinsics.<VType>cast(values[slot]);
          }
          slot = (slot + 1) & mask;
        }

        return Intrinsics.<VType>empty();
      }
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType getOrDefault(KType key, VType defaultValue) {
    if (concurrent) {
      lock.readLock().lock();
    }
    try {
      if (Intrinsics.<KType>isEmpty(key)) {
        return hasEmptyKey ? Intrinsics.<VType>cast(values[mask + 1]) : defaultValue;
      } else {
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        final int mask = this.mask;
        int slot = hashKey(key) & mask;

        KType existing;
        while (!Intrinsics.<KType>isEmpty(existing = keys[slot])) {
          if (Intrinsics.<KType>equals(this, key, existing)) {
            return Intrinsics.<VType>cast(values[slot]);
          }
          slot = (slot + 1) & mask;
        }

        return defaultValue;
      }
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(KType key) {
    if (Intrinsics.<KType> isEmpty(key)) {
      return hasEmptyKey;
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
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
  public int indexOf(KType key) {
    if (concurrent) {
      lock.readLock().lock();
    }
    try {
      final int mask = this.mask;
      if (Intrinsics.<KType>isEmpty(key)) {
        return hasEmptyKey ? mask + 1 : ~(mask + 1);
      } else {
        final KType[] keys = Intrinsics.<KType[]>cast(this.keys);
        int slot = hashKey(key) & mask;

        KType existing;
        while (!Intrinsics.<KType>isEmpty(existing = keys[slot])) {
          if (Intrinsics.<KType>equals(this, key, existing)) {
            return slot;
          }
          slot = (slot + 1) & mask;
        }

        return ~slot;
      }
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean indexExists(int index) {
    if (concurrent) {
      lock.readLock().lock();
    }
    try {
      assert index < 0 ||
          (index >= 0 && index <= mask) ||
          (index == mask + 1 && hasEmptyKey);

      return index >= 0;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType indexGet(int index) {
    assert index >= 0 : "The index must point at an existing key.";
    if (concurrent) {
      lock.readLock().lock();
    }
    try {
      assert index <= mask ||
          (index == mask + 1 && hasEmptyKey);

      return Intrinsics.<VType>cast(values[index]);
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType indexReplace(int index, VType newValue) {
    assert index >= 0 : "The index must point at an existing key.";
    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      assert index <= mask ||
          (index == mask + 1 && hasEmptyKey);

      VType previousValue = Intrinsics.<VType>cast(values[index]);
      values[index] = newValue;
      return previousValue;
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void indexInsert(int index, KType key, VType value) {
    assert index < 0 : "The index must not point at an existing key.";

    index = ~index;

    if (concurrent) {
      lock.writeLock().lock();
    }
    try {
      if (Intrinsics.<KType>isEmpty(key)) {
        assert index == mask + 1;
        values[index] = value;
        hasEmptyKey = true;
      } else {
        assert Intrinsics.<KType>isEmpty(keys[index]);

        if (assigned == resizeAt) {
          allocateThenInsertThenRehash(index, key, value);
        } else {
          keys[index] = key;
          values[index] = value;
        }

        assigned++;
      }
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType indexRemove(int index) {
    assert index >= 0 : "The index must point at an existing key.";

    if (concurrent) {
      lock.writeLock().lock();
    }
    try {

      assert index <= mask ||
          (index == mask + 1 && hasEmptyKey);

      VType previousValue = Intrinsics.<VType>cast(values[index]);
      if (index > mask) {
        hasEmptyKey = false;
        values[index] = Intrinsics.<VType>empty();
      } else {
        shiftConflictingKeys(index);
      }
      return previousValue;
    } finally {
      if (concurrent) {
        lock.writeLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    if (concurrent) {
      lock.writeLock().lock();
    }
    assigned = 0;
    hasEmptyKey = false;

    Arrays.fill(keys, Intrinsics.<KType> empty());

    /* #if ($TemplateOptions.VTypeGeneric) */ 
    Arrays.fill(values, Intrinsics.<VType> empty());
    /* #end */

    if (concurrent) {
      lock.writeLock().unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
    assigned = 0;
    hasEmptyKey = false;

    if (concurrent) {
      lock.writeLock().lock();
    }
    keys = null;
    values = null;
    ensureCapacity(Containers.DEFAULT_EXPECTED_ELEMENTS);
    if (concurrent) {
      lock.writeLock().unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    if (concurrent) {
      lock.readLock().lock();
    }

    try {
      return assigned + (hasEmptyKey ? 1 : 0);
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEmpty() {
    if (concurrent) {
      lock.readLock().lock();
    }

    try {
      return size() == 0;
    } finally {
      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int h = hasEmptyKey ? 0xDEADBEEF : 0;
    for (KTypeVTypeCursor<KType, VType> c : this) {
      h += BitMixer.mix(c.key) +
           BitMixer.mix(c.value);
    }
    return h;
  }

  /**
   * {@inheritDoc} 
   */
  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (
      obj != null &&
      getClass() == obj.getClass() &&
      equalElements(getClass().cast(obj))
    );
  }

  /**
   * Return true if all keys of some other container exist in this container.
#if ($TemplateOptions.KTypeGeneric) 
   * Equality comparison is performed with this object's {@link #equals(Object, Object)} 
   * method.
#end 
#if ($TemplateOptions.VTypeGeneric) 
   * Values are compared using {@link Objects#equals(Object)} method.
#end 
   */
  protected boolean equalElements(KTypeVTypeHashMap<?, ?> other) {
    if (other.size() != size()) {
      return false;
    }

    for (KTypeVTypeCursor<?, ?> c : other) {
      KType key = Intrinsics.<KType> cast(c.key);
      if (!containsKey(key) ||
          !Intrinsics.<VType> equals(c.value, get(key))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Ensure this container can hold at least the
   * given number of keys (entries) without resizing its buffers.
   * <p>
   * If {@link #concurrent} is true, invoke this from a write locked context.
   *
   * @param expectedElements The total number of keys, inclusive.
   */
  @Override
  public void ensureCapacity(int expectedElements) {
    if (expectedElements > resizeAt || keys == null) {
      final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
      final VType[] prevValues = Intrinsics.<VType[]> cast(this.values);
      allocateBuffers(minBufferSize(expectedElements, loadFactor));
      if (prevKeys != null && !isEmpty()) {
        rehash(prevKeys, prevValues);
      }
    }
  }

  @Override
  public long ramBytesAllocated() {
    // int: iterationSeed, assigned, mask, resizeAt
    // double: loadFactor
    // boolean: hasEmptyKey
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + 4 * Integer.BYTES + Double.BYTES + 1 +
            RamUsageEstimator.shallowSizeOfArray(keys) + RamUsageEstimator.shallowSizeOfArray(values);
  }

  @Override
  public long ramBytesUsed() {
    // int: iterationSeed, assigned, mask, resizeAt
    // double: loadFactor
    // boolean: hasEmptyKey
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + 4 * Integer.BYTES + Double.BYTES + 1 +
            RamUsageEstimator.shallowUsedSizeOfArray(keys, size()) +
            RamUsageEstimator.shallowUsedSizeOfArray(values, size());
  }

  /**
   * Provides the next iteration seed used to build the iteration starting slot and offset increment.
   * This method does not need to be synchronized, what matters is that each thread gets a sequence of varying seeds.
   */
  protected int nextIterationSeed() {
    return iterationSeed = BitMixer.mixPhi(iterationSeed);
  }

  /**
   * Captures a snapshot of keys, values, and mask. If {@link #concurrent} is true,
   * then this snapshot is captured with a read lock.
   *
   * This provides a consistent view of the map, albeit with eventual consistency
   * in the sense that if values are updated, or if keys are removed during an iteration,
   * they may or may not reflect during the iteration. If the map is rehased during an iteration,
   * the iteration will continue, but will refer to the last set of keys and values before rehashing.
   */
  private final class StateSnapshot {
    private final /*! #if ($TemplateOptions.KTypeGeneric) !*/
        Object []
        /*! #else KType [] #end !*/
        keys;

    private final  /*! #if ($TemplateOptions.VTypeGeneric) !*/
        Object []
        /*! #else VType [] #end !*/
        values;

    private final int mask;
    private final int seed;

    public StateSnapshot() {
      if (concurrent) {
        lock.readLock().lock();
      }
      this.seed = nextIterationSeed();
      this.mask = KTypeVTypeHashMap.this.mask;
      this.keys = Intrinsics.<KType[]> cast(KTypeVTypeHashMap.this.keys);
      this.values = Intrinsics.<VType[]> cast(KTypeVTypeHashMap.this.values);

      if (concurrent) {
        lock.readLock().unlock();
      }
    }
  }

  /**
   * An iterator implementation for {@link #iterator}.
   */
  private final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>> {
    private final KTypeVTypeCursor<KType, VType> cursor;
    private final int increment;
    private int index;
    private int slot;

    private final StateSnapshot state;

    public EntryIterator() {
      cursor = new KTypeVTypeCursor<KType, VType>();
      state = new StateSnapshot();

      if (concurrent) {
        lock.readLock().unlock();
      }

      increment = iterationIncrement(state.seed);
      slot = state.seed & state.mask;
    }

    @Override
    protected KTypeVTypeCursor<KType, VType> fetch() {
      while (index <= state.mask) {
        KType existing;
        index++;
        slot = (slot + increment) & state.mask;
        if (!Intrinsics.<KType>isEmpty(existing = Intrinsics.<KType>cast(state.keys[slot]))) {
          cursor.index = slot;
          cursor.key = existing;
          cursor.value = Intrinsics.<VType>cast(state.values[slot]);
          return cursor;
        }
      }

      if (index == state.mask + 1 && hasEmptyKey) {
        cursor.index = index;
        cursor.key = Intrinsics.<KType> empty();
        cursor.value = Intrinsics.<VType> cast(state.values[index++]);
        return cursor;
      }

      return done();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<KTypeVTypeCursor<KType, VType>> iterator() {
      return new EntryIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(T procedure) {
    if (concurrent) {
      lock.readLock().lock();
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);

    final int seed = nextIterationSeed();
    final int mask = this.mask;

    if (concurrent) {
      lock.readLock().unlock();
    }

    if (hasEmptyKey) {
      procedure.apply(Intrinsics.<KType> empty(), Intrinsics.<VType> cast(values[mask + 1]));
    }

    int inc = iterationIncrement(seed);
    for (int i = 0, slot = seed & mask; i <= mask; i++, slot = (slot + inc) & mask) {
      if (!Intrinsics.<KType> isEmpty(keys[slot])) {
        procedure.apply(keys[slot], values[slot]);
      }
    }

    return procedure;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends KTypeVTypePredicate<? super KType, ? super VType>> T forEach(T predicate) {
    if (concurrent) {
      lock.readLock().lock();
    }
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);
    final int mask = this.mask;
    final int seed = nextIterationSeed();

    if (concurrent) {
      lock.readLock().unlock();
    }

    if (hasEmptyKey) {
      if (!predicate.apply(Intrinsics.<KType> empty(), Intrinsics.<VType> cast(values[mask + 1]))) {
        return predicate;
      }
    }

    int inc = iterationIncrement(seed);
    for (int i = 0, slot = seed & mask; i <= mask; i++, slot = (slot + inc) & mask) {
      if (!Intrinsics.<KType> isEmpty(keys[slot])) {
        if (!predicate.apply(keys[slot], values[slot])) {
          break;
        }
      }
    }

    return predicate;
  }

  /**
   * Returns a specialized view of the keys of this associated container. The
   * view additionally implements {@link ObjectLookupContainer}.
   */
  public KeysContainer keys() {
    return new KeysContainer();
  }

  /**
   * A view of the keys inside this hash map.
   */
  public final class KeysContainer extends AbstractKTypeCollection<KType> 
                                   implements KTypeLookupContainer<KType> {
    private final KTypeVTypeHashMap<KType, VType> owner = KTypeVTypeHashMap.this;

    @Override
    public boolean contains(KType e) {
      return owner.containsKey(e);
    }

    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure) {
      owner.forEach((KTypeVTypeProcedure<KType, VType>) (k, v) -> procedure.apply(k));
      return procedure;
    }

    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate) {
      owner.forEach((KTypeVTypePredicate<KType, VType>) (key, value) -> predicate.apply(key));
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
      if (owner.containsKey(e)) {
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
    private final int increment;
    private int index;
    private int slot;

    private final StateSnapshot state;

    public KeysIterator() {
      cursor = new KTypeCursor<KType>();
      state = new StateSnapshot();
      increment = iterationIncrement(state.seed);
      slot = state.seed & state.mask;
    }

    @Override
    protected KTypeCursor<KType> fetch() {
      final int mask = state.mask;
      while (index <= mask) {
        KType existing;
        index++;
        slot = (slot + increment) & mask;
        if (!Intrinsics.<KType>isEmpty(existing = Intrinsics.<KType>cast(state.keys[slot]))) {
          cursor.index = slot;
          cursor.value = existing;
          return cursor;
        }
      }

      if (index == mask + 1 && hasEmptyKey) {
        cursor.index = index++;
        cursor.value = Intrinsics.<KType> empty();
        return cursor;
      }

      return done();
    }
  }

  /**
   * @return Returns a container with all values stored in this map.
   */
  @Override
  public KTypeCollection<VType> values() {
    return new ValuesContainer();
  }

  /**
   * A view over the set of values of this map.
   */
  private final class ValuesContainer extends AbstractKTypeCollection<VType> {
    private final KTypeVTypeHashMap<KType, VType> owner = KTypeVTypeHashMap.this;

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
      return owner.removeAll((key, value) -> Intrinsics.<VType> equals(e, value));
    }

    @Override
    public int removeAll(final KTypePredicate<? super VType> predicate) {
      return owner.removeAll((key, value) -> predicate.apply(value));
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
    private final int increment;
    private int index;
    private int slot;
    private final StateSnapshot state;

    public ValuesIterator() {
      cursor = new KTypeCursor<VType>();
      state = new StateSnapshot();
      increment = iterationIncrement(state.seed);
      slot = state.seed & state.mask;
    }

    @Override
    protected KTypeCursor<VType> fetch() {
      final int mask = state.mask;
      while (index <= mask) {
        index++;
        slot = (slot + increment) & mask;
        if (!Intrinsics.<KType> isEmpty(Intrinsics.<KType> cast(state.keys[slot]))) {
          cursor.index = slot;
          cursor.value = Intrinsics.<VType> cast(state.values[slot]);
          return cursor;
        }
      }

      if (index == mask + 1 && hasEmptyKey) {
        cursor.index = index;
        cursor.value = Intrinsics.<VType> cast(state.values[index++]);
        return cursor;
      }

      return done();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public KTypeVTypeHashMap<KType, VType> clone() {
    try {
      /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
      KTypeVTypeHashMap<KType, VType> cloned = (KTypeVTypeHashMap<KType, VType>) super.clone();
      cloned.keys = keys.clone();
      cloned.values = values.clone();
      cloned.hasEmptyKey = hasEmptyKey;
      cloned.iterationSeed = HashContainers.nextIterationSeed();
      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Convert the contents of this map to a human-friendly string.
   */
  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("[");

    boolean first = true;
    for (KTypeVTypeCursor<KType, VType> cursor : this) {
      if (!first) {
        buffer.append(", ");
      }
      buffer.append(cursor.key);
      buffer.append("=>");
      buffer.append(cursor.value);
      first = false;
    }
    buffer.append("]");
    return buffer.toString();
  }

  @Override
  public String visualizeKeyDistribution(int characters) {
    return KTypeBufferVisualizer.visualizeKeyDistribution(keys, mask, characters);
  }

  /**
   * Creates a hash map from two index-aligned arrays of key-value pairs.
   */
  public static <KType, VType> KTypeVTypeHashMap<KType, VType> from(KType[] keys, VType[] values) {
    if (keys.length != values.length) {
      throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
    }

    KTypeVTypeHashMap<KType, VType> map = new KTypeVTypeHashMap<>(keys.length);
    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }

    return map;
  }
    
  /**
   * Returns a hash code for the given key.
   *
   * <p>The output from this function should evenly distribute keys across the
   * entire integer range.</p>
   */
  /*! #if ($templateonly) !*/
  @Override
  public
  /*! #else protected #end !*/
  int hashKey(KType key) {
    assert !Intrinsics.<KType> isEmpty(key); // Handled as a special case (empty slot marker).
    return BitMixer.mixPhi(key);
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
   *
   * If {@link #concurrent} is true, invoke this from a write locked context.
   */
  protected void rehash(KType[] fromKeys, VType[] fromValues) {
    assert fromKeys.length == fromValues.length &&
           HashContainers.checkPowerOfTwo(fromKeys.length - 1);

    // Rehash all stored key/value pairs into the new buffers.
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);
    final int mask = this.mask;
    KType existing;

    // Copy the zero element's slot, then rehash everything else.
    int from = fromKeys.length - 1;
    keys[keys.length - 1] = fromKeys[from];
    values[values.length - 1] = fromValues[from];
    while (--from >= 0) {
      if (!Intrinsics.<KType> isEmpty(existing = fromKeys[from])) {
        int slot = hashKey(existing) & mask;
        while (!Intrinsics.<KType> isEmpty(keys[slot])) {
          slot = (slot + 1) & mask;
        }
        keys[slot] = existing;
        values[slot] = fromValues[from];
      }
    }
  }

  /**
   * Allocate new internal buffers. This method attempts to allocate
   * and assign internal buffers atomically (either allocations succeed or not).
   */
  protected void allocateBuffers(int arraySize) {
    assert Integer.bitCount(arraySize) == 1;

    // Ensure no change is done if we hit an OOM.
    KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
    VType[] prevValues = Intrinsics.<VType[]> cast(this.values);
    try {
      int emptyElementSlot = 1;
      this.keys = Intrinsics.<KType> newArray(arraySize + emptyElementSlot);
      this.values = Intrinsics.<VType> newArray(arraySize + emptyElementSlot);
    } catch (OutOfMemoryError e) {
      this.keys = prevKeys;
      this.values = prevValues;
      throw new BufferAllocationException(
          "Not enough memory to allocate buffers for rehashing: %,d -> %,d", 
          e,
          this.mask + 1, 
          arraySize);
    }

    this.resizeAt = expandAtCount(arraySize, loadFactor);
    this.mask = arraySize - 1;
  }

  /**
   * This method is invoked when there is a new key/ value pair to be inserted into
   * the buffers but there is not enough empty slots to do so.
   * 
   * New buffers are allocated. If this succeeds, we know we can proceed
   * with rehashing so we assign the pending element to the previous buffer
   * (possibly violating the invariant of having at least one empty slot)
   * and rehash all keys, substituting new buffers at the end.
   *
   * If {@link #concurrent} is set to true, invoke this from a write locked context.
   */
  protected void allocateThenInsertThenRehash(int slot, KType pendingKey, VType pendingValue) {
    assert assigned == resizeAt
           && Intrinsics.<KType> isEmpty(Intrinsics.<KType> cast(keys[slot]))
           && !Intrinsics.<KType> isEmpty(pendingKey);

    // Try to allocate new buffers first. If we OOM, we leave in a consistent state.
    final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] prevValues = Intrinsics.<VType[]> cast(this.values);
    allocateBuffers(nextBufferSize(mask + 1, size(), loadFactor));
    assert this.keys.length > prevKeys.length;

    // We have succeeded at allocating new data so insert the pending key/value at
    // the free slot in the old arrays before rehashing.
    prevKeys[slot] = pendingKey;
    prevValues[slot] = pendingValue;

    // Rehash old keys, including the pending key.
    rehash(prevKeys, prevValues);
  }
  
  /**
   * Shift all the slot-conflicting keys and values allocated to 
   * (and including) <code>slot</code>.
   *
   * If {@link #concurrent} is set to true, invoke this from a write locked context.
   */
  protected void shiftConflictingKeys(int gapSlot) {
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);
    final int mask = this.mask;

    // Perform shifts of conflicting keys to fill in the gap.
    int distance = 0;
    while (true) {
      final int slot = (gapSlot + (++distance)) & mask;
      final KType existing = keys[slot];
      if (Intrinsics.<KType> isEmpty(existing)) {
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
        values[gapSlot] = values[slot]; 
        gapSlot = slot;
        distance = 0;
      }
    }

    // Mark the last found gap slot without a conflict as empty.
    keys[gapSlot] = Intrinsics.<KType> empty();
    values[gapSlot] = Intrinsics.<VType> empty();
    assigned--;
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/
  /*! #if ($templateonly) !*/
  @Override
  public
  /*! #else protected #end !*/ boolean equals(Object v1, Object v2) {
    return (v1 == v2) || (v1 != null && v1.equals(v2));
  }
  /*! #end !*/    
}
