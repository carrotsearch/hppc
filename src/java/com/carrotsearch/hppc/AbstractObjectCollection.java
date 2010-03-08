package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.predicates.ObjectPredicate;

/**
 * Common superclass for collections. 
 */
abstract class AbstractObjectCollection<KType> implements ObjectCollection<KType>
{
    /**
     * Add all elements from an iterable cursor.
     */
    @Override
    public int addAll(Iterable<? extends ObjectCursor<? extends KType>> c)
    {
        int added = 0;
        for (ObjectCursor<? extends KType> cursor : c)
        {
            added += add(cursor.value);
        }
        return added;
    }

    /**
     * Default implementation uses a predicate for removal.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int removeAll(final ObjectLookupContainer<? extends KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        final ObjectContainer<KType> c2 = (ObjectContainer<KType>) c;
        return this.removeAll(new ObjectPredicate<KType>()
        {
            public boolean apply(KType k)
            {
                return c2.contains(k);
            }
        });
    }

    /**
     * Default implementation uses a predicate for retaining.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int retainAll(final ObjectLookupContainer<? extends KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        final ObjectContainer<KType> c2 = (ObjectContainer<KType>) c;
        return this.removeAll(new ObjectPredicate<KType>()
        {
            public boolean apply(KType k)
            {
                return !c2.contains(k);
            }
        });
    }

    /**
     * Default implementation redirects to {@link #removeAll(ObjectPredicate)}
     * and negates the predicate.
     */
    @Override
    public int retainAll(final ObjectPredicate<? super KType> predicate)
    {
        return removeAll(new ObjectPredicate<KType>()
        {
            public boolean apply(KType value)
            {
                return !predicate.apply(value);
            };
        });
    }

    /**
     * Default implementation of copying to an array.
     */
    public KType [] toArray()
    {
        final int size = size();
        final KType [] array = Intrinsics.<KType[]>newKTypeArray(size);
        int i = 0;
        for (ObjectCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }
}
