package com.carrotsearch.hppc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 */
public class AbstractIteratorTest
{
    public static class RangeIterator extends AbstractIterator<Integer>
    {
        int start;
        int count;

        public RangeIterator(int start, int count)
        {
            this.start = start;
            this.count = count;
        }

        @Override
        protected Integer fetch()
        {
            if (count == 0)
            {
                return done();
            }

            count--;
            return start++;
        }
    }

    @Test
    public void testEmpty()
    {
        RangeIterator i = new RangeIterator(1, 0);
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyExceptionOnNext()
    {
        RangeIterator i = new RangeIterator(1, 0);
        i.next();
    }

    @Test
    public void testNonEmpty()
    {
        RangeIterator i = new RangeIterator(1, 1);
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        i.next();
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
        try
        {
            i.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
            // expected.
        }
    }

    @Test
    public void testValuesAllRight()
    {
        assertEquals(Arrays.asList(1), addAll(new RangeIterator(1, 1)));
        assertEquals(Arrays.asList(1, 2), addAll(new RangeIterator(1, 2)));
        assertEquals(Arrays.asList(1, 2, 3), addAll(new RangeIterator(1, 3)));
    }

    private static <T> List<T> addAll(Iterator<T> i)
    {
        List<T> t = new ArrayList<T>();
        while (i.hasNext())
            t.add(i.next());
        return t;
    }
}
