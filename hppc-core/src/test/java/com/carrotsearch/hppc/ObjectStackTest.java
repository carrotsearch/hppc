package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import org.junit.*;
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
    public void testAddAllPushAll()
    {
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 1, 2));

        stack.addAll(list2);
        stack.pushAll(list2);

        assertListEquals(stack.toArray(), 
            0, 1, 2, 0, 1, 2);
    }

    /* */
    @Test
    public void testNullify()
    {
        stack.push(newArray(stack.buffer, 1, 2, 3, 4));
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

    /* */
    @Test
    public void testHashCodeEquals()
    {
        ObjectStack<Integer> l0 = ObjectStack.from();
        assertEquals(1, l0.hashCode());
        assertEquals(l0, ObjectArrayList.from());

        ObjectStack<Integer> l1 = ObjectStack.from(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2, 
            /* intrinsic:ktypecast */ 3);

        ObjectStack<Integer> l2 = ObjectStack.from(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2, 
            /* intrinsic:ktypecast */ 3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        l1 = ObjectStack.from(
            /* intrinsic:ktypecast */ 1,
            /* intrinsic:ktypecast */ 3);            
        assertFalse(l1.equals(l2));
    }

    /* */
    @Test
    public void testHashCodeEqualsWithOtherContainer()
    {
        ObjectStack<Integer> l1 = ObjectStack.from(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2, 
            /* intrinsic:ktypecast */ 3);

        ObjectArrayList<Integer> l2 = ObjectArrayList.from(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2, 
            /* intrinsic:ktypecast */ 3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }
    
    /* removeIf:primitive */
    @Test
    public void testToArrayWithClass()
    {
        ObjectStack<Integer> l1 = ObjectStack.from(1, 2, 3);
        Integer[] result = l1.toArray(Integer.class);
        assertArrayEquals(new Integer [] {1, 2, 3}, result); // dummy
    }

    @Test
    public void testToArray()
    {
        ObjectStack<Integer> l1 = ObjectStack.from(1, 2, 3);
        Object[] result = l1.toArray();
        assertArrayEquals(new Object [] {1, 2, 3}, result); // dummy
    }
    /* end:removeIf */
}
