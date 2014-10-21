package com.carrotsearch.hppc;

import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.hash.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

import static com.carrotsearch.hppc.Internals.*;
import static com.carrotsearch.hppc.HashContainerUtils.*;

/**
 * A hash set of <code>KType</code>s, implemented using using open
 * addressing with linear probing for collision resolution.
 * 
 * <p>
 * The internal buffers of this implementation ({@link #keys}), {@link #allocated})
 * are always allocated to the nearest size that is a power of two. When
 * the capacity exceeds the given load factor, the buffer size is doubled.
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
 * <tr            ><td>boolean add(E) </td><td>boolean add(E)</td></tr>
 * <tr class="odd"><td>boolean remove(E)    </td><td>int removeAllOccurrences(E)</td></tr>
 * <tr            ><td>size, clear,
 *                     isEmpty</td><td>size, clear, isEmpty</td></tr>
 * <tr class="odd"><td>contains(E)    </td><td>contains(E), lkey()</td></tr>
 * <tr            ><td>iterator       </td><td>{@linkplain #iterator() iterator} over set values,
 *                                               pseudo-closures</td></tr>
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
     * Minimum capacity for the map.
     */
    public final static int MIN_CAPACITY = HashContainerUtils.MIN_CAPACITY;

    /**
     * Default capacity.
     */
    public final static int DEFAULT_CAPACITY = HashContainerUtils.DEFAULT_CAPACITY;

    /**
     * Default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = HashContainerUtils.DEFAULT_LOAD_FACTOR;

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
    public KType[] keys;

    /**
     * True if key = 0 is in the set.
     */
    public boolean allocatedDefaultKey = false;

    /**
     * Cached number of assigned slots in {@link #keys}.
     */
    public int assigned;

    /**
     * The load factor for this map (fraction of allocated slots
     * before the buffers must be rehashed or reallocated).
     */
    public final float loadFactor;

    /**
     * Resize buffers when {@link #keys} hits this value.
     */
    protected int resizeAt;

    /**
     * The most recent slot accessed in {@link #contains}.
     * 
     * @see #contains
     * #if ($TemplateOptions.KTypeGeneric)
     * @see #lkey
     * #end
     */
    protected int lastSlot;

    /**
     * We perturb hashed values with the array size to avoid problems with
     * nearly-sorted-by-hash values on iterations.
     * 
     * @see "http://issues.carrot2.org/browse/HPPC-80"
     */
    protected int perturbation;

    /**
     * Creates a hash set with the default capacity of {@value #DEFAULT_CAPACITY},
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
    `     */
    public KTypeOpenHashSet()
    {
        this(KTypeOpenHashSet.DEFAULT_CAPACITY, KTypeOpenHashSet.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity,
     * load factor of {@value #DEFAULT_LOAD_FACTOR}.
     */
    public KTypeOpenHashSet(final int initialCapacity)
    {
        this(initialCapacity, KTypeOpenHashSet.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a hash set with the given capacity and load factor.
     */
    public KTypeOpenHashSet(int initialCapacity, final float loadFactor)
    {
        initialCapacity = Math.max(initialCapacity, KTypeOpenHashSet.MIN_CAPACITY);

        assert initialCapacity > 0 : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";
        assert loadFactor > 0 && loadFactor <= 1 : "Load factor must be between (0, 1].";

        this.loadFactor = loadFactor;
        allocateBuffers(HashContainerUtils.roundCapacity(initialCapacity));
    }

    /**
     * Creates a hash set from elements of another container. Default load factor is used.
     */
    public KTypeOpenHashSet(final KTypeContainer<KType> container)
    {
        this((int) (container.size() * (1 + KTypeOpenHashSet.DEFAULT_LOAD_FACTOR)));
        addAll(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final KType e)
    {
        if (Intrinsics.equalsKTypeDefault(e)) {

            if (this.allocatedDefaultKey) {

                return false;
            }

            this.allocatedDefaultKey = true;

            return true;
        }

        final int mask = this.keys.length - 1;
        int slot = Internals.rehash(e, this.perturbation) & mask;
        while (!Intrinsics.equalsKTypeDefault(this.keys[slot]))
        {
            if (Intrinsics.equalsKType(e, this.keys[slot]))
            {
                return false;
            }

            slot = (slot + 1) & mask;
        }

        // Check if we need to grow. If so, reallocate new data,
        // fill in the last element and rehash.
        if (this.assigned == this.resizeAt) {
            expandAndAdd(e, slot);
        }
        else {
            this.assigned++;

            this.keys[slot] = e;
        }
        return true;
    }

    /**
     * Adds two elements to the set.
     */
    public int add(final KType e1, final KType e2)
    {
        int count = 0;
        if (add(e1))
            count++;
        if (add(e2))
            count++;
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
    public int add(final KType... elements)
    {
        int count = 0;
        for (final KType e : elements)
            if (add(e))
                count++;
        return count;
    }

    /**
     * Adds all elements from a given container to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(final KTypeContainer<? extends KType> container)
    {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from a given iterable to this set.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (not previously present in the set).
     */
    public int addAll(final Iterable<? extends KTypeCursor<? extends KType>> iterable)
    {
        int count = 0;
        for (final KTypeCursor<? extends KType> cursor : iterable)
        {
            if (add(cursor.value))
                count++;
        }
        return count;
    }

    /**
     * Expand the internal storage buffers (capacity) or rehash current
     * keys and values if there are a lot of deleted slots.
     */
    private void expandAndAdd(final KType pendingKey, final int freeSlot)
    {
        assert this.assigned == this.resizeAt;
        assert !Intrinsics.equalsKTypeDefault(pendingKey);

        // Try to allocate new buffers first. If we OOM, it'll be now without
        // leaving the data structure in an inconsistent state.
        final KType[] oldKeys = this.keys;

        allocateBuffers(HashContainerUtils.nextCapacity(this.keys.length));

        // We have succeeded at allocating new data so insert the pending key/value at
        // the free slot in the old arrays before rehashing.
        this.lastSlot = -1;
        this.assigned++;

        oldKeys[freeSlot] = pendingKey;

        // Rehash all stored keys into the new buffers.
        final KType[] keys = this.keys;

        final int mask = keys.length - 1;
        for (int i = oldKeys.length; --i >= 0;)
        {
            if (!Intrinsics.equalsKTypeDefault(oldKeys[i]))
            {
                final KType k = oldKeys[i];

                int slot = Internals.rehash(k, this.perturbation) & mask;

                while (!Intrinsics.equalsKTypeDefault(keys[slot]))
                {
                    slot = (slot + 1) & mask;
                }

                keys[slot] = k;
            }
        }

        /* #if ($TemplateOptions.KTypeGeneric) */Arrays.fill(oldKeys, null); /* #end */
    }

    /**
     * Allocate internal buffers for a given capacity.
     * 
     * @param capacity New capacity (must be a power of two).
     */
    private void allocateBuffers(final int capacity)
    {
        final KType[] keys = Intrinsics.newKTypeArray(capacity);

        this.keys = keys;

        this.resizeAt = Math.max(2, (int) Math.ceil(capacity * this.loadFactor)) - 1;
        this.perturbation = computePerturbationValue(capacity);
    }

    /**
     * <p>Compute the key perturbation value applied before hashing. The returned value
     * should be non-zero and ideally different for each capacity. This matters because
     * keys are nearly-ordered by their hashed values so when adding one container's
     * values to the other, the number of collisions can skyrocket into the worst case
     * possible.
     * 
     * <p>If it is known that hash containers will not be added to each other
     * (will be used for counting only, for example) then some speed can be gained by
     * not perturbing keys before hashing and returning a value of zero for all possible
     * capacities. The speed gain is a result of faster rehash operation (keys are mostly
     * in order).
     */
    protected int computePerturbationValue(final int capacity)
    {
        return HashContainerUtils.PERTURBATIONS[Integer.numberOfLeadingZeros(capacity)];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAllOccurrences(final KType key)
    {
        return remove(key) ? 1 : 0;
    }

    /**
     * An alias for the (preferred) {@link #removeAllOccurrences}.
     */
    public boolean remove(final KType key)
    {
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {

                this.allocatedDefaultKey = false;
                return true;
            }

            return false;
        }

        final int mask = this.keys.length - 1;
        int slot = Internals.rehash(key, this.perturbation) & mask;

        while (!Intrinsics.equalsKTypeDefault(this.keys[slot]))
        {
            if (Intrinsics.equalsKType(key, this.keys[slot]))
            {
                this.assigned--;
                shiftConflictingKeys(slot);
                return true;
            }
            slot = (slot + 1) & mask;
        }

        return false;
    }

    /**
     * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
     */
    protected void shiftConflictingKeys(int slotCurr)
    {
        // Copied nearly verbatim from fastutil's impl.
        final int mask = this.keys.length - 1;
        int slotPrev, slotOther;

        while (true)
        {
            slotCurr = ((slotPrev = slotCurr) + 1) & mask;

            while (!Intrinsics.equalsKTypeDefault(this.keys[slotCurr]))
            {
                slotOther = Internals.rehash(this.keys[slotCurr], this.perturbation) & mask;
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

            if (Intrinsics.equalsKTypeDefault(this.keys[slotCurr]))
                break;

            // Shift key/value pair.
            this.keys[slotPrev] = this.keys[slotCurr];
        }

        this.keys[slotPrev] = Intrinsics.<KType> defaultKTypeValue();
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    /**
     * Returns the last key saved in a call to {@link #contains} if it returned <code>true</code>.
     * 
     * @see #contains
     */
    public KType lkey()
    {
        if (this.lastSlot == -2) {

            return Intrinsics.<KType> defaultKTypeValue();
        }

        assert this.lastSlot >= 0 : "Call contains() first.";
        assert !Intrinsics.equalsKTypeDefault(this.keys[this.lastSlot]) : "Last call to exists did not have any associated value.";

        return this.keys[this.lastSlot];
    }

    /* #end */

    /**
     * @return Returns the slot of the last key looked up in a call to {@link #contains} if
     * it returned <code>true</code>.
     * or else -2 if {@link #contains} were succesfull on key = 0
     * 
     * @see #contains
     */
    public int lslot()
    {
        assert this.lastSlot >= 0 || this.lastSlot == -2 : "Call contains() first.";
        return this.lastSlot;
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
    public boolean contains(final KType key)
    {
        if (Intrinsics.equalsKTypeDefault(key)) {

            if (this.allocatedDefaultKey) {
                this.lastSlot = -2;
            }
            else {
                this.lastSlot = -1;
            }

            return this.allocatedDefaultKey;
        }

        final int mask = this.keys.length - 1;
        int slot = Internals.rehash(key, this.perturbation) & mask;

        while (!Intrinsics.equalsKTypeDefault(this.keys[slot]))
        {
            if (Intrinsics.equalsKType(key, this.keys[slot]))
            {
                this.lastSlot = slot;
                return true;
            }
            slot = (slot + 1) & mask;
        }
        this.lastSlot = -1;
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
        this.assigned = 0;
        this.lastSlot = -1;

        Arrays.fill(this.keys, Intrinsics.<KType> defaultKTypeValue()); //Help GC  and reset states
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return this.assigned + (this.allocatedDefaultKey ? 1 : 0);
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

        if (this.allocatedDefaultKey) {
            h += Internals.rehash(Intrinsics.<KType> defaultKTypeValue());
        }

        final KType[] keys = this.keys;

        for (int i = keys.length; --i >= 0;)
        {
            if (!Intrinsics.equalsKTypeDefault(keys[i]))
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
    public boolean equals(final Object obj)
    {
        if (obj != null)
        {
            if (obj == this)
                return true;

            if (obj instanceof KTypeSet<?>)
            {
                final KTypeSet<Object> other = (KTypeSet<Object>) obj;
                if (other.size() == this.size())
                {
                    for (final KTypeCursor<KType> c : this)
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
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -2;
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {

            if (this.cursor.index == -2) {

                if (KTypeOpenHashSet.this.allocatedDefaultKey) {

                    this.cursor.index = -1;
                    this.cursor.value = Intrinsics.<KType> defaultKTypeValue();

                    return this.cursor;

                }
                else {
                    //no value associated with the default key, continue iteration...
                    this.cursor.index = -1;
                }
            }

            final int max = KTypeOpenHashSet.this.keys.length;

            int i = this.cursor.index + 1;
            while (i < KTypeOpenHashSet.this.keys.length && Intrinsics.equalsKTypeDefault(KTypeOpenHashSet.this.keys[i]))
            {
                i++;
            }

            if (i == max)
                return done();

            this.cursor.index = i;
            this.cursor.value = KTypeOpenHashSet.this.keys[i];
            return this.cursor;
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
    public <T extends KTypeProcedure<? super KType>> T forEach(final T procedure)
    {

        if (this.allocatedDefaultKey) {

            procedure.apply(Intrinsics.<KType> defaultKTypeValue());
        }

        final KType[] keys = this.keys;

        for (int i = 0; i < keys.length; i++)
        {
            if (!Intrinsics.equalsKTypeDefault(keys[i]))
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
    public Object[] toArray()
    /*! #end !*/
    {
        final KType[] cloned = Intrinsics.newKTypeArray(size());

        int count = 0;
        if (this.allocatedDefaultKey) {

            cloned[count++] = Intrinsics.<KType> defaultKTypeValue();
        }

        for (int i = 0; i < this.keys.length; i++)
            if (!Intrinsics.equalsKTypeDefault(this.keys[i]))
                cloned[count++] = this.keys[i];

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
            final/* #end */
            KTypeOpenHashSet<KType> cloned = (KTypeOpenHashSet<KType>) super.clone();
            cloned.keys = this.keys.clone();

            cloned.allocatedDefaultKey = this.allocatedDefaultKey;
            return cloned;
        }
        catch (final CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(final T predicate)
    {
        if (this.allocatedDefaultKey) {

            if (!predicate.apply(Intrinsics.<KType> defaultKTypeValue())) {

                return predicate;
            }
        }

        final KType[] keys = this.keys;

        for (int i = 0; i < keys.length; i++)
        {
            if (!Intrinsics.equalsKTypeDefault(keys[i]))
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
    public int removeAll(final KTypePredicate<? super KType> predicate)
    {
        final int before = this.size();

        if (this.allocatedDefaultKey) {

            if (predicate.apply(Intrinsics.<KType> defaultKTypeValue()))
            {
                this.allocatedDefaultKey = false;
            }
        }

        final KType[] keys = this.keys;

        for (int i = 0; i < keys.length;)
        {
            if (!Intrinsics.equalsKTypeDefault(keys[i]))
            {
                if (predicate.apply(keys[i]))
                {
                    this.assigned--;
                    shiftConflictingKeys(i);
                    // Repeat the check for the same i.
                    continue;
                }
            }
            i++;
        }

        return before - this.size();
    }

    /**
     * Create a set from a variable number of arguments or an array of <code>KType</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    public static <KType> KTypeOpenHashSet<KType> from(final KType... elements)
    {
        final KTypeOpenHashSet<KType> set = new KTypeOpenHashSet<KType>(
                (int) (elements.length * (1 + KTypeOpenHashSet.DEFAULT_LOAD_FACTOR)));
        set.add(elements);
        return set;
    }

    /**
     * Create a set from elements of another container.
     */
    public static <KType> KTypeOpenHashSet<KType> from(final KTypeContainer<KType> container)
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
     * Returns a new object with no key perturbations (see
     * {@link #computePerturbationValue(int)}). Only use when sure the container will not
     * be used for direct copying of keys to another hash container.
     */
    public static <KType> KTypeOpenHashSet<KType> newInstanceWithoutPerturbations()
    {
        return new KTypeOpenHashSet<KType>() {
            @Override
            protected int computePerturbationValue(final int capacity) {
                return 0;
            }
        };
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor).
     */
    public static <KType> KTypeOpenHashSet<KType> newInstanceWithCapacity(final int initialCapacity, final float loadFactor)
    {
        return new KTypeOpenHashSet<KType>(initialCapacity, loadFactor);
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor). The returned instance will have enough initial
     * capacity to hold <code>expectedSize</code> elements without having to resize.
     */
    public static <KType> KTypeOpenHashSet<KType> newInstanceWithExpectedSize(final int expectedSize)
    {
        return KTypeOpenHashSet.newInstanceWithExpectedSize(expectedSize, KTypeOpenHashSet.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Returns a new object of this class with no need to declare generic type (shortcut
     * instead of using a constructor). The returned instance will have enough initial
     * capacity to hold <code>expectedSize</code> elements without having to resize.
     */
    public static <KType> KTypeOpenHashSet<KType> newInstanceWithExpectedSize(final int expectedSize, final float loadFactor)
    {
        return KTypeOpenHashSet.newInstanceWithCapacity((int) (expectedSize / loadFactor) + 1, loadFactor);
    }
}
