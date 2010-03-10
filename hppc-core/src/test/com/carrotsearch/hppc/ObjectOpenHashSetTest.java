package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.ObjectPredicate;

/**
 * Unit tests for {@link ObjectOpenHashSet}.
 */
public class ObjectOpenHashSetTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectOpenHashSet<Object> set;

    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key1 = 1;
    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key2 = 2;

    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   defaultValue 
        = Intrinsics.<KType>defaultKTypeValue();

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* */
    @Before
    public void initialize()
    {
        set = new ObjectOpenHashSet<Object>();
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (set != null)
        {
            int occupied = 0;
            for (int i = 0; i < set.keys.length; i++)
            {
                if (set.states[i] == ObjectOpenHashSet.EMPTY)
                {
                    assertEquals2(Intrinsics.defaultKTypeValue(), set.keys[i]);
                }
                else
                {
                    occupied++;
                }
            }
            assertEquals(occupied, set.deleted + set.assigned);
        }
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
        set.add(newArray(set.keys, 0, 1, 2, 1, 0));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        ObjectOpenHashSet<Object> set2 = new ObjectOpenHashSet<Object>();
        set2.add(newArray(set2.keys, 1, 2));
        set.add(newArray(set2.keys, 0, 1));

        assertEquals(1, set.addAll(set2));
        assertEquals(0, set.addAll(set2));

        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.add(newArray(set.keys, 0, 1, 2, 3, 4));

        assertTrue(set.remove(/* intrinsic:ktypecast */ 2));
        assertFalse(set.remove(/* intrinsic:ktypecast */ 2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            ObjectOpenHashSet<Object> set = new ObjectOpenHashSet<Object>(i);
            
            for (int j = 0; j < i; j++)
            {
                set.add(/* intrinsic:ktypecast */ j);
            }

            assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        set.add(newArray(set.keys, 0, 1, 2, 3, 4));

        ObjectOpenHashSet<Object> list2 = new ObjectOpenHashSet<Object>();
        list2.add(newArray(list2.keys, 1, 3, 5));

        assertEquals(2, set.removeAll(list2));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        set.add(newArray(set.keys, 0, key1, key2));

        assertEquals(1, set.removeAll(new ObjectPredicate<Object>()
        {
            public boolean apply(/* replaceIf:primitive KType */ Object /* end:replaceIf */ v)
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
        set.add(newArray(set.keys, 0, key1, key2, 3, 4, 5));

        assertEquals(4, set.retainAll(new ObjectPredicate<Object>()
        {
            public boolean apply(/* replaceIf:primitive KType */ Object /* end:replaceIf */ v)
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
        set.add(newArray(set.keys, 1, 2, 3));
        set.clear();
        checkTrailingSpaceUninitialized();
        assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        set.add(newArray(set.keys, 1, 2, 2, 3, 4));
        set.remove(/* intrinsic:ktypecast */ 2);
        assertEquals(3, set.size());

        int count = 0;
        for (ObjectCursor<Object> cursor : set)
        {
            count++;
            assertTrue(set.contains(cursor.value));
            assertEquals2(cursor.value, set.lget());
        }
        assertEquals(count, set.size());

        set.clear();
        assertFalse(set.iterator().hasNext());
    }

    /* removeIf:primitiveKType */
    @Test
    public void testNullKey()
    {
        set.add((Object) null);
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertTrue(set.remove(null));
        assertEquals(0, set.size());
        assertFalse(set.contains(null));
    }
    /* end:removeIf */
    
    /* removeIf:primitive */
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashSet</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final java.util.Random rnd = new java.util.Random(0x11223344);
        final java.util.HashSet<Integer> other = new java.util.HashSet<Integer>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                Integer key = rnd.nextInt(size);

                if (rnd.nextBoolean())
                {
                    other.add(key);
                    set.add(key);

                    assertTrue(set.contains(key));
                    assertSame(key, set.lget());
                }
                else
                {
                    assertEquals(other.remove(key), set.remove(key));
                }

                assertEquals(other.size(), set.size());
            }
        }
    }
    /* end:removeIf */
}
