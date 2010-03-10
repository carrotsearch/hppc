package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;

/**
 * Tests for {@link ObjectObjectOpenHashMap}.
 */
public class ObjectObjectOpenHashMapTest
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectObjectOpenHashMap<Object, Object> map;

    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key1 = 1;
    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key2 = 2;
    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key3 = 3;
    /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key4 = 4;

    /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value1 = 1;
    /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value2 = 2;
    /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value3 = 3;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();
    
    /* */
    @Before
    public void initialize()
    {
        map = new ObjectObjectOpenHashMap<Object, Object>();
    }

    @After
    public void checkEmptySlotsUninitialized()
    {
        if (map != null)
        {
            int occupied = 0;
            for (int i = 0; i < map.keys.length; i++)
            {
                if (map.states[i] == ObjectObjectOpenHashMap.EMPTY)
                {
                    /* removeIf:primitive */
                    assertEquals2(Intrinsics.defaultKTypeValue(), map.keys[i]);
                    assertEquals2(Intrinsics.defaultVTypeValue(), map.values[i]);
                    /* end:removeIf */
                }
                else
                {
                    occupied++;
                }
            }
            assertEquals(occupied, map.deleted + map.assigned);
        }
    }

    /* */
    @Test
    public void testPut()
    {
        map.put(key1, value1);

        assertTrue(map.containsKey(key1));
        assertEquals2(value1, map.lget());
        assertEquals2(value1, map.get(key1));
    }

    /* */
    @Test
    public void testLPut()
    {
        map.put(key1, value2);
        if (map.containsKey(key1))
            map.lset(value3);

        assertTrue(map.containsKey(key1));
        assertEquals2(value3, map.lget());
        assertEquals2(value3, map.get(key1));
    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        map.put(key1, value1);
        assertEquals2(value1, map.put(key1, value3));
        assertEquals2(value3, map.get(key1));
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(0x11223344);
        final HashSet<Object> values = new HashSet<Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final int v = rnd.nextInt();
            final boolean hadKey = values.contains(/* intrinsic:ktypecast */ v);
            values.add(/* intrinsic:ktypecast */ v);

            assertEquals(hadKey, map.containsKey(/* intrinsic:ktypecast */ v));
            map.put(
                /* intrinsic:ktypecast */ v, 
                /* intrinsic:vtypecast */ v);
        }
        assertEquals(values.size(), map.size());
    }

    /* */
    @Test
    public void testPutAll()
    {
        map.put(key1, value1);
        map.put(key2, value1);

        ObjectObjectOpenHashMap<Object, Object> map2 = 
            new ObjectObjectOpenHashMap<Object, Object>();

        map2.put(key2, value2);
        map2.put(key3, value1);

        // One new key (key3).
        assertEquals(1, map.putAll(map2));
        
        // Assert the value under key2 has been replaced.
        assertEquals2(value2, map.get(key2));

        // And key3 has been added.
        assertEquals2(value1, map.get(key3));
        assertEquals(3, map.size());
    }
    
    /* */
    @Test
    public void testPutIfAbsent()
    {
        assertTrue(map.putIfAbsent(key1, value1));
        assertFalse(map.putIfAbsent(key1, value2));
        assertEquals2(value1, map.get(key1));
    }

    /* replaceIf:primitiveVType
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value1 + value2, map.putOrAdd(key1, value1, value2));
    }
    *//* end:replaceIf */

    /* */
    @Test
    public void testRemove()
    {
        map.put(key1, value1);
        assertEquals2(value1, map.remove(key1));
        assertEquals2(Intrinsics.defaultVTypeValue(), map.remove(key1));
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(1, map.deleted);
        assertEquals(0, map.assigned);
    }

    /* */
    @Test
    public void testRemoveAllWithContainer()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        /* replaceIf:primitiveKType
        UKTypeArrayList list2 = new UKTypeArrayList();
         */
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        /* end:replaceIf */
        list2.add(newArray(list2.buffer, key2, key3, key4));

        map.removeAll(list2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        map.removeAll(new ObjectPredicate<Object>()
        {
            public boolean apply(/* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ value)
            {
                return value == key2 || value == key3;
            }
        });
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveViaKeySetView()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        map.keySet().removeAll(new ObjectPredicate<Object>()
        {
            public boolean apply(/* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ value)
            {
                return value == key2 || value == key3;
            }
        });
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testMapsIntersection()
    {
        ObjectObjectOpenHashMap<Object, Object> map2 = 
            new ObjectObjectOpenHashMap<Object, Object>(); 

        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);
        
        map2.put(key2, value1);
        map2.put(key4, value1);

        assertEquals(2, map.keySet().retainAll(map2.keySet()));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(key2));
    }

    /* */
    @Test
    public void testClear()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.clear();
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(0, map.deleted);
        assertEquals(0, map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testRoundCapacity()
    {
        assertEquals(0x40000000, map.roundCapacity(Integer.MAX_VALUE));
        assertEquals(0x40000000, map.roundCapacity(Integer.MAX_VALUE / 2 + 1));
        assertEquals(0x40000000, map.roundCapacity(Integer.MAX_VALUE / 2));
        assertEquals(ObjectObjectOpenHashMap.MIN_CAPACITY, map.roundCapacity(0));
        assertEquals(Math.max(4, ObjectObjectOpenHashMap.MIN_CAPACITY), map.roundCapacity(3));
    }

    /* */
    @Test
    @SuppressWarnings("static-access")
    public void testIterable()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.remove(key2);

        int count = 0;
        for (ObjectObjectCursor<Object,Object> cursor : map)
        {
            count++;
            assertTrue(map.containsKey(cursor.key));
            assertEquals2(cursor.value, map.get(cursor.key));

            assertEquals2(cursor.value, map.values[cursor.index]);
            assertEquals2(cursor.key, map.keys[cursor.index]);
            assertEquals2(map.ASSIGNED, map.states[cursor.index]);
        }
        assertEquals(count, map.size());

        map.clear();
        assertFalse(map.iterator().hasNext());
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        map = new ObjectObjectOpenHashMap<Object, Object>(1, 1f);

        for (int i = 0; i < 0x100; i++)
        {
            map.put(/* intrinsic:ktypecast */ i, value1);
        }

        assertEquals(0x100, map.size());
        assertEquals(0x100, map.keys.length);
    }

    /* */
    @Test
    public void testHalfLoadFactor()
    {
        map = new ObjectObjectOpenHashMap<Object, Object>(1, 0.5f);

        for (int i = 0; i < 0x100; i++)
        {
            map.put(/* intrinsic:ktypecast */ i, value1);
        }

        assertEquals(0x100, map.size());
        assertEquals(0x100 * 2, map.keys.length);
    }

    /* removeIf:primitiveKType */
    @Test
    public void testNullKey()
    {
        map.put(null, /* intrinsic:vtypecast */ 10);
        assertEquals2(/* intrinsic:vtypecast */ 10, map.get(null));
        map.remove(null);
        assertEquals(0, map.size());
    }
    /* end:removeIf */

    /* removeIf:primitiveVType */
    @Test
    public void testNullValue()
    {
        map.put(key1, null);
        assertEquals(null, map.get(key1));
        assertTrue(map.containsKey(key1));
        map.remove(key1);
        assertFalse(map.containsKey(key1));
        assertEquals(0, map.size());
    }
    /* end:removeIf */

    /* removeIf:primitive */
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashMap</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final Random rnd = new Random(0x11223344);
        final java.util.HashMap<Integer, Integer> other = 
            new java.util.HashMap<Integer, Integer>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                Integer key = rnd.nextInt(size);
                Integer value = rnd.nextInt();

                if (rnd.nextBoolean())
                {
                    map.put(key, value);
                    other.put(key, value);

                    assertEquals(value, map.get(key));
                    assertTrue(map.containsKey(key));
                    assertEquals(value, map.lget());
                }
                else
                {
                    assertEquals(other.remove(key), map.remove(key));
                }

                assertEquals(other.size(), map.size());
            }
        }
    }
    /* end:removeIf */
}
