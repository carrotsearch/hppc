package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.HashContainers.*;
import static com.carrotsearch.hppc.Containers.*;

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
public class KTypeVTypeHashMap<KType, VType>
  implements /*! #if ($templateonly) !*/ Intrinsics.EqualityFunction, /*! #end !*/
             /*! #if ($templateonly) !*/ Intrinsics.KeyHasher<KType>, /*! #end !*/
             KTypeVTypeMap<KType, VType>,
             Preallocable,
             Cloneable
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
   * Per-instance hash order mixing strategy.
   * @see #keyMixer
   */
  protected HashOrderMixingStrategy orderMixer;

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
   * New instance with sane defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   */
  public KTypeVTypeHashMap(int expectedElements, double loadFactor) {
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
  public KTypeVTypeHashMap(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
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
    assert assigned < mask + 1;

    final int mask = this.mask;
    if (Intrinsics.<KType> isEmpty(key)) {
      hasEmptyKey = true;
      VType previousValue = Intrinsics.<VType> cast(values[mask + 1]);
      values[mask + 1] = value;
      return previousValue;
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
          final VType previousValue = Intrinsics.<VType> cast(values[slot]);
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
      return Intrinsics.<VType> empty();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int putAll(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
    final int count = size();
    for (KTypeVTypeCursor<? extends KType, ? extends VType> c : container) {
      put(c.key, c.value);
    }
    return size() - count;
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
    if (!containsKey(key)) {
      put(key, value);
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
    assert assigned < mask + 1;

    if (containsKey(key)) {
      putValue = get(key);
      putValue = (VType) (Intrinsics.<VType> add(putValue, incrementValue));
    }

    put(key, putValue);
    return putValue;
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
    final int mask = this.mask;
    if (Intrinsics.<KType> isEmpty(key)) {
      hasEmptyKey = false;
      VType previousValue = Intrinsics.<VType> cast(values[mask + 1]);
      values[mask + 1] = Intrinsics.<VType> empty();
      return previousValue;
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
          final VType previousValue = Intrinsics.<VType> cast(values[slot]);
          shiftConflictingKeys(slot);
          return previousValue;
        }
        slot = (slot + 1) & mask;
      }

      return Intrinsics.<VType> empty();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypeContainer<? super KType> other) {
    final int before = size();

    // Try to iterate over the smaller set of values or
    // over the container that isn't implementing 
    // efficient contains() lookup.

    if (other.size() >= size() &&
        other instanceof KTypeLookupContainer<?>) {
      if (hasEmptyKey) {
        if (other.contains(Intrinsics.<KType> empty())) {
          hasEmptyKey = false;
          values[mask + 1] = Intrinsics.<VType> empty();
        }
      }

      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      for (int slot = 0, max = this.mask; slot <= max;) {
        KType existing;
        if (!Intrinsics.<KType> isEmpty(existing = keys[slot]) && other.contains(existing)) {
          // Shift, do not increment slot.
          shiftConflictingKeys(slot);
        } else {
          slot++;
        }
      }
    } else {
      for (KTypeCursor<?> c : other) {
        this.remove(Intrinsics.<KType> cast(c.value));
      }
    }

    return before - size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypeVTypePredicate<? super KType, ? super VType> predicate) {
    final int before = size();

    final int mask = this.mask;

    if (hasEmptyKey) {
      if (predicate.apply(Intrinsics.<KType> empty(), Intrinsics.<VType> cast(values[mask + 1]))) {
        hasEmptyKey = false;
        values[mask + 1] = Intrinsics.<VType> empty();
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);
    for (int slot = 0; slot <= mask;) {
      KType existing;
      if (!Intrinsics.<KType> isEmpty(existing = keys[slot]) && 
          predicate.apply(existing, values[slot])) {
        // Shift, do not increment slot.
        shiftConflictingKeys(slot);
      } else {
        slot++;
      }
    }

    return before - size();    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int removeAll(KTypePredicate<? super KType> predicate) {
    final int before = size();

    if (hasEmptyKey) {
      if (predicate.apply(Intrinsics.<KType> empty())) {
        hasEmptyKey = false;
        values[mask + 1] = Intrinsics.<VType> empty();
      }
    }

    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    for (int slot = 0, max = this.mask; slot <= max;) {
      KType existing;
      if (!Intrinsics.<KType> isEmpty(existing = keys[slot]) &&
          predicate.apply(existing)) {
        // Shift, do not increment slot.
        shiftConflictingKeys(slot);
      } else {
        slot++;
      }
    }

    return before - size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType get(KType key) {
    if (Intrinsics.<KType> isEmpty(key)) {
      return hasEmptyKey ? Intrinsics.<VType> cast(values[mask + 1]) : Intrinsics.<VType> empty();
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
          return Intrinsics.<VType> cast(values[slot]);
        }
        slot = (slot + 1) & mask;
      }

      return Intrinsics.<VType> empty();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType getOrDefault(KType key, VType defaultValue) {
    if (Intrinsics.<KType> isEmpty(key)) {
      return hasEmptyKey ? Intrinsics.<VType> cast(values[mask + 1]) : defaultValue;
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.<KType> isEmpty(existing = keys[slot])) {
        if (Intrinsics.<KType> equals(this, key, existing)) {
          return Intrinsics.<VType> cast(values[slot]);
        }
        slot = (slot + 1) & mask;
      }

      return defaultValue;
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
   * {@inheritDoc}
   */
  @Override
  public boolean indexExists(int index) {
    assert index < 0 || 
           (index >= 0 && index <= mask) ||
           (index == mask + 1 && hasEmptyKey);

    return index >= 0; 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType indexGet(int index) {
    assert index >= 0 : "The index must point at an existing key.";
    assert index <= mask ||
           (index == mask + 1 && hasEmptyKey);

    return Intrinsics.<VType> cast(values[index]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType indexReplace(int index, VType newValue) {
    assert index >= 0 : "The index must point at an existing key.";
    assert index <= mask ||
           (index == mask + 1 && hasEmptyKey);

    VType previousValue = Intrinsics.<VType> cast(values[index]);
    values[index] = newValue;
    return previousValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void indexInsert(int index, KType key, VType value) {
    assert index < 0 : "The index must not point at an existing key.";

    index = ~index;
    if (Intrinsics.<KType> isEmpty(key)) {
      assert index == mask + 1;
      values[index] = value;
      hasEmptyKey = true;
    } else {
      assert Intrinsics.<KType> isEmpty(keys[index]);

      if (assigned == resizeAt) {
        allocateThenInsertThenRehash(index, key, value);
      } else {
        keys[index] = key;
        values[index] = value;
      }

      assigned++;
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

    /* #if ($TemplateOptions.VTypeGeneric) */ 
    Arrays.fill(values, Intrinsics.<VType> empty());
    /* #end */
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
    assigned = 0;
    hasEmptyKey = false;

    keys = null;
    values = null;
    ensureCapacity(Containers.DEFAULT_EXPECTED_ELEMENTS);
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
  public boolean isEmpty() {
    return size() == 0;
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
    return obj != null &&
           getClass() == obj.getClass() &&
           equalElements(getClass().cast(obj));
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

  /**
   * An iterator implementation for {@link #iterator}.
   */
  private final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>> {
    private final KTypeVTypeCursor<KType, VType> cursor;
    private final int max = mask + 1;
    private int slot = -1;

    public EntryIterator() {
      cursor = new KTypeVTypeCursor<KType, VType>();
    }

    @Override
    protected KTypeVTypeCursor<KType, VType> fetch() {
      if (slot < max) {
        KType existing;
        for (slot++; slot < max; slot++) {
          if (!Intrinsics.<KType> isEmpty(existing = Intrinsics.<KType> cast(keys[slot]))) {
            cursor.index = slot;
            cursor.key = existing;
            cursor.value = Intrinsics.<VType> cast(values[slot]);
            return cursor;
          }
        }
      }

      if (slot == max && hasEmptyKey) {
        cursor.index = slot;
        cursor.key = Intrinsics.<KType> empty();
        cursor.value = Intrinsics.<VType> cast(values[max]);
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
  public Iterator<KTypeVTypeCursor<KType, VType>> iterator() {
      return new EntryIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(T procedure) {
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);

    if (hasEmptyKey) {
      procedure.apply(Intrinsics.<KType> empty(), Intrinsics.<VType> cast(values[mask + 1]));
    }

    for (int slot = 0, max = this.mask; slot <= max; slot++) {
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
    final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
    final VType[] values = Intrinsics.<VType[]> cast(this.values);

    if (hasEmptyKey) {
      if (!predicate.apply(Intrinsics.<KType> empty(), Intrinsics.<VType> cast(values[mask + 1]))) {
        return predicate;
      }
    }

    for (int slot = 0, max = this.mask; slot <= max; slot++) {
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
    private final int max = mask + 1;
    private int slot = -1;

    public KeysIterator() {
      cursor = new KTypeCursor<KType>();
    }

    @Override
    protected KTypeCursor<KType> fetch() {
      if (slot < max) {
        KType existing;
        for (slot++; slot < max; slot++) {
          if (!Intrinsics.<KType> isEmpty(existing = Intrinsics.<KType> cast(keys[slot]))) {
            cursor.index = slot;
            cursor.value = existing;
            return cursor;
          }
        }
      }

      if (slot == max && hasEmptyKey) {
        cursor.index = slot;
        cursor.value = Intrinsics.<KType> empty();
        slot++;
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
      return owner.removeAll(new KTypeVTypePredicate<KType, VType>() {
        @Override
        public boolean apply(KType key, VType value) {
          return Intrinsics.<VType> equals(e, value);
        }
      });
    }

    @Override
    public int removeAll(final KTypePredicate<? super VType> predicate) {
      return owner.removeAll(new KTypeVTypePredicate<KType, VType>() {
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
    private final int max = mask + 1;
    private int slot = -1;

    public ValuesIterator() {
      cursor = new KTypeCursor<VType>();
    }

    @Override
    protected KTypeCursor<VType> fetch() {
      if (slot < max) {
        for (slot++; slot < max; slot++) {
          if (!Intrinsics.<KType> isEmpty(Intrinsics.<KType> cast(keys[slot]))) {
            cursor.index = slot;
            cursor.value = Intrinsics.<VType> cast(values[slot]);
            return cursor;
          }
        }
      }

      if (slot == max && hasEmptyKey) {
        cursor.index = slot;
        cursor.value = Intrinsics.<VType> cast(values[max]);
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
  public KTypeVTypeHashMap<KType, VType> clone() {
    try {
      /* #if ($templateOnly) */ @SuppressWarnings("unchecked") /* #end */
      KTypeVTypeHashMap<KType, VType> cloned = (KTypeVTypeHashMap<KType, VType>) super.clone();
      cloned.keys = keys.clone();
      cloned.values = values.clone();
      cloned.hasEmptyKey = cloned.hasEmptyKey;
      cloned.orderMixer = orderMixer.clone();
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
   * <p>The default implementation mixes the hash of the key with {@link #keyMixer}
   * to differentiate hash order of keys between hash containers. Helps
   * alleviate problems resulting from linear conflict resolution in open
   * addressing.</p>
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

    // Compute new hash mixer candidate before expanding.
    final int newKeyMixer = this.orderMixer.newKeyMixer(arraySize);

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
    this.keyMixer = newKeyMixer;
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
