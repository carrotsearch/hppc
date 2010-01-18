package com.carrotsearch.hppc;

import static org.junit.Assert.*;
import static com.carrotsearch.hppc.TestUtils.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.MethodRule;

/**
 * Tests for {@link ObjectObjectOpenHashMap}.
 */
public class ObjectObjectOpenHashMapTest
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectObjectOpenHashMap<Object, Object> map;

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
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key = 1;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value = 2;

        map.put(key, value);

        assertTrue(map.containsKey(key));
        assertEquals2(value, map.lget());
        assertEquals2(value, map.get(key));
    }

    /* */
    @Test
    public void testLPut()
    {
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key = 1;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value2 = 2;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value3 = 3;

        map.put(key, value2);
        if (map.containsKey(key))
            map.lset(value3);

        assertTrue(map.containsKey(key));
        assertEquals2(value3, map.lget());
        assertEquals2(value3, map.get(key));
    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */   key = 1;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value = 2;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value3 = 3;

        map.put(key, value);
        assertEquals2(value, map.put(key, value3));
        assertEquals2(value3, map.get(key));
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
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */ key1 = 1;
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */ key2 = 2;
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */ key3 = 3;

        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value1 = 1;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value2 = 2;

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
    public void testRemove()
    {
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */   key = 1;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value = 2;

        map.put(key, value);
        assertEquals2(value, map.remove(key));
        assertEquals2(Intrinsics.defaultVTypeValue(), map.remove(key));
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(1, map.deleted);
        assertEquals(0, map.assigned);
    }

    /* */
    @Test
    public void testRemoveAllIn()
    {
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key1 = 1;
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key2 = 2;
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key3 = 3;
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key4 = 4;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value1 = 1;

        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        /* replaceIf:primitiveKType 
           // TODO: temporarily not available in the primitive version.
         */
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.addv(newArray(list2.buffer, key2, key3, key4));

        map.removeAllKeysIn(list2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
        /* end:replaceIf */
    }

    /* */
    @Test
    public void testClear()
    {
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key = 1;
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key2 = 2;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value = 2;

        map.put(key, value);
        map.put(key2, value);
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
    public void testIterable()
    {
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */   key1 = 1;
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */   key2 = 2;
        /* replaceIf:primitiveKType   KType */ Object /* end:replaceIf */   key3 = 3;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value1 = 11;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value2 = 12;
        /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value3 = 13;

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
        }
        assertEquals(count, map.size());

        map.clear();
        assertFalse(map.iterator().hasNext());
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
        /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key = 10;
        map.put(key, null);
        assertEquals(null, map.get(key));
        assertTrue(map.containsKey(key));
        map.remove(key);
        assertFalse(map.containsKey(key));
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
