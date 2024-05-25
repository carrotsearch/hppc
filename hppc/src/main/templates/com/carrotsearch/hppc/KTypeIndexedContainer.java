package com.carrotsearch.hppc;

import java.util.RandomAccess;
/* #if ($TemplateOptions.KTypeGeneric) */
import java.util.stream.Stream;
/* #end */
/*! #if ($TemplateOptions.isKTypeAnyOf("INT"))
import java.util.stream.IntStream;
#end !*/
/*! #if ($TemplateOptions.isKTypeAnyOf("LONG"))
import java.util.stream.LongStream;
#end !*/
/*! #if ($TemplateOptions.isKTypeAnyOf("DOUBLE"))
import java.util.stream.DoubleStream;
#end !*/

/**
 * An indexed container provides random access to elements based on an
 * <code>index</code>. Indexes are zero-based.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeIndexedContainer<KType> extends KTypeCollection<KType>, RandomAccess {
  /**
   * Removes the first element that equals <code>e1</code>, returning whether
   * an element has been removed.
   */
  public boolean removeElement(KType e1);

  /**
   * Removes the first element that equals <code>e1</code>, returning its
   * deleted position or <code>-1</code> if the element was not found.
   */
  public int removeFirst(KType e1);

  /**
   * Removes the last element that equals <code>e1</code>, returning its deleted
   * position or <code>-1</code> if the element was not found.
   */
  public int removeLast(KType e1);

  /**
   * Returns the index of the first occurrence of the specified element in this
   * list, or -1 if this list does not contain the element.
   */
  public int indexOf(KType e1);

  /**
   * Returns the index of the last occurrence of the specified element in this
   * list, or -1 if this list does not contain the element.
   */
  public int lastIndexOf(KType e1);

  /**
   * Adds an element to the end of this container (the last index is incremented
   * by one).
   */
  public void add(KType e1);

  /**
   * Inserts the specified element at the specified position in this list.
   * 
   * @param index
   *          The index at which the element should be inserted, shifting any
   *          existing and subsequent elements to the right.
   */
  public void insert(int index, KType e1);

  /**
   * Replaces the element at the specified position in this list with the
   * specified element.
   * 
   * @return Returns the previous value in the list.
   */
  public KType set(int index, KType e1);

  /**
   * @return Returns the element at index <code>index</code> from the list.
   */
  public KType get(int index);

  /**
   * Removes the element at the specified position in this container and returns
   * it.
   * 
   * @see #removeFirst
   * @see #removeLast
   * @see #removeAll
   */
  public KType removeAt(int index);

  /**
   * Removes and returns the last element of this container.
   * This container must not be empty.
   */
  public KType removeLast();

  /**
   * Removes from this container all of the elements with indexes between
   * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
   */
  public void removeRange(int fromIndex, int toIndex);

  /**
   * Returns this container elements as a stream.
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  public Stream<KType> stream();
  /* #end */
  /*! #if ($TemplateOptions.isKTypeAnyOf("INT")) public IntStream stream(); #end !*/
  /*! #if ($TemplateOptions.isKTypeAnyOf("LONG")) public LongStream stream(); #end !*/
  /*! #if ($TemplateOptions.isKTypeAnyOf("DOUBLE")) public DoubleStream stream(); #end !*/

  /**
   * Sorts the elements in this container and returns this container.
   */
  public KTypeIndexedContainer<KType> sort();

  /**
   * Reverses the elements in this container and returns this container.
   */
  public KTypeIndexedContainer<KType> reverse();
}
