package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.hash.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.HashContainers.*;
import static com.carrotsearch.hppc.Containers.*;

/**
 * A hash set of <code>KType</code>s, implemented using using open
 * addressing with linear probing for collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation
 * are always allocated to the nearest size that is a power of two. When
 * the fill ratio of these buffers is exceeded, their size is doubled.
 * </p>
#if ($TemplateOptions.KTypeGeneric)
 * <p>
 * A brief comparison of the API against the Java Collections framework:
 * </p>
 * <table class="nice" summary="Java Collections HashSet and HPPC ObjectOpenHashSet, related methods.">
 * <caption>Java Collections {@link HashSet} and HPPC {@link ObjectOpenHashSet}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain HashSet java.util.HashSet}</th>
 *         <th scope="col">{@link ObjectOpenHashSet}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>boolean add(E)    </td><td>boolean add(E)</td></tr>
 * <tr class="odd"><td>boolean remove(E) </td><td>int removeAllOccurrences(E)</td></tr>
 * <tr            ><td>size, clear, 
 *                     isEmpty</td>      <td>size, clear, isEmpty</td></tr>
 * <tr class="odd"><td>contains(E)  </td><td>contains(E), lkey()</td></tr>
 * <tr            ><td>iterator     </td><td>{@linkplain #iterator() iterator} over set values,
 *                                           pseudo-closures</td></tr>
 * </tbody>
 * </table>
 * 
 * <p>This implementation supports <code>null</code> keys.</p>
#else
 * <p>See {@link ObjectOpenHashSet} class for API similarities and differences against Java
 * Collections.  
#end
 * 
 * <p><b>Important node.</b> The implementation uses power-of-two tables and linear
 * probing, which may cause poor performance (many collisions) if hash values are
 * not properly distributed. 
 * This implementation uses {@link MurmurHash3} for rehashing keys.</p>
 * 
 * @author This code is inspired by the collaboration and implementation in the <a
 *         href="http://fastutil.dsi.unimi.it/">fastutil</a> project.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenHashSet<KType>
    extends AbstractKTypeCollection<KType> 
    implements KTypeLookupContainer<KType>, KTypeSet<KType>, Cloneable
{
    /**
     * Hash-indexed array holding all set entries.
     * 
#if ($TemplateOptions.KTypeGeneric)
     * <p><strong>Important!</strong> 
     * The actual value in this field is always an instance of <code>Object[]</code>.
     * Be warned that <code>javac</code> emits additional casts when <code>keys</code> 
     * are directly accessed; <strong>these casts
     * may result in exceptions at runtime</strong>. A workaround is to cast directly to
     * <code>Object[]</code> before accessing the buffer's elements (although it is highly
     * recommended to use a {@link #iterator()} instead.
     * </pre>
#end
     * 
     * @see #allocated
     */
    public KType [] keys;

    /**
     * Information if an entry (slot) in the {@link #keys} table is allocated
     * or empty.
     * 
     * @see #assigned
     */
    public boolean [] allocated;

    /**
     * The number of assigned slots.
     */
    public int assigned;
    
    /**
     * Buffer round-robin mask.
     */
    protected int mask;

    /**
     * We perturb hash values with a container-unique
     * seed to avoid problems with nearly-sorted-by-hash 
     * values on iterations.
     * 
     * @see "http://issues.carrot2.org/browse/HPPC-80"
     * @see "http://issues.carrot2.org/browse/HPPC-103"
     */
    protected int keyMixer;

    /**
     * Resize buffers when {@link #assigned} hits this value. 
     */
    protected int resizeAt;

    /**
     * The load factor for this map (fraction of allocated slots
     * before the buffers must be rehashed or reallocated) passed at 
     * construction time.
     * 
     * @see #getLoadFactor()
     */
    protected double loadFactor;

    /**
     * The most recent slot accessed in {@link #contains}.
     * 
     * @see #contains
     * #if ($TemplateOptions.KTypeGeneric)
     * @see #lkey
     * #end
     */
    // NOCOMMIT: remove, http://issues.carrot2.org/browse/HPPC-116
    protected int lastSlot;

    /**
     * Per-instance hash order mixing strategy.
     */
    protected final HashOrderMixingStrategy orderMixer;

    /**
     * Creates a hash set with the default expected number of elements, 
     * load factor and a random mixing seed.
`     */
    public KTypeOpenHashSet()
    {
        this(DEFAULT_EXPECTED_ELEMENTS, DEFAULT_LOAD_FACTOR);
    }
    /**
     * Creates a hash set with the given number of expected elements and
     * a random mixing seed.
     */
    public KTypeOpenHashSet(int expectedElements)
    {
        this(expectedElements, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set capable of storing the given number of keys without
     * resizing, the given load factor and a random key mixing seed. 
     * 
     * @param expectedElements The expected number of keys that won't cause a rehash (inclusive).  
     * @param loadFactor The load factor for internal buffers in (0, 1) range.
     */
    public KTypeOpenHashSet(int expectedElements, double loadFactor)
    {
      this(expectedElements, loadFactor, HashOrderMixing.randomized());
    }

    /**
     * Creates a hash set capable of storing the given number of keys without
     * resizing, the given load factor and key mixing seed. 
     * 
     * @param expectedElements The expected number of keys that won't cause a rehash (inclusive).  
     * @param loadFactor The load factor for internal buffers in (0, 1) range.
     * @param mixSeed The initial key mixing seed.
     */
    public KTypeOpenHashSet(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer)
    {
        this.loadFactor = loadFactor;
        this.orderMixer = orderMixer;
        loadFactor = getLoadFactor();
        allocateBuffers(minBufferSize(expectedElements, loadFactor), loadFactor);
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public KTypeOpenHashSet(KTypeContainer<KType> container)
    {
        // NOCOMMIT: leftover.
        this((int) (container.size() * (1 + DEFAULT_LOAD_FACTOR)));
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(KType e)
    {
        assert assigned < allocated.length;

        final int mask = this.mask;
        int slot = hashKey(e) & mask;
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(e, keys[slot]))
            {
                return false;
            }

            slot = (slot + 1) & mask;
        }

        // Check if we need to grow. If so, reallocate new data, 
        // fill in the last element and rehash.
        if (assigned == resizeAt) {
            expandAndAdd(e, slot);
        } else {
            assigned++;
            allocated[slot] = true;
            keys[slot] = e;                
        }
        return true;
    }

    /**
     * Adds two elements to the set.
     */
    // TODO: remove, esoteric use case
    public int add(KType e1, KType e2)
    {
        int count = 0;
        if (add(e1)) count++;
        if (add(e2)) count++;
        return count;
    }

    /**
     * Vararg-signature method for adding elements to this set.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     * 
     * @return Returns the number of elements that were added to the set
     * (were not present in the set).
     */
    // TODO: rename as addAll, ensureCapacity
    public int add(KType... elements)
    {
        int count = 0;
        for (KType e : elements)
            if (add(e)) count++;
        return count;
    }

    /**
     * Adds all elements from a given container to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(KTypeContainer<? extends KType> container)
    {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from a given iterable to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(Iterable<? extends KTypeCursor<? extends KType>> iterable)
    {
        int count = 0;
        for (KTypeCursor<? extends KType> cursor : iterable)
        {
            if (add(cursor.value)) count++;
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAllOccurrences(KType key)
    {
        return remove(key) ? 1 : 0;
    }

    /**
     * An alias for the (preferred) {@link #removeAllOccurrences}.
     */
    public boolean remove(KType key)
    {
        final int mask = this.mask;
        int slot = hashKey(key) & mask; 
        while (allocated[slot])
        {
            if (Intrinsics.equalsKType(key, keys[slot]))
             {
                assigned--;
                shiftConflictingKeys(slot);
                return true;
             }
             slot = (slot + 1) & mask;
        }
        return false;
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    /**
     * Returns the last key saved in a call to {@link #contains} if it returned <code>true</code>.
     * 
     * @see #contains
     */
    public KType lkey()
    {
        assert lastSlot >= 0 : "Call contains() first.";
        assert allocated[lastSlot] : "Last call to exists did not have any associated value.";
    
        return keys[lastSlot];
    }
    /* #end */

    /**
     * @return Returns the slot of the last key looked up in a call to {@link #contains} if
     * it returned <code>true</code>.
     * 
     * @see #contains
     */
    public int lslot()
    {
        assert lastSlot >= 0 : "Call contains() first.";
        return lastSlot;
    }

    /**
     * {@inheritDoc}
     * 
     * #if ($TemplateOptions.KTypeGeneric) <p>Saves the associated value for fast access using {@link #lkey()}.</p>
     * <pre>
     * if (map.contains(key))
     *   value = map.lkey(); 
     * </pre> #end
     */
    @Override
    public boolean contains(KType key)
    {
        final int mask = allocated.length - 1;
        int slot = hashKey(key) & mask;
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

        Arrays.fill(allocated, false);
        Arrays.fill(keys, Intrinsics.<KType>defaultKTypeValue());
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
     */
    @Override
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
        final KType [] keys = this.keys;
        final boolean [] allocated = this.allocated;
        for (int i = allocated.length; --i >= 0;)
        {
            if (allocated[i])
            {
                h += Internals.rehash(keys[i]);
            }
        }
        return h;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings("unchecked") 
    /* #end */
    public boolean equals(Object obj)
    {
        if (obj != null)
        {
            if (obj == this) return true;

            if (obj instanceof KTypeSet<?>)
            {
                KTypeSet<Object> other = (KTypeSet<Object>) obj;
                if (other.size() == this.size())
                {
                    for (KTypeCursor<KType> c : this)
                    {
                        if (!other.contains(c.value))
                        {
                            return false;
                        }
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
    private final class EntryIterator extends AbstractIterator<KTypeCursor<KType>>
    {
        private final KTypeCursor<KType> cursor;

        public EntryIterator()
        {
            cursor = new KTypeCursor<KType>();
            cursor.index = -1;
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {
            final int max = keys.length;

            int i = cursor.index + 1;
            while (i < keys.length && !allocated[i])
            {
                i++;
            }

            if (i == max)
                return done();

            cursor.index = i;
            cursor.value = keys[i];
            return cursor;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<KTypeCursor<KType>> iterator()
    {
        return new EntryIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(T procedure)
    {
        final KType [] keys = this.keys;
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i])
                procedure.apply(keys[i]);
        }

        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*! #if ($TemplateOptions.KTypePrimitive) 
    public KType [] toArray()
        #else !*/
    public Object [] toArray()
    /*! #end !*/
    {
        final KType [] cloned = Intrinsics.newKTypeArray(assigned);
        for (int i = 0, j = 0; i < keys.length; i++)
            if (allocated[i])
                cloned[j++] = keys[i];
        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KTypeOpenHashSet<KType> clone()
    {
        try
        {
            /* #if ($TemplateOptions.KTypeGeneric) */
            @SuppressWarnings("unchecked")
            /* #end */
            KTypeOpenHashSet<KType> cloned = (KTypeOpenHashSet<KType>) super.clone();
            cloned.keys = keys.clone();
            cloned.allocated = allocated.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(T predicate)
    {
        final KType [] keys = this.keys;
        final boolean [] states = this.allocated;

        for (int i = 0; i < states.length; i++)
        {
            if (states[i])
            {
                if (!predicate.apply(keys[i]))
                    break;
            }
        }

        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAll(KTypePredicate<? super KType> predicate)
    {
        final KType [] keys = this.keys;
        final boolean [] allocated = this.allocated;

        int before = assigned;
        for (int i = 0; i < allocated.length;)
        {
            if (allocated[i])
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
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static <KType> KTypeOpenHashSet<KType> from(KType... elements)
    {
        final KTypeOpenHashSet<KType> set = new KTypeOpenHashSet<KType>(
            // NOCOMMIT: LEFTOVER!
            (int) (elements.length * (1 + DEFAULT_LOAD_FACTOR)));
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static <KType> KTypeOpenHashSet<KType> from(KTypeContainer<KType> container)
    {
        return new KTypeOpenHashSet<KType>(container);
    }
    
    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenHashSet<KType> newInstance()
    {
        return new KTypeOpenHashSet<KType>();
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenHashSet<KType> newInstance(int expectedElements)
    {
        return new KTypeOpenHashSet<KType>(expectedElements);
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenHashSet<KType> newInstance(int expectedElements, double loadFactor)
    {
        return new KTypeOpenHashSet<KType>(expectedElements, loadFactor);
    }

    /**
     * Validate load factor range and return it.
     */
    protected double getLoadFactor() {
      checkLoadFactor(loadFactor, MIN_LOAD_FACTOR, MAX_LOAD_FACTOR);
      return loadFactor;
    }

    /**
     * Expand the internal storage buffers or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    protected void expandAndAdd(KType pendingKey, int freeSlot)
    {
        assert assigned == resizeAt;
        assert !allocated[freeSlot];
    
        // Try to allocate new buffers first. If we OOM, it'll be now without
        // leaving the data structure in an inconsistent state.
        final KType   [] prevKeys      = this.keys;
        final boolean [] prevAllocated = this.allocated;
        final double loadFactor = getLoadFactor();
        allocateBuffers(nextBufferSize(keys.length, assigned, loadFactor), loadFactor);
        assert this.keys.length > prevKeys.length;
    
        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.
        lastSlot = -1;
        assigned++;
        prevAllocated[freeSlot] = true;
        prevKeys[freeSlot] = pendingKey;
        
        // Rehash all stored keys into the new buffers.
        final KType []   keys = this.keys;
        final boolean [] allocated = this.allocated;
        final int mask = this.mask;
        for (int i = prevAllocated.length; --i >= 0;)
        {
            if (prevAllocated[i])
            {
                final KType k = prevKeys[i];
    
                int slot = hashKey(k) & mask;
                while (allocated[slot])
                {
                    slot = (slot + 1) & mask;
                }
    
                allocated[slot] = true;
                keys[slot] = k;                
            }
        }
    
        // NOCOMMIT: Just release the reference, let the GC handle this?
        /* #if ($TemplateOptions.KTypeGeneric) */ Arrays.fill(prevKeys,   null); /* #end */
    }

    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>. 
     */
    protected void shiftConflictingKeys(int slotCurr)
    {
        // Copied nearly verbatim from fastutil's impl.
        final int mask = this.mask;
        int slotPrev, slotOther;
        while (true)
        {
            slotCurr = ((slotPrev = slotCurr) + 1) & mask;
    
            while (allocated[slotCurr])
            {
                slotOther = hashKey(keys[slotCurr]) & mask;
                if (slotPrev <= slotCurr)
                {
                    // We are on the right of the original slot.
                    if (slotPrev >= slotOther || slotOther > slotCurr) {
                        break;
                    }
                }
                else
                {
                    // We have wrapped around.
                    if (slotPrev >= slotOther && slotOther > slotCurr) {
                        break;
                    }
                }
                slotCurr = (slotCurr + 1) & mask;
            }
    
            if (!allocated[slotCurr]) { 
                break;
            }
    
            // Shift key/value pair.
            keys[slotPrev] = keys[slotCurr];
        }
    
        allocated[slotPrev] = false;
    
        /* #if ($TemplateOptions.KTypeGeneric) */ 
        keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue(); 
        /* #end */
    }

    /**
     * Allocate internal buffers and thresholds to ensure they can hold 
     * the given number of elements.
     */
    protected void allocateBuffers(int arraySize, double loadFactor)
    {
        // Compute new hash mixer before actually expanding
        final int newKeyMixer = this.orderMixer.newKeyMixer(arraySize);
    
        // Ensure no change is done if we hit an OOM.
        KType [] prevKeys = this.keys;
        boolean [] prevAllocated = this.allocated;
        try {
          this.keys = Intrinsics.newKTypeArray(arraySize);
          this.allocated = new boolean [arraySize];
        } catch (OutOfMemoryError e) {
          this.keys = prevKeys;
          this.allocated = prevAllocated;
          throw new BufferAllocationException(
              "Not enough memory to allocate buffers for rehashing: %,d -> %,d", 
              e,
              this.keys,
              arraySize);
        }
    
        this.resizeAt = expandAtCount(arraySize, loadFactor);
        this.keyMixer = newKeyMixer;
        this.mask = arraySize - 1;
    }

    /**
     * Additionally perturbs the hash of a given key with {@link #keyMixer}.
     */
    protected int hashKey(KType key) {
      return Internals.rehash(key, this.keyMixer);
    }
}
