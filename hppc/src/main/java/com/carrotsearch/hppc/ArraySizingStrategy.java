package com.carrotsearch.hppc;

/**
 * Resizing (growth) strategy for array-backed buffers.
 */
public interface ArraySizingStrategy
{
    /**
     * Array sizing strategies may require that the initial size fulfills
     * certain constraints (is a prime or a power of two, for example). This
     * method must return the first size that fulfills these conditions
     * and is greater or equal to <code>capacity</code>.
     */
    int round(int capacity);
    
    /**
     * @param currentBufferLength Current size of the array (buffer). This number
     *  should comply with the strategy's policies (it is a result of initial rounding
     *  or further growths). It can also be zero, indicating the growth from an empty
     *  buffer.
     *
     * @param elementsCount Number of elements stored in the buffer.
     * 
     * @param expectedAdditions Expected number of additions (resize hint).
     * 
     * @return Must return a new size at least as big as to hold
     *         <code>elementsCount + expectedAdditions</code>.
     */
    int grow(int currentBufferLength, int elementsCount, int expectedAdditions);
}
