package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import com.carrotsearch.hppc.procedures.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.cursors.*;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for {@link KTypeVTypeOpenHashMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeOpenHashMapTest<KType, VType> extends AbstractKTypeTest<KType>
{
    protected VType value0 = vcast(0);
    protected VType value1 = vcast(1);
    protected VType value2 = vcast(2);
    protected VType value3 = vcast(3);

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeOpenHashMap<KType, VType> map = KTypeVTypeOpenHashMap.newInstance();

    @After
    public void checkEmptySlotsUninitialized()
    {
        if (map != null)
        {
            int occupied = 0;
            for (int i = 0; i < map.keys.length; i++)
            {
                if (map.allocated[i] == false)
                {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    assertEquals2(Intrinsics.defaultKTypeValue(), map.keys[i]);
                    /*! #end !*/
                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    assertEquals2(Intrinsics.defaultVTypeValue(), map.values[i]);
                    /*! #end !*/
                }
                else
                {
                    occupied++;
                }
            }
            assertEquals(occupied, map.assigned);
        }
    }

    /**
     * Convert to target type from an integer used to test stuff. 
     */
    protected VType vcast(int value)
    {
        /*! #if ($TemplateOptions.VTypePrimitive)
            return (VType) value;
            #else !*/ 
            @SuppressWarnings("unchecked")        
            VType v = (VType)(Object) value;
            return v;
        /*! #end !*/
    }

    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    protected VType [] newvArray(VType... elements)
    {
        return elements;
    }

    private void assertSameMap(
        final KTypeVTypeMap<KType, VType> c1,
        final KTypeVTypeMap<KType, VType> c2)
    {
        assertEquals(c1.size(), c2.size());

        c1.forEach(new KTypeVTypeProcedure<KType, VType>()
        {
            public void apply(KType key, VType value)
            {
                assertTrue(c2.containsKey(key));
                assertEquals2(value, c2.get(key));
            }
        });
    }
    
    /* */
    @Test
    public void testCloningConstructor()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertSameMap(map, KTypeVTypeOpenHashMap.from(map));
        assertSameMap(map, new KTypeVTypeOpenHashMap<KType, VType>(map));
    }

    /* */
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        KTypeVTypeOpenHashMap<KType, VType> map2 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        assertSameMap(map, map2);
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
        final Random rnd = new Random();
        final HashSet<Object> values = new HashSet<Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final int v = rnd.nextInt();
            final boolean hadKey = values.contains(cast(v));
            values.add(cast(v));

            assertEquals(hadKey, map.containsKey(cast(v)));
            map.put(cast(v), vcast(v));
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

        KTypeVTypeOpenHashMap<KType, VType> map2 = 
            new KTypeVTypeOpenHashMap<KType, VType>();

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

    /*! #if ($TemplateOptions.VTypePrimitive)
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value1 + value2, map.putOrAdd(key1, value1, value2));
    }
    #end !*/

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

        KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(newArray(key2, key3, key4));

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

        map.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType value)
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

        map.keySet().removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType value)
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
        KTypeVTypeOpenHashMap<KType, VType> map2 = 
            KTypeVTypeOpenHashMap.newInstance(); 

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
        for (KTypeCursor<KType> c : map.keySet())
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
        assertEquals(KTypeVTypeOpenHashMap.MIN_CAPACITY, map.roundCapacity(0));
        assertEquals(Math.max(4, KTypeVTypeOpenHashMap.MIN_CAPACITY), map.roundCapacity(3));
    }

    /* */
    @Test
    public void testIterable()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.remove(key2);

        int count = 0;
        for (KTypeVTypeCursor<KType, VType> cursor : map)
        {
            count++;
            assertTrue(map.containsKey(cursor.key));
            assertEquals2(cursor.value, map.get(cursor.key));

            assertEquals2(cursor.value, map.values[cursor.index]);
            assertEquals2(cursor.key, map.keys[cursor.index]);
            assertEquals2(true, map.allocated[cursor.index]);
        }
        assertEquals(count, map.size());

        map.clear();
        assertFalse(map.iterator().hasNext());
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        map = new KTypeVTypeOpenHashMap<KType, VType>(1, 1f);

        for (int i = 0; i < 0x100; i++)
        {
            map.put(cast(i), value1);
        }

        assertEquals(0x100, map.size());
        assertEquals(0x100, map.keys.length);
    }

    /* */
    @Test
    public void testHalfLoadFactor()
    {
        map = new KTypeVTypeOpenHashMap<KType, VType>(1, 0.5f);

        for (int i = 0; i < 0x100; i++)
        {
            map.put(cast(i), value1);
        }

        assertEquals(0x100, map.size());
        assertEquals(0x100 * 2, map.keys.length);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        KTypeVTypeOpenHashMap<KType, VType> l0 = 
            new KTypeVTypeOpenHashMap<KType, VType>();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, new KTypeVTypeOpenHashMap<KType, VType>());

        KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
            newArray(key2, key1, key3),
            newvArray(value2, value1, value3));

        KTypeVTypeOpenHashMap<KType, VType> l3 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2),
            newvArray(value2, value1));

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertFalse(l1.equals(l3));
        assertFalse(l2.equals(l3));
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testBug_HPPC37()
    {
        KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
            newArray(key1),
            newvArray(value1));

        KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
            newArray(key2),
            newvArray(value1));

        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
    }    

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        map.put(null, vcast(10));
        assertEquals2(vcast(10), map.get(null));
        map.remove(null);
        assertEquals(0, map.size());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
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
    /*! #end !*/

    /*! #if ($TemplateOptions.AllGeneric) !*/
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashMap</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final Random rnd = new Random();
        final java.util.HashMap<KType, VType> other = 
            new java.util.HashMap<KType, VType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                KType key = cast(rnd.nextInt(size));
                VType value = vcast(rnd.nextInt());

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
    /*! #end !*/
    
    /*
     * 
     */
    @Test
    public void testClone()
    {
        this.map.put(key1, value1);
        this.map.put(key2, value2);
        this.map.put(key3, value3);

        KTypeVTypeOpenHashMap<KType, VType> cloned = map.clone();
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
        Assume.assumeTrue(
            (int[].class.isInstance(map.keys)     ||
             short[].class.isInstance(map.keys)   ||
             byte[].class.isInstance(map.keys)    ||
             long[].class.isInstance(map.keys)    ||
             Object[].class.isInstance(map.keys)) &&
            (int[].class.isInstance(map.values)   ||
             byte[].class.isInstance(map.values)  ||
             short[].class.isInstance(map.values) ||
             long[].class.isInstance(map.values)  ||
             Object[].class.isInstance(map.values)));

        this.map.put(key1, value1);
        this.map.put(key2, value2);

        String asString = map.toString();
        asString = asString.replaceAll("[^0-9]", "");
        char [] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        assertEquals("1122", new String(asCharArray));
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
            map.put(cast(c.value), this.value1);

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
            map.put(cast(c.value), value2);

        assertEquals(hashChain.size() + differentKeys.size(), map.size());

        /* 
         * Verify the map contains all of the conflicting keys.
         */
        for (IntCursor c : hashChain)
            assertEquals2(value1, map.get(cast(c.value)));

        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertEquals2(value2, map.get(cast(c.value)));

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (IntCursor c : hashChain)
            assertEquals2(value1, map.remove(cast(c.value)));

        assertEquals(differentKeys.size(), map.size());
        
        /*
         * Verify the map contains all the other keys.
         */
        for (IntCursor c : differentKeys)
            assertEquals2(value2, map.get(cast(c.value)));        
    }
    
    /* */
    @Test
    public void testMapValues()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);
        assertSortedListEquals(map.values().toArray(), value1, value2, value3);

        map.clear();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);        
    }

    /* */
    @Test
    public void testMapValuesIterator()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        int counted = 0;
        for (KTypeCursor<VType> c : map.values())
        {
            assertEquals2(map.values[c.index], c.value);
            counted++;
        }
        assertEquals(counted, map.size());
    }

    /* */
    @Test
    public void testMapValuesContainer()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);

        // contains()
        for (KTypeVTypeCursor<KType, VType> c : map)
            assertTrue(map.values().contains(c.value));
        assertFalse(map.values().contains(value3));
        
        assertEquals(map.isEmpty(), map.values().isEmpty());
        assertEquals(map.size(), map.values().size());

        final KTypeArrayList<VType> values = new KTypeArrayList<VType>();
        map.values().forEach(new KTypeProcedure<VType>()
            {
                public void apply(VType value)
                {
                    values.add(value);
                }
            });
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);

        values.clear();
        map.values().forEach(new KTypePredicate<VType>()
            {
                public boolean apply(VType value)
                {
                    values.add(value);
                    return true;
                }
            });
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);
    }
}
