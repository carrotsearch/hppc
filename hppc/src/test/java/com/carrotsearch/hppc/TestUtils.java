package com.carrotsearch.hppc;

import static org.junit.Assert.*;
import java.util.Arrays;

/**
 * Test utilities.
 */
public abstract class TestUtils
{
    private final static float delta = 0;

    // no instances.
    private TestUtils() {}

    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static Object [] reverse(Object [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            Object t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }

    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static byte [] reverse(byte [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            byte t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }
    
    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static char [] reverse(char [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            char t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }

    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static short [] reverse(short [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            short t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }

    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static int [] reverse(int [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            int t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }
    
    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static float [] reverse(float [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            float t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }
    
    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static double [] reverse(double [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            double t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }

    /**
     * Reverse the order of elements in an array. Returns the <code>array</code> argument
     * for easier chaining.
     */
    public static long [] reverse(long [] array)
    {
        for (int i = 0, mid = array.length / 2, j = array.length - 1; i < mid; i++, j--)
        {
            long t = array[i];
            array[i] = array[j];
            array[j] = t;
        }
        return array;
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(Object [] array, Object... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(double [] array, double... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(elements, array, delta);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(float [] array, float... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(elements, array, delta);
    }
    
    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(int [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(long [] array, long... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(short [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(short [] array, short... elements)
    {
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(byte [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(byte [] array, byte... elements)
    {
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(char [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertListEquals(char [] array, char... elements)
    {
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(Object [] array, Object... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(double [] array, double... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(elements, array, delta);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(float [] array, float... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(elements, array, delta);
    }
    
    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(int [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        Arrays.sort(elements);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(long [] array, long... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(elements, array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(short [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(byte [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Check if the array's content is identical to a given sequence of elements.
     */
    public static void assertSortedListEquals(char [] array, int... elements)
    {
        assertEquals(elements.length, array.length);
        Arrays.sort(array);
        assertArrayEquals(newArray(array, elements), array);
    }

    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    public static <T> T [] newArray(T [] arrayType, @SuppressWarnings("unchecked") T... elements)
    {
        return elements;
    }

    /**
     * Create a new array of ints.
     */
    public static int [] newArray(int [] arrayType, int... elements)
    {
        return elements;
    }
    
    /**
     * Create a new array of doubles.
     */
    public static double [] newArray(double [] arrayType, double... elements)
    {
        return elements;
    }

    /**
     * Create a new array of float.
     */
    public static float [] newArray(float [] arrayType, float... elements)
    {
        return elements;
    }
    
    /**
     * Create a new array of longs.
     */
    public static long [] newArray(long [] arrayType, long... elements)
    {
        return elements;
    }

    /**
     * Create a new array of shorts.
     */
    public static short [] newArray(short [] arrayType, int... elements)
    {
        final short [] result = new short [elements.length];
        for (int i = 0; i < elements.length; i++)
        {
            org.junit.Assert.assertTrue(
                elements[i] >= Short.MIN_VALUE && elements[i] <= Short.MAX_VALUE);
            result[i] = (short) elements[i];
        }
        return result;
    }

    /**
     * Create a new array of chars.
     */
    public static char [] newArray(char [] arrayType, int... elements)
    {
        final char [] result = new char [elements.length];
        for (int i = 0; i < elements.length; i++)
        {
            org.junit.Assert.assertTrue(
                elements[i] >= Character.MIN_VALUE && elements[i] <= Character.MAX_VALUE);
            result[i] = (char) elements[i];
        }
        return result;
    }
    
    /**
     * Create a new array of bytes.
     */
    public static byte [] newArray(byte [] arrayType, int... elements)
    {
        final byte [] result = new byte [elements.length];
        for (int i = 0; i < elements.length; i++)
        {
            org.junit.Assert.assertTrue(
                elements[i] >= Byte.MIN_VALUE && elements[i] <= Byte.MAX_VALUE);
            result[i] = (byte) elements[i];
        }
        return result;
    }

    /** Override for generated templates. */
    public static void assertEquals2(double a, double b)
    {
        org.junit.Assert.assertEquals(a, b, delta);
    }
    
    /** Override for generated templates. */
    public static void assertEquals2(float a, float b)
    {
        org.junit.Assert.assertEquals(a, b, delta);
    }

    /** Override for generated templates. */
    public static void assertEquals2(Object a, Object b)
    {
        org.junit.Assert.assertEquals(a, b);
    }
}
