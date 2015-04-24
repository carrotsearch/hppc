package com.carrotsearch.hppc;

import java.util.Arrays;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;

/**
 * Common superclass for collections. 
 */
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
abstract class AbstractKTypeCollection<KType> 
  implements /*! #if ($templateonly) !*/ Intrinsics.EqualityFunction, /*! #end !*/
             KTypeCollection<KType>
{
  /**
   * Default implementation uses a predicate for removal.
   */
  @Override
  public int removeAll(final KTypeLookupContainer<? super KType> c) {
    // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
    return this.removeAll(new KTypePredicate<KType>() {
      public boolean apply(KType k) {
        return c.contains(k);
      }
    });
  }

  /**
   * Default implementation uses a predicate for retaining.
   */
  @Override
  public int retainAll(final KTypeLookupContainer<? super KType> c) {
    // We know c holds sub-types of KType and we're not modifying c, so go unchecked.
    return this.removeAll(new KTypePredicate<KType>() {
      public boolean apply(KType k) {
        return !c.contains(k);
      }
    });
  }

  /**
   * Default implementation redirects to {@link #removeAll(KTypePredicate)} and
   * negates the predicate.
   */
  @Override
  public int retainAll(final KTypePredicate<? super KType> predicate) {
    return removeAll(new KTypePredicate<KType>() {
      public boolean apply(KType value) {
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
  public Object[] toArray()
  /*! #end !*/
  {
    KType[] array = Intrinsics.<KType> newArray(size());
    int i = 0;
    for (KTypeCursor<KType> c : this) {
      array[i++] = c.value;
    }
    return array;
  }

  /*!#if ($TemplateOptions.KTypeGeneric) !*/
  public <T> T [] toArray(Class<T> componentClass) {
    final int size = size();
    final T[] array = (T[]) java.lang.reflect.Array.newInstance(componentClass, size);
    int i = 0;
    for (KTypeCursor<KType> c : this) {
      array[i++] = (T) c.value;
    }
    return array;
  }
  /*! #end !*/

  /**
   * Convert the contents of this container to a human-friendly string.
   */
  @Override
  public String toString() {
    return Arrays.toString(this.toArray());
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
