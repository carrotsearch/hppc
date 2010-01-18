package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.MethodRule;

/**
 * Unit tests for {@link ObjectOpenHashSet}.
 */
public class ObjectOpenHashSetTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectOpenHashSet<Object> set;

    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key = 1;

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
    public void testAddAndGet()
    {
        assertEquals2(defaultValue, set.addAndGet(key));
        assertTrue(key == set.addAndGet(key));

        assertSortedListEquals(set.toArray(), 1);
        assertEquals(1, set.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        assertFalse(set.add(key));
        assertTrue(set.add(key));
        assertEquals(1, set.size());
    }

    /* */
    @Test
    public void testAddv()
    {
        set.addv(newArray(set.keys, 0, 1, 2, 1, 0));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.addv(newArray(set.keys, 0, 1, 2, 3, 4));

        assertTrue(set.remove(/* intrinsic:ktypecast */ 2));
        assertFalse(set.remove(/* intrinsic:ktypecast */ 2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testClear()
    {
        set.addv(newArray(set.keys, 1, 2, 3));
        set.clear();
        checkTrailingSpaceUninitialized();
        assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testRemoveAndGet()
    {
        set.addv(newArray(set.keys, 0, 1, 2, 3, 4));

        assertEquals2(
            /* intrinsic:ktypecast */ 2, set.removeAndGet(/* intrinsic:ktypecast */ 2));
        assertEquals2(
            Intrinsics.defaultKTypeValue(), set.removeAndGet(/* intrinsic:ktypecast */ 2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testIterable()
    {
        set.addv(newArray(set.keys, 1, 2, 2, 3, 4));
        set.remove(/* intrinsic:ktypecast */ 2);
        assertEquals(3, set.size());

        int count = 0;
        for (ObjectCursor<Object> cursor : set)
        {
            count++;
            assertTrue(set.contains(cursor.value));
            assertEquals2(cursor.value, set.addAndGet(cursor.value));
        }
        assertEquals(count, set.size());

        set.clear();
        assertFalse(set.iterator().hasNext());
    }

    /* removeIf:primitiveKType */
    @Test
    public void testNullKey()
    {
        set.add(null);
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
