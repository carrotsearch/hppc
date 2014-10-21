package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import org.junit.*;
import java.util.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.KTypePredicate;

/**
 * Unit tests for {@link KTypeOpenHashSet}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeOpenHashSetTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeOpenHashSet<KType> set;

    /* */
    @Before
    public void initialize()
    {
        set = KTypeOpenHashSet.newInstance();
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (set != null)
        {
            int occupied = 0;
            for (int i = 0; i < set.keys.length; i++)
            {
                if (Intrinsics.equalsKTypeDefault(set.keys[i]))
                {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    assertEquals2(Intrinsics.<KType>defaultKTypeValue(), set.keys[i]);
                    /*! #end !*/
                }
                else
                {
                    occupied++;
                }
            }

          if (set.allocatedDefaultKey) {

            //try to reach the key by contains()
            Assert.assertTrue(this.set.contains(Intrinsics.<KType>defaultKTypeValue()));

            //check slot
           Assert.assertEquals(-2, this.set.lslot());

            occupied++;
         }

            assertEquals(occupied, set.size());
        }
    }

    @Test
    public void testAddRemoveSameHashCollision()
    {
        // This test is only applicable to selected key types.
        Assume.assumeTrue(
            int[].class.isInstance(set.keys) ||
            long[].class.isInstance(set.keys) ||
            Object[].class.isInstance(set.keys));

        IntArrayList hashChain = TestUtils.generateMurmurHash3CollisionChain(0x1fff, 0x7e, 0x1fff /3);

        /*
         * Add all of the conflicting keys to a map. 
         */
        for (IntCursor c : hashChain)
            set.add(cast(c.value));

        assertEquals(hashChain.size(), set.size());
        
        /*
         * Add some more keys (random).
         */
        Random rnd = getRandom();
        IntSet chainKeys = IntOpenHashSet.from(hashChain);
        IntSet differentKeys = new IntOpenHashSet();
        while (differentKeys.size() < 500)
        {
            int k = rnd.nextInt();
            if (!chainKeys.contains(k) && !differentKeys.contains(k))
                differentKeys.add(k);
        }

        for (IntCursor c : differentKeys)
            set.add(cast(c.value));

        assertEquals(hashChain.size() + differentKeys.size(), set.size());

        /* 
         * Verify the map contains all of the conflicting keys.
         */
        for (IntCursor c : hashChain)
            assertTrue(set.contains(cast(c.value)));

        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertTrue(set.contains(cast(c.value)));

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (IntCursor c : hashChain)
            assertTrue(set.remove(cast(c.value)));

        assertEquals(differentKeys.size(), set.size());

        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertTrue(set.contains(cast(c.value)));        
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        assertTrue(set.add(key1));
        assertFalse(set.add(key1));
        assertEquals(1, set.size());
    }

    /* */
    @Test
    public void testAdd2()
    {
        set.add(key1, key1);
        assertEquals(1, set.size());
        assertEquals(1, set.add(key1, key2));
        assertEquals(2, set.size());
    }

    /* */
    @Test
    public void testAddVarArgs()
    {
        set.add(asArray(0, 1, 2, 1, 0));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        KTypeOpenHashSet<KType> set2 = new KTypeOpenHashSet<KType>();
        set2.add(asArray(1, 2));
        set.add(asArray(0, 1));

        assertEquals(1, set.addAll(set2));
        assertEquals(0, set.addAll(set2));

        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.add(asArray(0, 1, 2, 3, 4));

        assertTrue(set.remove(k2));
        assertFalse(set.remove(k2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            KTypeOpenHashSet<KType> set = new KTypeOpenHashSet<KType>(i);
            
            for (int j = 0; j < i; j++)
            {
                set.add(cast(j));
            }

            assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        this.set = new KTypeOpenHashSet<KType>(1, 1f);
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 1; i <= max; i++)
        {
            this.set.add(cast(i));
        }
        
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Non-existent key.
        this.set.remove(cast(max + 1));
        Assert.assertFalse(this.set.contains(cast(max + 1)));

        // Should not expand because we're replacing an existing element.
        Assert.assertFalse(this.set.add(cast(1)));
        Assert.assertEquals(max, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);

        // Remove from a full set.
        this.set.remove(cast(1));
        Assert.assertEquals(max - 1, this.set.size());
        Assert.assertEquals(capacity, this.set.keys.length);
    }

    
    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        set.add(asArray(0, 1, 2, 3, 4));

        KTypeOpenHashSet<KType> list2 = new KTypeOpenHashSet<KType>();
        list2.add(asArray(1, 3, 5));

        assertEquals(2, set.removeAll(list2));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        set.add(newArray(k0, k1, k2));

        assertEquals(1, set.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1;
            };
        }));

        assertSortedListEquals(set.toArray(), 0, key2);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        set.add(newArray(k0, k1, k2, k3, k4, k5));

        assertEquals(4, set.retainAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertSortedListEquals(set.toArray(), key1, key2);
    }

    /* */
    @Test
    public void testClear()
    {
        set.add(asArray(1, 2, 3));
        set.clear();
        checkTrailingSpaceUninitialized();
        assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        set.add(asArray(1, 2, 2, 3, 4));
        set.remove(k2);
        assertEquals(3, set.size());

        int count = 0;
        for (KTypeCursor<KType> cursor : set)
        {
            if (cursor.index == -1) {

                TestUtils.assertEquals2(Intrinsics.<KType>defaultKTypeValue(), cursor.value);
                count++;
                continue;
            }

            count++;
            assertTrue(set.contains(cursor.value));
            /* #if ($TemplateOptions.KTypeGeneric) */
            assertEquals2(cursor.value, set.lkey());
            /* #end */
        }
        assertEquals(count, set.size());

        set.clear();
        assertFalse(set.iterator().hasNext());
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        set.add((KType) null);
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertTrue(set.remove(null));
        assertEquals(0, set.size());
        assertFalse(set.contains(null));
    }
    /*! #end !*/
    
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new java.util.Random();
        final java.util.HashSet<KType> other = new java.util.HashSet<KType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                Integer key = rnd.nextInt(size);

                if (rnd.nextBoolean())
                {
                    other.add(cast(key));
                    set.add(cast(key));

                    assertTrue(set.contains(cast(key)));
                    assertEquals2(key, set.lkey());
                }
                else
                {
                    assertEquals(other.remove(key), set.remove(cast(key)));
                }

                assertEquals(other.size(), set.size());
            }
        }
    }
    /*! #end !*/

    /* */
    @Test
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public void testHashCodeEquals()
    {
        KTypeOpenHashSet<Integer> l0 = KTypeOpenHashSet.from();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, KTypeOpenHashSet.newInstance());

        KTypeOpenHashSet<KType> l1 = KTypeOpenHashSet.from(k1, k2, k3);
        KTypeOpenHashSet<KType> l2 = KTypeOpenHashSet.from(k1, k2);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }

    /* */
    @Test
    public void testHashCodeEqualsDifferentPerturbance()
    {
        KTypeOpenHashSet<KType> l0 = new KTypeOpenHashSet<KType>() {
            @Override
            protected int computePerturbationValue(int capacity)
            {
                return 0xDEADBEEF;
            }
        };

        KTypeOpenHashSet<KType> l1 = new KTypeOpenHashSet<KType>() {
            @Override
            protected int computePerturbationValue(int capacity)
            {
                return 0xCAFEBABE;
            }
        };
        
        assertEquals(0, l0.hashCode());
        assertEquals(l0.hashCode(), l1.hashCode());
        assertEquals(l0, l1);

        l0.add(newArray(k1, k2, k3));
        l1.add(newArray(k1, k2, k3));

        assertEquals(l0.hashCode(), l1.hashCode());
        assertEquals(l0, l1);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    @Test
    public void testHashCodeWithNulls()
    {
        KTypeOpenHashSet<KType> l1 = KTypeOpenHashSet.from(k1, null, k3);
        KTypeOpenHashSet<KType> l2 = KTypeOpenHashSet.from(k1, null);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testClone()
    {
        this.set.add(key1, key2, key3);

        KTypeOpenHashSet<KType> cloned = set.clone();
        cloned.removeAllOccurrences(key1);

        assertSortedListEquals(set.toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.toArray(), key2, key3);
    }
    
    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(
             int[].class.isInstance(set.keys)     ||
             short[].class.isInstance(set.keys)   ||
             byte[].class.isInstance(set.keys)    ||
             long[].class.isInstance(set.keys)    ||
             Object[].class.isInstance(set.keys));

        this.set.add(key1, key2);
        String asString = set.toString();
        asString = asString.replaceAll("[\\[\\],\\ ]", "");
        char [] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        assertEquals("12", new String(asCharArray));
    }

    /**
     * Tests that instances created with the <code>newInstanceWithExpectedSize</code>
     * static factory methods do not have to resize to hold the expected number of elements.
     */
    @Test
    public void testExpectedSizeInstanceCreation()
    {
        KTypeOpenHashSet<KType> fixture =
                KTypeOpenHashSet.newInstanceWithExpectedSize(KTypeOpenHashSet.DEFAULT_CAPACITY);

        assertEquals(KTypeOpenHashSet.DEFAULT_CAPACITY, this.set.keys.length);
        assertEquals(KTypeOpenHashSet.DEFAULT_CAPACITY * 2, fixture.keys.length);

        for (int i = 0; i < KTypeOpenHashSet.DEFAULT_CAPACITY; i++)
        {
            KType key = cast(i);
            this.set.add(key);
            fixture.add(key);
        }

        assertEquals(KTypeOpenHashSet.DEFAULT_CAPACITY * 2, this.set.keys.length);
        assertEquals(KTypeOpenHashSet.DEFAULT_CAPACITY * 2, fixture.keys.length);
    }
}
