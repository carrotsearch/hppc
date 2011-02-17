package com.carrotsearch.hppc;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Various API expectations from generated classes.
 */
public class APIExpectationsTest
{
    public volatile int [] t1;

    @Test
    public void testPrimitiveToArray()
    {
        t1 = IntArrayList.from(1, 2, 3).toArray();
        t1 = IntStack.from(1, 2, 3).toArray();
        t1 = IntArrayDeque.from(1, 2, 3).toArray();
        t1 = IntOpenHashSet.from(1, 2, 3).toArray();

        t1 = IntObjectOpenHashMap.from(
            new int [] {1, 2}, new Long [] {1L, 2L}).keySet().toArray();
    }

    @Test
    public void testObjectToArray()
    {
        isObjectArray(ObjectArrayList.from(1, 2, 3).toArray());
        isObjectArray(ObjectStack.from(1, 2, 3).toArray());
        isObjectArray(ObjectArrayDeque.from(1, 2, 3).toArray());
        isObjectArray(ObjectOpenHashSet.from(1, 2, 3).toArray());

        isObjectArray(ObjectObjectOpenHashMap.from(
            new Integer [] {1, 2}, new Long [] {1L, 2L}).keySet().toArray());
    }

    @Test
    public void testWithClassToArray()
    {
        isIntegerArray(ObjectArrayList.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectStack.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectArrayDeque.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectOpenHashSet.from(1, 2, 3).toArray(Integer.class));

        isIntegerArray(ObjectObjectOpenHashMap.from(
            new Integer [] {1, 2}, new Long [] {1L, 2L}).keySet().toArray(Integer.class));
    }

    /**
     * Check if the array is indeed of Object component type.
     */
    private void isObjectArray(Object [] array)
    {
        assertEquals(Object.class, array.getClass().getComponentType());
    }
    
    /**
     * Check if the array is indeed of Integer component type.
     */
    private void isIntegerArray(Integer [] array)
    {
        assertEquals(Integer.class, array.getClass().getComponentType());
    }
}
