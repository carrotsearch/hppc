package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import org.assertj.core.api.Assertions;
import org.junit.*;

/**
 * Unit tests for {@link KTypeStack}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeStackTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeStack<KType> stack;

    /* */
    @Before
    public void initialize()
    {
        stack = new KTypeStack<>();
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
        stack.push(key1);
        assertEquals(1, stack.size());
        assertEquals2(key1, stack.peek());
        assertEquals2(key1, stack.pop());
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testPush2()
    {
        stack.push(key1, key3);
        assertEquals(2, stack.size());
        assertEquals2(key3, stack.peek());
        assertEquals2(key1, stack.get(0));
        assertEquals2(key3, stack.get(1));
        assertEquals2(key3, stack.pop());
        assertEquals2(key1, stack.pop());
        assertEquals(0, stack.size());
    }

    /* */
    @Test
    public void testPushArray()
    {
        stack.push(asArray(1, 2, 3, 4), 1, 2);
        assertEquals(2, stack.size());
        assertEquals2(key2, stack.get(0));
        assertEquals2(key3, stack.get(1));
    }
    
    /* */
    @Test
    public void testAddAllPushAll()
    {
        KTypeArrayList<KType> list2 = new KTypeArrayList<>();
        list2.add(asArray(0, 1, 2));

        stack.addAll(list2);
        stack.pushAll(list2);

        assertListEquals(stack.toArray(), 0, 1, 2, 0, 1, 2);
    }
    
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /* */
    @Test
    public void testNullify()
    {
        stack.push(asArray(1, 2, 3, 4));
        stack.pop();
        stack.discard();
        stack.discard(2);
        assertEquals(0, stack.size());

        /* 
         * Cleanup only for the generic version (to allow GCing of references). 
         */
        for (int i = 0; i < stack.buffer.length; i++)
        {
            assertEquals2(Intrinsics.<KType> empty(), stack.buffer[i]);
        }
    }
    /*! #end !*/

    /* */
    @Test
    public void testDiscard()
    {
        stack.push(key1, key3);
        assertEquals(2, stack.size());

        stack.discard();
        assertEquals(1, stack.size());

        stack.push(key4);
        assertEquals(2, stack.size());

        assertEquals2(1, stack.get(0));
        assertEquals2(4, stack.get(1));

        stack.discard(2);
        assertEquals(0, stack.size());
    }
    
    /* */
    @Test(expected = AssertionError.class)
    public void testGetAssertions()
    {
        stack.push(key1);
        stack.pop();
        stack.get(0);
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testDiscardAssertions()
    {
        stack.push(key1);
        stack.discard(2);
    }

    /* */
    @Test
    public void testHashCodeEquals()
    {
        KTypeStack<KType> s0 = new KTypeStack<>();
        assertEquals(1, s0.hashCode());

        KTypeStack<KType> s1 = KTypeStack.from(key1, key2, key3);
        KTypeStack<KType> s2 = KTypeStack.from(key1, key2, key3);

        assertEquals(s1.hashCode(), s2.hashCode());
        assertEquals(s1, s2);

        s1 = KTypeStack.from(key1, key2);
        assertFalse(s1.equals(s2));
    }

    /* */
    @Test
    public void testHashCodeEqualsWithOtherContainer()
    {
        KTypeStack<KType> s1 = KTypeStack.from(key1, key2, key3);
        KTypeArrayList<KType> s2 = KTypeArrayList.from(key1, key2, key3);

        assertEquals(s1.hashCode(), s2.hashCode());
        assertTrue(s1.equalElements(s2));
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        KTypeStack<Integer> l1 = KTypeStack.from(1, 2, 3);
        Integer[] result = l1.toArray(Integer.class);
        assertArrayEquals(new Integer [] {1, 2, 3}, result); // dummy
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArray()
    {
        KTypeStack<Integer> l1 = KTypeStack.from(1, 2, 3);
        Object[] result = l1.toArray();
        assertArrayEquals(new Object [] {1, 2, 3}, result); // dummy
    }
    /*! #end !*/

    /* */
    @Test
    public void testClone()
    {
        stack.push(key1, key2, key3);

        KTypeStack<KType> cloned = stack.clone();
        cloned.removeAll(key1);

        assertSortedListEquals(stack.toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.toArray(), key2, key3);
    }

    /* */
    @Test
    public void testToString()
    {
        assertEquals("[" 
            + key1 + ", "
            + key2 + ", "
            + key3 + "]", KTypeStack.from(key1, key2, key3).toString());
    }
    
    /* */
    @Test
    public void testEqualsSameClass()
    {
        KTypeStack<KType> l1 = KTypeStack.from(k1, k2, k3);
        KTypeStack<KType> l2 = KTypeStack.from(k1, k2, k3);
        KTypeStack<KType> l3 = KTypeStack.from(k1, k3, k2);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeStack<KType> {
        };

        KTypeStack<KType> l1 = KTypeStack.from(k1, k2, k3);
        KTypeStack<KType> l2 = new Sub();
        KTypeStack<KType> l3 = new Sub();
        l2.addAll(l1);
        l3.addAll(l1);

        Assertions.assertThat(l2).isEqualTo(l3);
        Assertions.assertThat(l1).isNotEqualTo(l2);
        
        KTypeArrayList<KType> l4 = new KTypeArrayList<>();
        l4.addAll(l1);
        Assertions.assertThat(l1).isNotEqualTo(l4);
        Assertions.assertThat(l1.equalElements(l4)).isTrue();
    }        
}
