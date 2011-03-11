package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/* removeIf:primitive */
import com.carrotsearch.hppc.hash.*;
import com.carrotsearch.hppc.hash.MurmurHash3.*;
/* end:removeIf */

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
            assertEquals(occupied, map.assigned);
        }
    }

    @Test
    public void testAddRemoveSameHashCollision()
    {
        // This test is only applicable to selected key types.
        Assume.assumeTrue(
            int[].class.isInstance(map.keys) ||
            long[].class.isInstance(map.keys) ||
            Object[].class.isInstance(map.keys));

        IntArrayList hashChain = TestUtils.generateMurmurHash3CollisionChain(0x1fff, 0x7e, 0x1fff /3);

        /*
         * Add all of the conflicting keys to a map. 
         */
        for (IntCursor c : hashChain)
            map.put(/* intrinsic:ktypecast */ c.value, this.value1);

        assertEquals(hashChain.size(), map.size());
        
        /*
         * Add some more keys (random).
         */
        Random rnd = new Random(0xbabebeef);
        IntSet chainKeys = IntOpenHashSet.from(hashChain);
        IntSet differentKeys = new IntOpenHashSet();
        while (differentKeys.size() < 500)
        {
            int k = rnd.nextInt();
            if (!chainKeys.contains(k) && !differentKeys.contains(k))
                differentKeys.add(k);
        }

        for (IntCursor c : differentKeys)
            map.put(/* intrinsic:ktypecast */ c.value, value2);

        assertEquals(hashChain.size() + differentKeys.size(), map.size());

        /* 
         * Verify the map contains all of the conflicting keys.
         */
        for (IntCursor c : hashChain)
            assertEquals2(value1, map.get(/* intrinsic:ktypecast */ c.value));

        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertEquals2(value2, map.get(/* intrinsic:ktypecast */ c.value));

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (IntCursor c : hashChain)
            assertEquals2(value1, map.remove(/* intrinsic:ktypecast */ c.value));

        assertEquals(differentKeys.size(), map.size());
        
        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertEquals2(value2, map.get(/* intrinsic:ktypecast */ c.value));        
    }
    
    /* */
    @Test
    public void testCloningConstructor()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertSameMap(map, ObjectObjectOpenHashMap.from(map));
        assertSameMap(map, new ObjectObjectOpenHashMap<Object, Object>(map));
    }

    /* */
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        ObjectObjectOpenHashMap<Object, Object> map2 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key1, key2, key3},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value1, value2, value3});

        assertSameMap(map, map2);
    }

    private void assertSameMap(
        final ObjectObjectMap<Object, Object> c1,
        final ObjectObjectMap<Object, Object> c2)
    {
        assertEquals(c1.size(), c2.size());

        c1.forEach(new ObjectObjectProcedure<Object, Object>()
        {
            public void apply(
                /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ key,
                /* replaceIf:primitiveVType VType */ Object /* end:replaceIf */ value)
            {
                assertTrue(c2.containsKey(key));
                assertEquals2(value, c2.get(key));
            }
        });
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
            assertEquals(values.size(), map.size());
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
    public void testMapKeySet()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        assertSortedListEquals(map.keySet().toArray(), key1, key2, key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        int counted = 0;
        for (ObjectCursor<Object> c : map.keySet())
        {
            assertEquals2(map.keys[c.index], c.value);
            counted++;
        }
        assertEquals(counted, map.size());
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

    /* */
    @Test
    public void testHashCodeEquals()
    {
        ObjectObjectOpenHashMap<Object, Object> l0 = 
            new ObjectObjectOpenHashMap<Object, Object>();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, new ObjectObjectOpenHashMap<Object, Object>());

        ObjectObjectOpenHashMap<Object, Object> l1 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key1, key2, key3},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value1, value2, value3});

        ObjectObjectOpenHashMap<Object, Object> l2 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key2, key1, key3},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value2, value1, value3});        

        ObjectObjectOpenHashMap<Object, Object> l3 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key1, key2},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value2, value1});        

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertFalse(l1.equals(l3));
        assertFalse(l2.equals(l3));
    }

    /* */
    @Test
    public void testBug_HPPC37()
    {
        ObjectObjectOpenHashMap<Object, Object> l1 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key1},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value1});

        ObjectObjectOpenHashMap<Object, Object> l2 = ObjectObjectOpenHashMap.from(
            new /* replaceIf:primitiveKType KType [] */ Object [] /* end:replaceIf */ {key2},
            new /* replaceIf:primitiveVType VType [] */ Object [] /* end:replaceIf */ {value1});        

        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
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
    
    /* removeIf:primitive */
    @Test
    public void testCustomKeyEquality()
    {
        ObjectHashFunction<int[]> intArrayHash = new ObjectHashFunction<int[]>() {
            @Override
            public int hash(int[] arg)
            {
                int hash = 0;
                for (int v : arg) 
                    hash += 31 * hash + v;
                return hash;
            }
        };

        Comparator<int[]> intArrayComparator = new Comparator<int[]>() {
            @Override
            public int compare(int [] o1, int [] o2)
            {
                return Arrays.equals(o1, o2) ? 0 : 1;
            }
        };
        
        ObjectObjectOpenHashMap<int[], Integer> map = new ObjectObjectOpenHashMap<int[], Integer>(
            ObjectObjectOpenHashMap.DEFAULT_CAPACITY,
            ObjectObjectOpenHashMap.DEFAULT_LOAD_FACTOR,
            intArrayHash, new ObjectMurmurHash(),
            intArrayComparator);

        map.put(new int [] {1, 2, 3}, 1);
        map.put(new int [] {1, 2, 3}, 2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(new int [] {1, 2, 3}));

        map.put(new int [] {3, 2, 1}, 3);

        ObjectObjectOpenHashMap<int[], Integer> map2 = new ObjectObjectOpenHashMap<int[], Integer>(
            ObjectObjectOpenHashMap.DEFAULT_CAPACITY,
            ObjectObjectOpenHashMap.DEFAULT_LOAD_FACTOR,
            intArrayHash, new ObjectMurmurHash(),
            intArrayComparator);
        map2.putAll(map);
        
        assertTrue(map2.equals(map));
        assertEquals(map2.hashCode(), map.hashCode());
    }
    /* end:removeIf */
    
    /*
     * 
     */
    @Test
    public void testClone()
    {
        this.map.put(key1, value1);
        this.map.put(key2, value2);
        this.map.put(key3, value3);
        
        ObjectObjectOpenHashMap<Object, Object> cloned = map.clone();
        cloned.remove(key1);

        assertSortedListEquals(map.keySet().toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.keySet().toArray(), key2, key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        this.map.put(key1, value1);
        this.map.put(key2, value2);

        String asString = map.toString();
        System.out.println(asString);
        asString = asString.replaceAll("[^0-9]", "");
        char [] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        assertEquals("1122", new String(asCharArray));
    }
}
