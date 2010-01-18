package com.carrotsearch.hppc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.carrotsearch.hppc.TestUtils.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

/**
 * Unit tests for {@link ObjectStack}.
 */
public class ObjectStackTest
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectStack<Object> stack;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* */
    @Before
    public void initialize()
    {
        stack = new ObjectStack<Object>();
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testPush1()
    {
        stack.push(/* intrinsic:ktypecast */ 1);
        assertEquals(1, stack.size());
        assertEquals2(/* intrinsic:ktypecast */ 1, stack.peek());
        assertEquals2(/* intrinsic:ktypecast */ 1, stack.pop());
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testPush2()
    {
        stack.push(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 3);
        assertEquals(2, stack.size());
        assertEquals2(/* intrinsic:ktypecast */ 3, stack.peek());
        assertEquals2(/* intrinsic:ktypecast */ 1, stack.get(0));
        assertEquals2(/* intrinsic:ktypecast */ 3, stack.get(1));
        assertEquals2(/* intrinsic:ktypecast */ 3, stack.pop());
        assertEquals2(/* intrinsic:ktypecast */ 1, stack.pop());
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testPushArray()
    {
        stack.push(newArray(stack.buffer, 1, 2, 3, 4), 1, 2);
        assertEquals(2, stack.size());
        assertEquals2(/* intrinsic:ktypecast */ 2, stack.get(0));
        assertEquals2(/* intrinsic:ktypecast */ 3, stack.get(1));
    }

    /* */
    @Test
    public void testNullify()
    {
        stack.pushv(newArray(stack.buffer, 1, 2, 3, 4));
        stack.pop();
        stack.discard();
        stack.discard(2);
        assertEquals(0, stack.size());

        /* 
         * Nullification only for the generic version (to allow GC-ing of
         * references. 
         */

        /* removeIf:primitive */
        for (int i = 0; i < stack.buffer.length; i++)
        {
            assertEquals2(Intrinsics.defaultKTypeValue(), stack.buffer[i]);
        }
        /* end:removeIf */
    }

    /* */
    @Test
    public void testDiscard()
    {
        stack.push(
            /* intrinsic:ktypecast */ 1,
            /* intrinsic:ktypecast */ 3);
        assertEquals(2, stack.size());

        stack.discard();
        assertEquals(1, stack.size());

        stack.push(/* intrinsic:ktypecast */ 4);
        assertEquals(2, stack.size());

        assertEquals2(1, /* intrinsic:ktypecast */ stack.get(0));
        assertEquals2(4, /* intrinsic:ktypecast */ stack.get(1));

        stack.discard(2);
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testGetAssertions()
    {
        boolean passed = true;
        try
        {
            stack.push(/* intrinsic:ktypecast */ 0);
            stack.pop();
            stack.get(0);
            passed = false;
        }
        catch (AssertionError e)
        {
            // Expected.
        }

        assertTrue(passed);
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testDiscardAssertions()
    {
        stack.push(/* intrinsic:ktypecast */ 0);
        stack.discard(2);
    }
}
