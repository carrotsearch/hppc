/*! #set($TemplateOptions.ignored = ($TemplateOptions.KTypePrimitive)) !*/
package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import org.junit.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/**
 * Tests for {@link KTypeVTypeIdentityHashMap}.
 */
/*! #if ($TemplateOptions.anyGeneric) @SuppressWarnings("all") #end !*/
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeIdentityHashMapTest<KType, VType> extends AbstractKTypeTest<KType>
{
    protected VType value0 = vcast(0);
    protected VType value1 = vcast(1);
    protected VType value2 = vcast(2);
    protected VType value3 = vcast(3);

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeIdentityHashMap<KType, VType> map = new KTypeVTypeIdentityHashMap<>();

    @After
    public void checkEmptySlotsUninitialized()
    {
      if (map != null)
      {
          int occupied = 0;
          for (int i = 0; i <= map.mask; i++)
          {
              if (Intrinsics.<KType> isEmpty(map.keys[i]))
              {
                  /*! #if ($TemplateOptions.KTypeGeneric) !*/
                  assertEquals2(Intrinsics.<KType> empty(), map.keys[i]);
                  /*! #end !*/
                  /*! #if ($TemplateOptions.VTypeGeneric) !*/
                  assertEquals2(Intrinsics.<VType> empty(), map.values[i]);
                  /*! #end !*/
              }
              else
              {
                  occupied++;
              }
          }
          assertEquals(occupied, map.assigned);
          
          if (!map.hasEmptyKey)
          {
            /*! #if ($TemplateOptions.VTypeGeneric) !*/
            assertEquals2(Intrinsics.<VType> empty(), map.values[map.mask + 1]);
            /*! #end !*/
          }
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
    @SafeVarargs
    protected final VType [] newvArray(VType... elements)
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

        assertSameMap(map, new KTypeVTypeIdentityHashMap<KType, VType>(map));
    }

    /* */
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        KTypeVTypeIdentityHashMap<KType, VType> map2 = KTypeVTypeIdentityHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        assertSameMap(map, map2);
    }

    @Test
    public void testGetOrDefault()
    {
        map.put(key2, value2);
        assertTrue(map.containsKey(key2));

        map.put(key1, value1);
        assertEquals2(value1, map.getOrDefault(key1, value3));
        assertEquals2(value3, map.getOrDefault(key3, value3));
        map.remove(key1);
        assertEquals2(value3, map.getOrDefault(key1, value3));
    }

    /* */
    @Test
    public void testPut()
    {
        map.put(key1, value1);

        assertTrue(map.containsKey(key1));
        assertEquals2(value1, map.get(key1));
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
    public void testPutAll()
    {
        map.put(key1, value1);
        map.put(key2, value1);

        KTypeVTypeIdentityHashMap<KType, VType> map2 = 
            new KTypeVTypeIdentityHashMap<KType, VType>();

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

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value3, map.putOrAdd(key1, value1, value2));
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive) !*/
    @Test
    public void testAddTo()
    {
        assertEquals2(value1, map.addTo(key1, value1));
        assertEquals2(value3, map.addTo(key1, value2));
    }
    /*! #end !*/

    /* */
    @Test
    public void testRemove()
    {
        map.put(key1, value1);
        assertEquals2(value1, map.remove(key1));
        assertEquals2(Intrinsics.<VType> empty(), map.remove(key1));
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

        KTypeArrayList<KType> list2 = new KTypeArrayList<>();
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

        map.keys().removeAll(new KTypePredicate<KType>()
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
        KTypeVTypeIdentityHashMap<KType, VType> map2 = 
            new KTypeVTypeIdentityHashMap<>(); 

        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);
        
        map2.put(key2, value1);
        map2.put(key4, value1);

        assertEquals(2, map.keys().retainAll(map2.keys()));

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

        assertSortedListEquals(map.keys().toArray(), key1, key2, key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        int counted = 0;
        for (KTypeCursor<KType> c : map.keys())
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
        }
        assertEquals(count, map.size());

        map.clear();
        assertFalse(map.iterator().hasNext());
    }

    @Test
    public void testHashCodeEquals()
    {
        KTypeVTypeIdentityHashMap<KType, VType> l0 = 
            new KTypeVTypeIdentityHashMap<KType, VType>();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, new KTypeVTypeIdentityHashMap<KType, VType>());

        KTypeVTypeIdentityHashMap<KType, VType> l1 = KTypeVTypeIdentityHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        KTypeVTypeIdentityHashMap<KType, VType> l2 = KTypeVTypeIdentityHashMap.from(
            newArray(key2, key1, key3),
            newvArray(value2, value1, value3));

        KTypeVTypeIdentityHashMap<KType, VType> l3 = KTypeVTypeIdentityHashMap.from(
            newArray(key1, key2),
            newvArray(value2, value1));

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertFalse(l1.equals(l3));
        assertFalse(l2.equals(l3));
    }

    @Test
    public void testBug_HPPC37()
    {
        KTypeVTypeIdentityHashMap<KType, VType> l1 = KTypeVTypeIdentityHashMap.from(
            newArray(key1),
            newvArray(value1));

        KTypeVTypeIdentityHashMap<KType, VType> l2 = KTypeVTypeIdentityHashMap.from(
            newArray(key2),
            newvArray(value1));

        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
    }    
}
