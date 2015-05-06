package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/**
 * Tests for {@link KTypeVTypeHashMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeHashMapTest<KType, VType> extends AbstractKTypeTest<KType>
{
    protected VType value0 = vcast(0);
    protected VType value1 = vcast(1);
    protected VType value2 = vcast(2);
    protected VType value3 = vcast(3);
    protected VType value4 = vcast(4);

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeHashMap<KType, VType> map = newInstance();
    
    protected KTypeVTypeHashMap<KType, VType> newInstance() {
      return new KTypeVTypeHashMap<>();
    }

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
    /* #if ($TemplateOptions.VTypeGeneric) */
    @SafeVarargs
    /* #end */
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

    @Test
    public void testVisualizeKeys()
    {
      map.clear();
      
      Assertions.assertThat(map.visualizeKeyDistribution(20).trim()).matches("\\.+");

      map.put(keyE, value0);
      Assertions.assertThat(map.visualizeKeyDistribution(20).trim()).matches("\\.+");

      map.put(key1, value1);
      Assertions.assertThat(map.visualizeKeyDistribution(20).trim()).matches("\\.*X\\.*");
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        final AtomicInteger expands = new AtomicInteger();
        KTypeVTypeHashMap<KType, VType> map = new KTypeVTypeHashMap<KType, VType>(0) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            expands.incrementAndGet();
          }
        };

        // Add some elements.
        final int max = rarely() ? 0 : randomIntBetween(0, 250);
        for (int i = 0; i < max; i++) {
          map.put(cast(i), value0);
        }

        final int additions = randomIntBetween(max, max + 5000);
        map.ensureCapacity(additions + map.size());
        final int before = expands.get();
        for (int i = 0; i < additions; i++) {
          map.put(cast(i), value0);
        }
        assertEquals(before, expands.get());
    }

    @Test
    public void testCursorIndexIsValid()
    {
      map.put(keyE, value1);
      map.put(key1, value2);
      map.put(key2, value3);

      for (KTypeVTypeCursor<KType, VType> c : map) {
        Assertions.assertThat(map.indexExists(c.index)).isTrue();
        Assertions.assertThat(map.indexGet(c.index)).isEqualTo(c.value);
      }
    }

    @Test
    public void testIndexMethods()
    {
      map.put(keyE, value1);
      map.put(key1, value2);
      
      Assertions.assertThat(map.indexOf(keyE)).isNotNegative();
      Assertions.assertThat(map.indexOf(key1)).isNotNegative();
      Assertions.assertThat(map.indexOf(key2)).isNegative();

      Assertions.assertThat(map.indexExists(map.indexOf(keyE))).isTrue();
      Assertions.assertThat(map.indexExists(map.indexOf(key1))).isTrue();
      Assertions.assertThat(map.indexExists(map.indexOf(key2))).isFalse();

      Assertions.assertThat(map.indexGet(map.indexOf(keyE))).isEqualTo(value1);
      Assertions.assertThat(map.indexGet(map.indexOf(key1))).isEqualTo(value2);
      try {
        map.indexGet(map.indexOf(key2));
        fail();
      } catch (AssertionError e) {
        // Expected.
      }

      Assertions.assertThat(map.indexReplace(map.indexOf(keyE), value3)).isEqualTo(value1);
      Assertions.assertThat(map.indexReplace(map.indexOf(key1), value4)).isEqualTo(value2);
      Assertions.assertThat(map.indexGet(map.indexOf(keyE))).isEqualTo(value3);
      Assertions.assertThat(map.indexGet(map.indexOf(key1))).isEqualTo(value4);

      map.indexInsert(map.indexOf(key2), key2, value1);
      Assertions.assertThat(map.indexGet(map.indexOf(key2))).isEqualTo(value1);
      Assertions.assertThat(map.size()).isEqualTo(3);
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertSameMap(map, new KTypeVTypeHashMap<>(map));
    }

    /* */
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        KTypeVTypeHashMap<KType, VType> map2 = KTypeVTypeHashMap.from(
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
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(randomLong());
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

        KTypeVTypeHashMap<KType, VType> map2 = newInstance();

        map2.put(key2, value2);
        map2.put(keyE, value1);

        // One new key (keyE).
        assertEquals(1, map.putAll(map2));
        
        // Assert the value under key2 has been replaced.
        assertEquals2(value2, map.get(key2));

        // And key3 has been added.
        assertEquals2(value1, map.get(keyE));
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
    public void testEmptyKey()
    {
        final KType empty = Intrinsics.<KType> empty(); 

        map.put(empty, value1);
        assertEquals(1, map.size());
        assertEquals(false, map.isEmpty());
        assertEquals2(value1, map.get(empty));
        assertEquals2(value1, map.getOrDefault(empty, value2));
        assertEquals(true, map.iterator().hasNext());
        assertEquals2(empty, map.iterator().next().key);
        assertEquals2(value1, map.iterator().next().value);
        
        assertEquals(true, map.forEach(new KTypeVTypeProcedure<KType, VType>() {
            boolean hadKeyValue; 
  
            @Override
            public void apply(KType key, VType value) {
              hadKeyValue |= (key == empty && value == value1); 
            }
          }).hadKeyValue);

        assertEquals(true, map.forEach(new KTypeVTypePredicate<KType, VType>() {
          boolean hadKeyValue; 

          @Override
          public boolean apply(KType key, VType value) {
            hadKeyValue |= (key == empty && value == value1);
            return true;
          }
        }).hadKeyValue);

        assertEquals(1, map.keys().size());
        assertEquals(true, map.keys().contains(empty));
        assertEquals2(empty, map.keys().iterator().next().value);
        Assertions.assertThat(map.keys().toArray()).containsOnly(empty);

        assertEquals(1, map.values().size());
        assertEquals(true, map.values().contains(value1));
        assertEquals2(value1, map.values().iterator().next().value);
        Assertions.assertThat(map.values().toArray()).containsOnly(value1);

        map.remove(empty);
        assertEquals2(Intrinsics.<VType> empty(), map.get(empty));
    }

    /* */
    @Test
    public void testRemoveAllWithList()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(keyE, value1);

        KTypeArrayList<KType> other = new KTypeArrayList<>();
        other.add(newArray(key2, keyE, key4));

        assertEquals(2, map.removeAll(other));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveAllWithSet()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);
        map.put(keyE, value1);

        KTypeHashSet<KType> other = new KTypeHashSet<>();
        other.addAll(newArray(key2, keyE, key4));

        assertEquals(2, map.removeAll(other));
        assertEquals(2, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveAllWithKeyPredicate()
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
    public void testRemoveAllWithKeyValuePredicate()
    {
        map.put(keyE, value1);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);

        map.removeAll(new KTypeVTypePredicate<KType, VType>()
        {
            public boolean apply(KType key, VType value)
            {
                return value == value1;
            }
        });

        assertEquals(2, map.size());
        Assertions.assertThat(map.getOrDefault(key2, value0)).isEqualTo(value2);
        Assertions.assertThat(map.getOrDefault(key3, value0)).isEqualTo(value2);
    }

    /* */
    @Test
    public void testRemoveAllOnValueContainer()
    {
        map.put(keyE, value1);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);

        map.values().removeAll(value1);

        assertEquals(2, map.size());
        Assertions.assertThat(map.getOrDefault(key2, value0)).isEqualTo(value2);
        Assertions.assertThat(map.getOrDefault(key3, value0)).isEqualTo(value2);
    }

    /* */
    @Test
    public void testRemoveAllPredicateOnValueContainer()
    {
        map.put(keyE, value1);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);

        map.values().removeAll(new KTypePredicate<VType>() {
          @Override
          public boolean apply(VType value) {
            return value == value1;
          }
        });

        assertEquals(2, map.size());
        Assertions.assertThat(map.getOrDefault(key2, value0)).isEqualTo(value2);
        Assertions.assertThat(map.getOrDefault(key3, value0)).isEqualTo(value2);
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
        KTypeVTypeHashMap<KType, VType> map2 = newInstance(); 

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

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testRelease()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.release();
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(0, map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
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

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
      final AtomicInteger reallocations = new AtomicInteger();
      final int elements = 0x7F;
      map = new KTypeVTypeHashMap<KType, VType>(elements, 1f) {
        @Override
        protected double verifyLoadFactor(double loadFactor) {
          // Skip load factor sanity range checking.
          return loadFactor;
        }
        
        @Override
        protected void allocateBuffers(int arraySize) {
          super.allocateBuffers(arraySize);
          reallocations.incrementAndGet();
        }
      };

      int reallocationsBefore = reallocations.get();
      assertEquals(reallocationsBefore, 1);
      for (int i = 1; i <= elements; i++)
      {
          map.put(cast(i), value1);
      }
      
      // Non-existent key.
      KType outOfSet = cast(elements + 1);
      map.remove(outOfSet);
      assertFalse(map.containsKey(outOfSet));
      assertEquals(reallocationsBefore, reallocations.get());

      // Should not expand because we're replacing an existing element.
      map.put(k1, value2);
      assertEquals(reallocationsBefore, reallocations.get());

      // Remove from a full map.
      map.remove(k1);
      assertEquals(reallocationsBefore, reallocations.get());
      map.put(k1, value2);

      // Check expand on "last slot of a full map" condition.
      map.put(outOfSet, value1);
      assertEquals(reallocationsBefore + 1, reallocations.get());
    }

    @Test
    public void testHashCodeEquals()
    {
        KTypeVTypeHashMap<KType, VType> l0 = newInstance();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, newInstance());

        KTypeVTypeHashMap<KType, VType> l1 = KTypeVTypeHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        KTypeVTypeHashMap<KType, VType> l2 = KTypeVTypeHashMap.from(
            newArray(key2, key1, key3),
            newvArray(value2, value1, value3));

        KTypeVTypeHashMap<KType, VType> l3 = KTypeVTypeHashMap.from(
            newArray(key1, key2),
            newvArray(value2, value1));

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertFalse(l1.equals(l3));
        assertFalse(l2.equals(l3));
    }

    /* */
    @Test 
    public void testHashCodeEqualsForDifferentMix()
    {
      KTypeVTypeHashMap<KType, VType> l0 = new KTypeVTypeHashMap<>(0, 0.5d, HashOrderMixing.constant(1));
      KTypeVTypeHashMap<KType, VType> l1 = new KTypeVTypeHashMap<>(0, 0.5d, HashOrderMixing.constant(2));

      assertEquals(0, l0.hashCode());
      assertEquals(l0.hashCode(), l1.hashCode());
      assertEquals(l0, l1);

      KTypeVTypeHashMap<KType, VType> l2 = KTypeVTypeHashMap.from(
          newArray(key1, key2, key3),
          newvArray(value1, value2, value3));

      l0.putAll(l2);
      l1.putAll(l2);

      assertEquals(l0.hashCode(), l1.hashCode());
      assertEquals(l0, l1);
    }

    @Test
    public void testBug_HPPC37()
    {
        KTypeVTypeHashMap<KType, VType> l1 = KTypeVTypeHashMap.from(
            newArray(key1),
            newvArray(value1));

        KTypeVTypeHashMap<KType, VType> l2 = KTypeVTypeHashMap.from(
            newArray(key2),
            newvArray(value1));

        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
    }    

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        assertEquals(null, map.put(key1, null));
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
        final Random rnd = new Random(randomLong());
        final java.util.HashMap<KType, VType> other = 
            new java.util.HashMap<KType, VType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                KType key = cast(rnd.nextInt(size));
                if (rnd.nextInt(50) == 0) {
                  key = Intrinsics.<KType> empty();
                }

                VType value = vcast(rnd.nextInt());

                if (rnd.nextBoolean())
                {
                    map.put(key, value);
                    other.put(key, value);

                    assertEquals(value, map.get(key));
                    assertTrue(map.containsKey(key));
                }
                else
                {
                    assertEquals(other.containsKey(key), map.containsKey(key));
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

        KTypeVTypeHashMap<KType, VType> cloned = map.clone();
        cloned.remove(key1);

        assertSortedListEquals(map.keys().toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.keys().toArray(), key2, key3);
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
    
    /* */
    @Test
    public void testEqualsSameClass()
    {
        KTypeVTypeHashMap<KType, VType> l1 = newInstance();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeHashMap<KType, VType> l2 = new KTypeVTypeHashMap<>(l1);
        l2.putAll(l1);
  
        KTypeVTypeHashMap<KType, VType> l3 = new KTypeVTypeHashMap<>(l2);
        l3.putAll(l2);
        l3.put(k4, value0);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeVTypeHashMap<KType, VType> {
        };

        KTypeVTypeHashMap<KType, VType> l1 = newInstance();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeHashMap<KType, VType> l2 = new Sub();
        l2.putAll(l1);
        l2.put(k4, value3);
  
        KTypeVTypeHashMap<KType, VType> l3 = new Sub();
        l3.putAll(l2);

        Assertions.assertThat(l1).isNotEqualTo(l2);
        Assertions.assertThat(l2.hashCode()).isEqualTo(l3.hashCode());
        Assertions.assertThat(l2).isEqualTo(l3);
    }    
}
