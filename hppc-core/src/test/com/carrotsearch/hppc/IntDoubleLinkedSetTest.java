package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.assertSortedListEquals;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.predicates.IntPredicate;

/**
 * Unit tests for {@link IntDoubleLinkedSet}.
 */
public class IntDoubleLinkedSetTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public IntDoubleLinkedSet set;

    int key1 = 1;
    int key2 = 2;
    int defaultValue = 0;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* */
    @Before
    public void initialize()
    {
        set = new IntDoubleLinkedSet();
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

        assertTrue(set.contains(key1));
        assertFalse(set.contains(key2));
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
        set.add(0, 1, 2, 1, 0);
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        IntDoubleLinkedSet set2 = new IntDoubleLinkedSet();
        set2.add(1, 2);
        set.add(0, 1);

        assertEquals(1, set.addAll(set2));
        assertEquals(0, set.addAll(set2));

        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.add(0, 1, 2, 3, 4);

        assertTrue(set.remove(2));
        assertFalse(set.remove(2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            IntDoubleLinkedSet set = new IntDoubleLinkedSet(i, i);

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
        set.add(0, 1, 2, 3, 4);

        IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

        assertEquals(2, set.removeAll(list2));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        set.add(0, key1, key2);

        assertEquals(1, set.removeAll(new IntPredicate()
        {
            public boolean apply(int v)
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
        set.add(0, key1, key2, 3, 4, 5);

        assertEquals(4, set.retainAll(new IntPredicate()
        {
            public boolean apply(int v)
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
        set.add(1, 2, 3);
        set.clear();
        assertEquals(0, set.size());
        assertEquals(0, set.toArray().length);
    }

    /* */
    @Test
    public void testIterable()
    {
        set.add(1, 2, 2, 3, 4);
        set.remove(2);
        assertEquals(3, set.size());

        int count = 0;
        for (IntCursor cursor : set)
        {
            count++;
            assertTrue(set.contains(cursor.value));
        }
        assertEquals(count, set.size());

        set.clear();
        assertFalse(set.iterator().hasNext());
    }

    /* */
    @Test
    public void testConstructorFromContainer()
    {
        IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

        set = new IntDoubleLinkedSet(list2);
        assertEquals(3, set.size());
        assertSortedListEquals(list2.toArray(), set.toArray());
    }

    /* */
    @Test
    public void testFromMethod()
    {
        IntOpenHashSet list2 = new IntOpenHashSet();
        list2.add(1, 3, 5);

        IntDoubleLinkedSet s1 = IntDoubleLinkedSet.from(1, 3, 5);
        IntDoubleLinkedSet s2 = IntDoubleLinkedSet.from(1, 3, 5);

        assertSortedListEquals(list2.toArray(), s1.toArray());
        assertSortedListEquals(list2.toArray(), s2.toArray());
    }

    /* */
    @Test
    public void testToString()
    {
        assertEquals("[1, 3, 5]", IntDoubleLinkedSet.from(1, 3, 5).toString());
    }
    
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
                    assertEquals(other.add(key), set.add(key));
                    assertTrue(set.contains(key));
                }
                else
                {
                    assertEquals(other.remove(key), set.remove(key));
                }

                assertEquals(other.size(), set.size());
            }
            
            int [] actual = set.toArray();
            int [] expected = new int [other.size()];
            int i = 0;
            for (Integer v : other)
                expected[i++] = v;
            Arrays.sort(expected);
            Arrays.sort(actual);
            assertArrayEquals(expected, actual);
        }
    }
}
