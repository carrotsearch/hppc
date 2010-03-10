package com.carrotsearch.hppc;

import java.util.List;
import java.util.RandomAccess;

/**
 * An indexed container provides random access to elements based on an
 * <code>index</code>. Indexes are zero-based.
 */
public interface ObjectIndexedContainer<KType> extends ObjectCollection<KType>, RandomAccess
{
    /**
     * Removes the first element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public int removeFirstOccurrence(KType e1);
    
    /**
     * Removes the last element that equals <code>e1</code>, returning its 
     * deleted position or <code>-1</code> if the element was not found.   
     */
    public int removeLastOccurrence(KType e1);
    
    /**
     * Returns the index of the first occurrence of the specified element in this list, 
     * or -1 if this list does not contain the element.
     */
    public int indexOf(KType e1);
    
    /**
     * Returns the index of the last occurrence of the specified element in this list, 
     * or -1 if this list does not contain the element.
     */
    public int lastIndexOf(KType e1);

    /**
     * Adds an element to the end of this container (the last index is incremented by one).
     */
    public void add(KType e1);
    
    /**
     * Inserts the specified element at the specified position in this list.
     * 
     * @param index The index at which the element should be inserted, shifting
     * any existing and subsequent elements to the right.
     */
    public void insert(int index, KType e1);

    /**
     * Replaces the element at the specified position in this list 
     * with the specified element. 
     * 
     * @return Returns the previous value in the list.
     */
    public KType set(int index, KType e1);

    /**
     * @return Returns the element at index <code>index</code> from the list.
     */
    public KType get(int index);
    
    /**
     * Removes the element at the specified position in this list and returns it.
     * 
     * <p><b>Careful.</b> Do not confuse this method with the overridden signature in
     * Java Collections ({@link List#remove(Object)}). Use various overloads
     * of {@link #removeAll},
     * {@link #removeFirstOccurrence} or 
     * {@link #removeLastOccurrence} for this purpose.</p> 
     */
    public KType remove(int index);

    /**
     * Removes from this list all of the elements whose index is between 
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
     */
    public void removeRange(int fromIndex, int toIndex);
}
