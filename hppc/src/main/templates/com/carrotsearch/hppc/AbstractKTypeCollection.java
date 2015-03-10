package com.carrotsearch.hppc;

import java.util.Arrays;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;

/**
 * Common superclass for collections. 
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
abstract class AbstractKTypeCollection<KType> implements KTypeCollection<KType>
{
    /**
     * Default implementation uses a predicate for removal.
     */
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings("unchecked")
    /* #end */
    @Override
    public int removeAll(final KTypeLookupContainer<? extends KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        final KTypeContainer<KType> c2 = (KTypeContainer<KType>) c;
        return this.removeAll(new KTypePredicate<KType>()
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
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SuppressWarnings("unchecked")
    /* #end */
    @Override
    public int retainAll(final KTypeLookupContainer<? extends KType> c)
    {
        // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
        final KTypeContainer<KType> c2 = (KTypeContainer<KType>) c;
        return this.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType k)
            {
                return !c2.contains(k);
            }
        });
    }

    /**
     * Default implementation redirects to {@link #removeAll(KTypePredicate)}
     * and negates the predicate.
     */
    @Override
    public int retainAll(final KTypePredicate<? super KType> predicate)
    {
        return removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType value)
            {
                return !predicate.apply(value);
            };
        });
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    /**
     * Default implementation of copying to an array.
     */
    @Override
    @SuppressWarnings("unchecked")
    public KType/*keep*/[] toArray(Class<? super KType> clazz)
    {
        final int size = size();
        final KType/*keep*/[] array = (KType/*keep*/[]) java.lang.reflect.Array.newInstance(clazz, size);

        int i = 0;
        for (KTypeCursor<KType> c : this)
        {
            array[i++] = c.value;
        }

        return array;
    }
    /* #end */

    @Override
    public KType[] toArray()
    {
        final KType [] array = Intrinsics.newKTypeArray(size());
        int i = 0;
        for (KTypeCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }

    /**
     * Convert the contents of this container to a human-friendly string.
     */
    @Override
    public String toString()
    {
        return Arrays.toString(this.toArray());
    }
}
