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
 */
/*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeOpenHashMap<KType, VType>
  implements KTypeVTypeMap<KType, VType>, 
             Cloneable
{
  protected static final 
  /*! #if ($TemplateOptions.KTypeGeneric) !*/ Object /*! #else KType #end !*/
      EMPTY_KEY =
  /*! #if ($TemplateOptions.KTypeGeneric) !*/ null   /*! #else 0     #end !*/;

  /** The hash array holding keys. */
  public /*! #if ($TemplateOptions.KTypeGeneric) !*/ 
         Object [] 
         /*! #else KType [] #end !*/ 
         keys;

  public /*! #if ($TemplateOptions.VTypeGeneric) !*/ 
         Object [] 
         /*! #else VType [] #end !*/ 
         values;
  
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
   * Value associated with the "empty" key.
   * 
   * @see #hasEmptyKey
   */
  protected VType emptyKeyValue;
  
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
  public KTypeVTypeOpenHashMap() {
    this(DEFAULT_EXPECTED_ELEMENTS);
  }

  /**
   * New instance with sane defaults.
   */
  public KTypeVTypeOpenHashMap(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   */
  public KTypeVTypeOpenHashMap(int expectedElements, double loadFactor) {
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
  public KTypeVTypeOpenHashMap(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

  /**
   * Create a hash map from all key-value pairs of another container.
   */
  public KTypeVTypeOpenHashMap(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
    this(container.size());
    putAll(container);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VType put(KType key, VType value) {
    assert assigned < keys.length;

    if (Intrinsics.isEmptyKey(key)) {
      boolean hadEmptyKey = hasEmptyKey;
      VType previousValue = (hadEmptyKey ? emptyKeyValue : Intrinsics.<VType> defaultVTypeValue());
      hasEmptyKey = true;
      emptyKeyValue = value;
      return previousValue;
    } else {
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      final int mask = this.mask;
      int slot = hashKey(key) & mask;

      KType existing;
      while (!Intrinsics.isEmptyKey(existing = keys[slot])) {
        if (Intrinsics.equalsKType(key, existing)) {
          final VType oldValue = Intrinsics.<VType> cast(values[slot]);
          values[slot] = value;
          return oldValue;
        }
        slot = (slot + 1) & mask;
      }

      if (assigned == resizeAt) {
        allocateThenInsertThenRehash(slot, key, value);
      } else {
        keys[slot] = key;
      }

      assigned++;
      return Intrinsics.<VType> defaultVTypeValue();
    }
  }

  
  
  
    /**
     * {@inheritDoc}
     */
    @Override
    public int putAll(
        KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container)
    {
        final int count = this.assigned;
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : container)
        {
            put(c.key, c.value);
        }
        return this.assigned - count;
    }

    /**
     * Puts all key/value pairs from a given iterable into this map.
     */
    @Override
    public int putAll(
        Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>> iterable)
    {
        final int count = this.assigned;
        for (KTypeVTypeCursor<? extends KType, ? extends VType> c : iterable)
        {
            put(c.key, c.value);
        }
        return this.assigned - count;
    }

    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
     * of the following code:
     * <pre>
     * if (!map.containsKey(key)) map.put(value);
     * </pre>
     * 
     * <p>This method saves to {@link #lastSlot} as a side effect of each call.</p>
     * 
     * @param key The key of the value to check.
     * @param value The value to put if <code>key</code> does not exist.
     * @return <code>true</code> if <code>key</code> did not exist and <code>value</code>
     * was placed in the map.
     */
    public boolean putIfAbsent(KType key, VType value)
    {
        if (!containsKey(key))
        {
            put(key, value);
            return true;
        }
        return false;
    }

    /*! #if ($TemplateOptions.VTypePrimitive) !*/ 
    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. A logical 
     * equivalent of the following code (but does not update {@link #lastSlot}):
     * <pre>
     *  if (containsKey(key))
     *  {
     *      VType v = (VType) (lget() + additionValue);
     *      lset(v);
     *      return v;
     *  }
     *  else
     *  {
     *     put(key, putValue);
     *     return putValue;
     *  }
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param putValue The value to put if <code>key</code> does not exist.
     * @param additionValue The value to add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    /*! #end !*/
    /*! #if ($TemplateOptions.VTypePrimitive) 
    public VType putOrAdd(KType key, VType putValue, VType additionValue)
    {
        assert assigned < allocated.length;

        final int mask = allocated.length - 1;
        int slot = BitMixer.mix0(key, perturbation) & mask;
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
            {
                return values[slot] = (VType) (values[slot] + additionValue);
            }

            slot = (slot + 1) & mask;
        }

        if (assigned == resizeAt) {
            expandAndPut(key, putValue, slot);
        } else {
            assigned++;
            allocated[slot] = true;
            keys[slot] = key;                
            values[slot] = putValue;
        }
        return putValue;
    }
    #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive) !*/ 
    /**
     * An equivalent of calling
     * <pre>
     *  if (containsKey(key))
     *  {
     *      VType v = (VType) (lget() + additionValue);
     *      lset(v);
     *      return v;
     *  }
     *  else
     *  {
     *     put(key, additionValue);
     *     return additionValue;
     *  }
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param additionValue The value to put or add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
    /*! #end !*/
    /*! #if ($TemplateOptions.VTypePrimitive) 
    public VType addTo(KType key, VType additionValue)
    {
        return putOrAdd(key, additionValue, additionValue);
    }
    #end !*/

    /**
     * Expand the internal storage buffers and rehash.
     */
    private void expandAndPut(KType pendingKey, VType pendingValue, int freeSlot)
    {
        assert assigned == resizeAt;
        assert !allocated[freeSlot];

        // Try to allocate new buffers first to leave the map in a consistent
        // state in case of OOMs.
        final KType   [] oldKeys      = keys_();
        final VType   [] oldValues    = values_();
        final boolean [] oldAllocated = this.allocated;
        final double loadFactor = getLoadFactor();
        allocateBuffers(nextBufferSize(keys.length, assigned, loadFactor), loadFactor);
        assert this.keys.length > oldKeys.length;

        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.
        lastSlot = -1;
        assigned++;
        oldAllocated[freeSlot] = true;
        oldKeys[freeSlot] = pendingKey;
        oldValues[freeSlot] = pendingValue;
        
        // Rehash all stored keys into the new buffers.
        final KType []   keys = keys_();
        final VType []   values = values_();
        final boolean [] allocated = this.allocated;
        final int mask = allocated.length - 1;
        for (int i = oldAllocated.length; --i >= 0;)
        {
            if (oldAllocated[i])
            {
                final KType k = oldKeys[i];
                final VType v = oldValues[i];

                int slot = BitMixer.mix0(k, perturbation) & mask;
                while (allocated[slot])
                {
                    slot = (slot + 1) & mask;
                }

                allocated[slot] = true;
                keys[slot] = k;                
                values[slot] = v;
            }
        }

        /* #if ($TemplateOptions.KTypeGeneric) */ Arrays.fill(oldKeys,   null); /* #end */
        /* #if ($TemplateOptions.VTypeGeneric) */ Arrays.fill(oldValues, null); /* #end */
    }

    /**
     * Allocate internal buffers and thresholds to ensure they can hold 
     * the given number of elements.
     */
    protected void allocateBuffers(int arraySize, double loadFactor)
    {
        // Ensure no change is done if we hit an OOM.
        final int newPerturbation = computePerturbationValue(arraySize);
        KType [] keys = keys_();
        VType [] values = values_();
        boolean [] allocated = this.allocated;
        try {
          this.keys = Intrinsics.<KType> newArray(arraySize);
          this.values = Intrinsics.<VType> newArray(arraySize);
          this.allocated = new boolean [arraySize];
        } catch (OutOfMemoryError e) {
          this.keys = keys;
          this.values = values;
          this.allocated = allocated;
          throw new BufferAllocationException("Not enough memory.", e);
        }

        this.resizeAt = expandAtCount(arraySize, loadFactor);
        this.perturbation = newPerturbation;
    }

    /**
     * <p>Compute the key perturbation value applied before hashing. The returned value
     * should be non-zero and ideally be different for each instance.
     * 
     * <p>This matters because
     * keys are nearly-ordered by their hashed values so when adding one container's
     * values to the other, the number of collisions can skyrocket into the worst case
     * possible.
     * 
     * <p>The default implementation tries to provide a repeatable hash order 
     * and at the same attempts to minimize the risk of hash keys clustering. It can be
     * overriden.
     * 
     * <p>If it is known that hash containers will not be added to each other 
     * (will be used for counting only, for example) then some speed can be gained by 
     * not perturbing keys before hashing and returning a value of zero for all possible
     * capacities. The speed gain is a result of faster rehash operation (keys are mostly
     * in order).   
     */
    protected int computePerturbationValue(int newBufferSize)
    {
        // Make sure we still have access to old keys.
        assert this.keys == null || this.keys.length < newBufferSize;
        return newBufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType remove(KType key)
    {
        final KType [] keys = keys_();
        final VType [] values = values_();
        final int mask = allocated.length - 1;
        int slot = BitMixer.mix0(key, perturbation) & mask; 
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
             {
                assigned--;
                VType v = values[slot];
                shiftConflictingKeys(slot);
                return v;
             }
             slot = (slot + 1) & mask;
        }

        return Intrinsics.<VType> defaultVTypeValue();
    }

    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>. 
     */
    protected void shiftConflictingKeys(int slotCurr)
    {
        // Copied nearly verbatim from fastutil's impl.
        final int mask = allocated.length - 1;
        int slotPrev, slotOther;
        while (true)
        {
            slotCurr = ((slotPrev = slotCurr) + 1) & mask;

            while (allocated[slotCurr])
            {
                slotOther = BitMixer.mix0(keys[slotCurr], perturbation) & mask;
                if (slotPrev <= slotCurr)
                {
                    // We are on the right of the original slot.
                    if (slotPrev >= slotOther || slotOther > slotCurr)
                        break;
                }
                else
                {
                    // We have wrapped around.
                    if (slotPrev >= slotOther && slotOther > slotCurr)
                        break;
                }
                slotCurr = (slotCurr + 1) & mask;
            }

            if (!allocated[slotCurr]) 
                break;

            // Shift key/value pair.
            keys[slotPrev] = keys[slotCurr];           
            values[slotPrev] = values[slotCurr];           
        }

        allocated[slotPrev] = false;
        
        /* #if ($TemplateOptions.KTypeGeneric) */ 
        keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue(); 
        /* #end */
        /* #if ($TemplateOptions.VTypeGeneric) */
        values[slotPrev] = Intrinsics.<VType> defaultVTypeValue(); 
        /* #end */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(KTypeContainer<? extends KType> container)
    {
        final int before = this.assigned;

        for (KTypeCursor<? extends KType> cursor : container)
        {
            remove(cursor.value);
        }

        return before - this.assigned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(KTypePredicate<? super KType> predicate)
    {
        final int before = this.assigned;
        final KType [] keys = keys_();
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length;)
        {
            if (states[i])
            {
                if (predicate.apply(keys[i]))
                {
                    assigned--;
                    shiftConflictingKeys(i);
                    // Repeat the check for the same i.
                    continue;
                }
            }
            i++;
        }
        return before - this.assigned;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Use the following snippet of code to check for key existence
     * first and then retrieve the value if it exists.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lget(); 
     * </pre>
     * <p>The above code <strong>cannot</strong> be used by multiple concurrent
     * threads because a call to {@link #containsKey} stores
     * the temporary slot number in {@link #lastSlot}. An alternative to the above
     * conditional statement is to use {@link #getOrDefault} and
     * provide a custom default value sentinel (not present in the value set).</p>
     */
    @Override
    public VType get(KType key)
    {
        final KType [] keys = keys_();
        final VType [] values = values_();

        // Same as:
        // getOrDefault(key, Intrinsics.<VType> defaultVTypeValue())
        // but let's keep it duplicated for VMs that don't have advanced inlining.
        final int mask = allocated.length - 1;
        int slot = BitMixer.mix0(key, perturbation) & mask;
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
            {
                return values[slot]; 
            }
            
            slot = (slot + 1) & mask;
        }
        return Intrinsics.<VType> defaultVTypeValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VType getOrDefault(KType key, VType defaultValue)
    {
        final KType [] keys = keys_();
        final VType [] values = values_();
      
        final int mask = allocated.length - 1;
        int slot = BitMixer.mix0(key, perturbation) & mask;
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
            {
                return values[slot]; 
            }
            
            slot = (slot + 1) & mask;
        }
        return defaultValue;
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    /**
     * Returns the last key stored in this has map for the corresponding
     * most recent call to {@link #containsKey}.
     * 
     * <p>Use the following snippet of code to check for key existence
     * first and then retrieve the key value if it exists.</p>
     * <pre>
     * if (map.containsKey(key))
     *   value = map.lkey(); 
     * </pre>
     * 
     * <p>This is equivalent to calling:</p>
     * <pre>
     * if (map.containsKey(key))
     *   key = map.keys[map.lslot()];
     * </pre>
     */
    public KType lkey()
    {
        return Intrinsics.<KType> cast(keys[lslot()]);
    }
    /* #end */

    /**
     * Returns the last value saved in a call to {@link #containsKey}.
     * 
     * @see #containsKey
     */
    public VType lget()
    {
        assert lastSlot >= 0 : "Call containsKey() first.";
        assert allocated[lastSlot] : "Last call to exists did not have any associated value.";
    
        return Intrinsics.<VType> cast(values[lastSlot]);
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
        assert allocated[lastSlot] : "Last call to exists did not have any associated value.";

        final VType previous = Intrinsics.<VType> cast(values[lastSlot]);
        values[lastSlot] = key;
        return previous;
    }

    /**
     * @return Returns the slot of the last key looked up in a call to {@link #containsKey} if
     * it returned <code>true</code>.
     * 
     * @see #containsKey
     */
    public int lslot()
    {
        assert lastSlot >= 0 : "Call containsKey() first.";
        return lastSlot;
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
     * or, to modify the value at the given key without looking up
     * its slot twice:
     * <pre>
     * if (map.containsKey(key))
     *   map.lset(map.lget() + 1);
     * </pre>
     * #if ($TemplateOptions.KTypeGeneric) or, to retrieve the key-equivalent object from the map:
     * <pre>
     * if (map.containsKey(key))
     *   map.lkey();
     * </pre>#end
     * 
     * #if ($TemplateOptions.KTypeGeneric)
     * <p><strong>Important:</strong> {@link #containsKey} and consecutive {@link #lget}, {@link #lset}
     * or {@link #lkey} must not be used by concurrent threads because {@link #lastSlot} is 
     * used to store state.</p>
     * #end
     */
    @Override
    public boolean containsKey(KType key)
    {
        final int mask = allocated.length - 1;
        int slot = BitMixer.mix0(key, perturbation) & mask;
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
            {
                lastSlot = slot;
                return true; 
            }
            slot = (slot + 1) & mask;
        }
        lastSlot = -1;
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Does not release internal buffers.</p>
     */
    @Override
    public void clear()
    {
        assigned = 0;

        // States are always cleared.
        Arrays.fill(allocated, false);

        /* #if ($TemplateOptions.KTypeGeneric) */
        Arrays.fill(keys, null); // Help the GC.
        /* #end */

        /* #if ($TemplateOptions.VTypeGeneric) */
        Arrays.fill(values, null); // Help the GC.
        /* #end */
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
        for (KTypeVTypeCursor<KType, VType> c : this)
        {
            h += BitMixer.mix0(c.key) + BitMixer.mix0(c.value);
        }
        return h;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj != null &&
               getClass() == obj.getClass() &&
               sameEntries(getClass().cast(obj));
    }

    /**
     * Return true if all keys of some other container exist in this container.
#if ($TemplateOptions.KTypeGeneric) 
     * Equality comparison is performed with this object's {@link #sameKeys} 
     * method.
#end 
     */
    private boolean sameEntries(KTypeVTypeOpenHashMap<?, ?> other) {
      if (other.size() != size()) {
        return false;
      }

      Iterator<? extends KTypeVTypeCursor<?, ?>> i = other.iterator();
      while (i.hasNext()) {
        KTypeVTypeCursor<?, ?> c = i.next();
        KType key = Intrinsics.<KType> cast(c.key);
        if (containsKey(key) &&
            Intrinsics.equalsVType(c.value, get(key))) {
          continue;
        }
        return false;
      }

      return true;
    }

    /**
     * An iterator implementation for {@link #iterator}.
     */
    private final class EntryIterator extends AbstractIterator<KTypeVTypeCursor<KType, VType>>
    {
        private final KTypeVTypeCursor<KType, VType> cursor;

        public EntryIterator()
        {
            cursor = new KTypeVTypeCursor<KType, VType>();
            cursor.index = -1;
        }

        @Override
        protected KTypeVTypeCursor<KType, VType> fetch()
        {
            int i = cursor.index + 1;
            final int max = keys.length;
            while (i < max && !allocated[i])
            {
                i++;
            }
            
            if (i == max)
                return done();

            cursor.index = i;
            cursor.key = Intrinsics.<KType> cast(keys[i]);
            cursor.value = Intrinsics.<VType> cast(values[i]);

            return cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<KTypeVTypeCursor<KType, VType>> iterator()
    {
        return new EntryIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeVTypeProcedure<? super KType, ? super VType>> T forEach(T procedure)
    {
        final KType [] keys = this.keys_();
        final VType [] values = this.values_();
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i])
                procedure.apply(keys[i], values[i]);
        }
        
        return procedure;
    }

    /**
     * Returns a specialized view of the keys of this associated container. 
     * The view additionally implements {@link ObjectLookupContainer}.
     */
    public KeysContainer keys()
    {
        return new KeysContainer();
    }

    /**
     * A view of the keys inside this hash map.
     */
    public final class KeysContainer 
        extends AbstractKTypeCollection<KType> implements KTypeLookupContainer<KType>
    {
        private final KTypeVTypeOpenHashMap<KType, VType> owner = 
            KTypeVTypeOpenHashMap.this;
        
        @Override
        public boolean contains(KType e)
        {
            return containsKey(e);
        }
        
        @Override
        public <T extends KTypeProcedure<? super KType>> T forEach(T procedure)
        {
            final KType [] localKeys = owner.keys_();
            final boolean [] localStates = owner.allocated;

            for (int i = 0; i < localStates.length; i++)
            {
                if (localStates[i])
                    procedure.apply(localKeys[i]);
            }

            return procedure;
        }

        @Override
        public <T extends KTypePredicate<? super KType>> T forEach(T predicate)
        {
            final KType [] localKeys = owner.keys_();
            final boolean [] localStates = owner.allocated;

            for (int i = 0; i < localStates.length; i++)
            {
                if (localStates[i])
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
        public Iterator<KTypeCursor<KType>> iterator()
        {
            return new KeysIterator();
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
        public int removeAll(KTypePredicate<? super KType> predicate)
        {
            return owner.removeAll(predicate);
        }

        @Override
        public int removeAll(final KType e)
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
    private final class KeysIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        private final KTypeCursor<KType> cursor;

        public KeysIterator()
        {
            cursor = new KTypeCursor<KType>();
            cursor.index = -1;
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {
            int i = cursor.index + 1;
            final int max = keys.length;
            while (i < max && !allocated[i])
            {
                i++;
            }
            
            if (i == max)
                return done();

            cursor.index = i;
            cursor.value = keys_()[i];

            return cursor;
        }
    }

    /**
     * @return Returns a container with all values stored in this map.
     */
    @Override
    public KTypeContainer<VType> values()                                                                                                    
    {                                                                                                                                         
        return new ValuesContainer();                                                                                                         
    }                                                                                                                                         

    /**                                                                                                                                       
     * A view over the set of values of this map.                                                                                                         
     */                                                                                                                                       
    private final class ValuesContainer extends AbstractKTypeCollection<VType>                                                               
    {                                                                                                                                         
        @Override                                                                                                                             
        public int size()                                                                                                                     
        {                                                                                                                                     
            return KTypeVTypeOpenHashMap.this.size();                                                                                       
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public boolean isEmpty()                                                                                                              
        {                                                                                                                                     
            return KTypeVTypeOpenHashMap.this.isEmpty();                                                                                    
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public boolean contains(VType value)                                                                                                  
        {                                                                                                                                     
            // This is a linear scan over the values, but it's in the contract, so be it.                                                     
            final boolean [] allocated = KTypeVTypeOpenHashMap.this.allocated;                                                              
            final VType [] values = KTypeVTypeOpenHashMap.this.values_();                                                                      
                                                                                                                                              
            for (int slot = 0; slot < allocated.length; slot++)                                                                               
            {                                                                                                                                 
                if (allocated[slot] && Intrinsics.equalsVType(value, values[slot]))                                    
                {                                                                                                                             
                    return true;                                                                                                              
                }                                                                                                                             
            }
            return false;                                                                                                                     
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public <T extends KTypeProcedure<? super VType>> T forEach(T procedure)                                                              
        {                                                                                                                                     
            final boolean [] allocated = KTypeVTypeOpenHashMap.this.allocated;                                                              
            final VType [] values = KTypeVTypeOpenHashMap.this.values_();                                                                      
                                                                                                                                              
            for (int i = 0; i < allocated.length; i++)                                                                                        
            {                                                                                                                                 
                if (allocated[i])                                                                                                             
                    procedure.apply(values[i]);                                                                                               
            }                                                                                                                                 
                                                                                                                                              
            return procedure;                                                                                                                 
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public <T extends KTypePredicate<? super VType>> T forEach(T predicate)                                                              
        {                                                                                                                                     
            final boolean [] allocated = KTypeVTypeOpenHashMap.this.allocated;                                                              
            final VType [] values = KTypeVTypeOpenHashMap.this.values_();                                                                      
                                                                                                                                              
            for (int i = 0; i < allocated.length; i++)                                                                                        
            {                                                                                                                                 
                if (allocated[i])                                                                                                             
                {                                                                                                                             
                    if (!predicate.apply(values[i]))                                                                                          
                        break;                                                                                                                
                }                                                                                                                             
            }                                                                                                                                 
                                                                                                                                              
            return predicate;                                                                                                                 
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public Iterator<KTypeCursor<VType>> iterator()                                                                                       
        {                                                                                                                                     
            return new ValuesIterator();                                                                                                      
        }                                                                                                                                     

        @Override                                                                                                                             
        public int removeAll(VType e)                                                                                              
        {                                                                                                                                     
            throw new UnsupportedOperationException();                                                                                        
        }                                                                                                                                     
                                                                                                                                              
        @Override                                                                                                                             
        public int removeAll(KTypePredicate<? super VType> predicate)                                                                        
        {                                                                                                                                     
            throw new UnsupportedOperationException();                                                                                        
        }                                                                                                                                     

        @Override                                                                                                                             
        public void clear()                                                                                                                   
        {                                                                                                                                     
            throw new UnsupportedOperationException();                                                                                        
        }                                                                                                                                     
    }                                                                                                                                         

    public void ensureCapacity(int expectedKeys) {
      throw new UnsupportedOperationException();
    }

    /**                                                                                                                                       
     * An iterator over the set of assigned values.                                                                                           
     */                                                                                                                                       
    private final class ValuesIterator extends AbstractIterator<KTypeCursor<VType>>                                                               
    {                                                                                                                                         
        private final KTypeCursor<VType> cursor;                                                                                             
                                                                                                                                              
        public ValuesIterator()                                                                                                               
        {                                                                                                                                     
            cursor = new KTypeCursor<VType>();                                                                                               
            cursor.index = -1;                                                                                                        
        }                                                                                                                                     
        
        @Override
        protected KTypeCursor<VType> fetch()
        {
            int i = cursor.index + 1;
            final int max = keys.length;
            while (i < max && !allocated[i])
            {
                i++;
            }
            
            if (i == max)
                return done();

            cursor.index = i;
            cursor.value = Intrinsics.<VType> cast(values[i]);

            return cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KTypeVTypeOpenHashMap<KType, VType> clone()
    {
        try
        {
            /*! #if ($templateOnly) !*/ @SuppressWarnings("unchecked") /*! #end !*/
            KTypeVTypeOpenHashMap<KType, VType> cloned = 
                (KTypeVTypeOpenHashMap<KType, VType>) super.clone();
            
            cloned.keys = keys.clone();
            cloned.values = values.clone();
            cloned.allocated = allocated.clone();

            return cloned;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Compares two keys for equality. Override if custom equality behavior 
     * is required.
     */
    protected boolean sameKeys(KType k1, KType k2) {
      return Intrinsics.<KType> same(k1, k2); 
    }
    /*! #end !*/

    /**
     * Convert the contents of this map to a human-friendly string. 
     */
    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[");

        boolean first = true;
        for (KTypeVTypeCursor<KType, VType> cursor : this)
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
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> from(KType [] keys, VType [] values)
    {
        if (keys.length != values.length) 
            throw new IllegalArgumentException("Arrays of keys and values must have an identical length."); 

        KTypeVTypeOpenHashMap<KType, VType> map = new KTypeVTypeOpenHashMap<KType, VType>();
        for (int i = 0; i < keys.length; i++)
        {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Create a hash map from another associative container.
     */
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> from(KTypeVTypeAssociativeContainer<KType, VType> container)
    {
        return new KTypeVTypeOpenHashMap<KType, VType>(container);
    }
    
    /**
     * Create a new hash map without providing the full generic signature (constructor
     * shortcut). 
     */
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> newInstance()
    {
        return new KTypeVTypeOpenHashMap<KType, VType>();
    }


    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> newInstance(int expectedElements)
    {
        return new KTypeVTypeOpenHashMap<KType, VType>(expectedElements);
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> newInstance(int expectedElements, double loadFactor)
    {
      return new KTypeVTypeOpenHashMap<KType, VType>(expectedElements, loadFactor);
    }

    /**
     * Returns a new object with no key perturbations (see
     * {@link #computePerturbationValue(int)}). Only use when sure the container will not
     * be used for direct copying of keys to another hash container.
     */
    public static <KType, VType> KTypeVTypeOpenHashMap<KType, VType> newInstanceWithoutPerturbations()
    {
        return new KTypeVTypeOpenHashMap<KType, VType>() {
            @Override
            protected int computePerturbationValue(int capacity) { return 0; }
        };
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
    protected void rehash(KType[] fromKeys, VType[] fromValues) {
      // Rehash all stored key/value pairs into the new buffers.
      final KType[] keys = Intrinsics.<KType[]> cast(this.keys);
      final VType[] values = Intrinsics.<VType[]> cast(this.values);
      final int mask = this.mask;
      KType existing;
      for (int i = fromKeys.length; --i >= 0;) {
        if (!Intrinsics.isEmptyKey(existing = fromKeys[i])) {
          int slot = hashKey(existing) & mask;
          while (!Intrinsics.isEmptyKey(keys[slot])) {
            slot = (slot + 1) & mask;
          }
          keys[slot] = existing;
          values[slot] = fromValues[i];
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
        this.keys = Intrinsics.<KType> newArray(arraySize);
        this.values = Intrinsics.<VType> newArray(arraySize);
      } catch (OutOfMemoryError e) {
        this.keys = prevKeys;
        this.values = prevValues;
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
             && Intrinsics.isEmptyKey(Intrinsics.<KType> cast(keys[slot]))
             && !Intrinsics.isEmptyKey(pendingKey);

      // Try to allocate new buffers first. If we OOM, we leave in a consistent state.
      final KType[] prevKeys = Intrinsics.<KType[]> cast(this.keys);
      final VType[] prevValues = Intrinsics.<VType[]> cast(this.values);
      allocateBuffers(nextBufferSize(keys.length, assigned, loadFactor));
      assert this.keys.length > prevKeys.length;

      // We have succeeded at allocating new data so insert the pending key/value at
      // the free slot in the old arrays before rehashing.
      prevKeys[slot] = pendingKey;
      prevValues[slot] = pendingValue;

      // Rehash old keys, including the pending key.
      rehash(prevKeys, prevValues);
    }

    /*! #if ($templateOnly) !*/ 
    @SuppressWarnings("unchecked")
    /*! #end !*/
    private KType[] keys_() {
      return (KType[]) keys;
    }

    /*! #if ($templateOnly) !*/ 
    @SuppressWarnings("unchecked")
    /*! #end !*/
    private VType[] values_() {
      return (VType[]) values;
    }
}
