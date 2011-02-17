package com.carrotsearch.hppc;

import java.util.Arrays;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.predicates.ObjectPredicate;

/**
 * Common superclass for collections. 
 */
abstract class AbstractObjectCollection<KType> implements ObjectCollection<KType>
{
    /**
     * Default implementation uses a predicate for removal.
     */
    /* removeIf:primitiveKType */
    @SuppressWarnings("unchecked")
    /* end:removeIf */
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
    /* removeIf:primitiveKType */
    @SuppressWarnings("unchecked")
    /* end:removeIf */
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
    @Override
    /* replaceIf:primitive 
    public KType [] toArray() */
    @SuppressWarnings("unchecked")
    public KType [] toArray(Class<KType> clazz)
    /* end:replaceIf */
    {
        final int size = size();
        final KType [] array = 
        /* replaceIf:primitive
           Intrinsics.newKTypeArray(size);
         */
           (KType []) java.lang.reflect.Array.newInstance(clazz, size);
        /* end:replaceIf */

        int i = 0;
        for (ObjectCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }
    
    /* removeIf:primitiveKType */
    @Override
    public Object[] toArray()
    {
        final Object [] array = new Object [size()];
        int i = 0;
        for (ObjectCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }
    /* end:removeIf */

    /**
     * Convert the contents of this container to a human-friendly string.
     */
    @Override
    public String toString()
    {
        return Arrays.toString(this.toArray());
    }
}
