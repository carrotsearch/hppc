package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import org.junit.*;

import com.carrotsearch.hppc.predicates.KTypePredicate;

/**
 * Unit tests for {@link KTypeArrayList}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayListTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayList<KType> list;

    /* */
    @Before
    public void initialize()
    {
        list = KTypeArrayList.newInstance();
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (list != null)
        {
            for (int i = list.elementsCount; i < list.buffer.length; i++)
                assertTrue(Intrinsics.<KType> defaultKTypeValue() == list.buffer[i]);
        }
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        assertEquals(0, list.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        list.add(key1, key2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddTwoArgs()
    {
        list.add(key1, key2);
        list.add(key3, key4);
        assertListEquals(list.toArray(), 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddArray()
    {
        list.add(asArray(0, 1, 2, 3), 1, 2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testAddVarArg()
    {
        list.add(asArray(0, 1, 2, 3));
        list.add(key4, key5, key6, key7);
        assertListEquals(list.toArray(), 0, 1, 2, 3, 4, 5, 6, 7);
    }

    /* */
    @Test
    public void testAddAll()
    {
        KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        list.addAll(list2);
        list.addAll(list2);

        assertListEquals(list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAddAll_subclass()
    {
        class A {
        }

        class B extends A {
        }

        KTypeArrayList<B> list2 = new KTypeArrayList<B>();
        list2.add(new B());

        KTypeArrayList<A> list3 = new KTypeArrayList<A>();
        list3.add(new B());
        list3.add(new A());
        list3.addAll(list2);
        assertEquals(3, list3.size());
    }
    /*! #end !*/

    /* */
    @Test
    public void testInsert()
    {
        list.insert(0, k1);
        list.insert(0, k2);
        list.insert(2, k3);
        list.insert(1, k4);

        assertListEquals(list.toArray(), 2, 4, 1, 3);
    }

    /* */
    @Test
    public void testSet()
    {
        list.add(asArray(0, 1, 2));

        assertEquals2(0, list.set(0, k3));
        assertEquals2(1, list.set(1, k4));
        assertEquals2(2, list.set(2, k5));

        assertListEquals(list.toArray(), 3, 4, 5);
    }
    
    /* */
    @Test
    public void testRemove()
    {
        list.add(asArray(0, 1, 2, 3, 4));

        list.remove(0);
        list.remove(2);
        list.remove(1);

        assertListEquals(list.toArray(), 1, 4);
    }

    /* */
    @Test
    public void testRemoveRange()
    {
        list.add(asArray(0, 1, 2, 3, 4));

        list.removeRange(0, 2);
        assertListEquals(list.toArray(), 2, 3, 4);

        list.removeRange(2, 3);
        assertListEquals(list.toArray(), 2, 3);
        
        list.removeRange(1, 1);
        assertListEquals(list.toArray(), 2, 3);

        list.removeRange(0, 1);
        assertListEquals(list.toArray(), 3);
    }

    /* */
    @Test
    public void testRemoveFirstLast()
    {
        list.add(asArray(0, 1, 2, 1, 0));

        assertEquals(-1, list.removeFirstOccurrence(k5));
        assertEquals(-1, list.removeLastOccurrence(k5));
        assertListEquals(list.toArray(), 0, 1, 2, 1, 0);

        assertEquals(1, list.removeFirstOccurrence(k1));
        assertListEquals(list.toArray(), 0, 2, 1, 0);
        assertEquals(3, list.removeLastOccurrence(k0));
        assertListEquals(list.toArray(), 0, 2, 1);
        assertEquals(0, list.removeLastOccurrence(k0));
        assertListEquals(list.toArray(), 2, 1);
        assertEquals(-1, list.removeLastOccurrence(k0));
        
        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        list.clear();
        list.add(newArray(k0, null, k2, null, k0));
        assertEquals(1, list.removeFirstOccurrence(null));
        assertEquals(2, list.removeLastOccurrence(null));
        assertListEquals(list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAll()
    {
        list.add(asArray(0, 1, 0, 1, 0));

        assertEquals(0, list.removeAllOccurrences(k2));
        assertEquals(3, list.removeAllOccurrences(k0));
        assertListEquals(list.toArray(), 1, 1);

        assertEquals(2, list.removeAllOccurrences(k1));
        assertTrue(list.isEmpty());

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        list.clear();
        list.add(newArray(k0, null, k2, null, k0));
        assertEquals(2, list.removeAllOccurrences(null));
        assertEquals(0, list.removeAllOccurrences(null));
        assertListEquals(list.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        list.add(asArray(0, 1, 2, 1, 0));

        KTypeOpenHashSet<KType> list2 = KTypeOpenHashSet.newInstance();
        list2.add(asArray(0, 2));

        assertEquals(3, list.removeAll(list2));
        assertEquals(0, list.removeAll(list2));

        assertListEquals(list.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        list.add(newArray(k0, k1, k2, k1, k4));

        assertEquals(3, list.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertListEquals(list.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        list.add(newArray(k0, k1, k2, k1, k0));

        assertEquals(2, list.retainAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertListEquals(list.toArray(), 1, 2, 1);
    }
    
    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        list.add(newArray(k0, k1, k2, k1, k4));

        final RuntimeException t = new RuntimeException(); 
        try
        {
            assertEquals(3, list.removeAll(new KTypePredicate<KType>()
            {
                public boolean apply(KType v)
                {
                    if (v == key2) throw t;
                    return v == key1;
                };
            }));
            fail();
        }
        catch (RuntimeException e)
        {
            // Make sure it's really our exception...
            if (e != t) throw e;
        }

        // And check if the list is in consistent state.
        assertListEquals(list.toArray(), 0, key2, key1, 4);
        assertEquals(4, list.size());
    }    
}
