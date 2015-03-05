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

    /**
     * Default implementation of copying to an array.
     */
    @Override
    /*!#if ($TemplateOptions.KTypePrimitive)
    public KType [] toArray()
       #else !*/
    @SuppressWarnings("unchecked")
    public KType [] toArray(Class<? super KType> clazz)
    /*! #end !*/
    {
        final int size = size();
        final KType [] array = 
        /*!#if ($TemplateOptions.KTypePrimitive) 
           new KType [size];   
           #else !*/
           (KType []) java.lang.reflect.Array.newInstance(clazz, size);
        /*!#end !*/

        int i = 0;
        for (KTypeCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    @Override
    public Object[] toArray()
    {
        final Object [] array = new Object [size()];
        int i = 0;
        for (KTypeCursor<KType> c : this)
        {
            array[i++] = c.value;
        }
        return array;
    }
    /* #end */

    /**
     * Convert the contents of this container to a human-friendly string.
     */
    @Override
    public String toString()
    {
        return Arrays.toString(this.toArray());
    }
}
