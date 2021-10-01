/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

import java.util.*;

import static com.carrotsearch.hppc.TestUtils.assertEquals2;
import static com.carrotsearch.hppc.TestUtils.assertSortedListEquals;
import static org.junit.Assert.*;

/**
 * Tests for {@link KTypeVTypeWormMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeWormMapTest<KType, VType> extends AbstractKTypeVTypeTest<KType, VType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeWormMap<KType, VType> map = newInstance();
    
    protected KTypeVTypeWormMap<KType, VType> newInstance() {
      return new KTypeVTypeWormMap<>();
    }

    @After
    public void checkEmptySlotsUninitialized()
    {
        if (map != null)
        {
            int occupied = 0;
            int max = map.keys.length;
            for (int i = 0; i < max; i++)
            {
                if (map.next[i] == 0)
                {

                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.<KType> empty(), map.keys[i]);
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
            assertEquals(occupied, map.size());
        }
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
      Assertions.assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> map.indexGet(map.indexOf(key2)));

      Assertions.assertThat(map.indexReplace(map.indexOf(keyE), value3)).isEqualTo(value1);
      Assertions.assertThat(map.indexReplace(map.indexOf(key1), value4)).isEqualTo(value2);
      Assertions.assertThat(map.indexGet(map.indexOf(keyE))).isEqualTo(value3);
      Assertions.assertThat(map.indexGet(map.indexOf(key1))).isEqualTo(value4);

      map.indexInsert(map.indexOf(key2), key2, value1);
      Assertions.assertThat(map.indexGet(map.indexOf(key2))).isEqualTo(value1);
      Assertions.assertThat(map.size()).isEqualTo(3);

      Assertions.assertThat(map.indexRemove(map.indexOf(keyE))).isEqualTo(value3);
      Assertions.assertThat(map.size()).isEqualTo(2);
      Assertions.assertThat(map.indexRemove(map.indexOf(key2))).isEqualTo(value1);
      Assertions.assertThat(map.size()).isEqualTo(1);
      Assertions.assertThat(map.indexOf(keyE)).isNegative();
      Assertions.assertThat(map.indexOf(key1)).isNotNegative();
      Assertions.assertThat(map.indexOf(key2)).isNegative();
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertSameMap(map, new KTypeVTypeWormMap<>(map));
    }

    /* */
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        KTypeVTypeWormMap<KType, VType> map2 = KTypeVTypeWormMap.from(
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

        KTypeVTypeWormMap<KType, VType> map2 = newInstance();

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
        assertEquals(0, map.size());
    }

    /* */
    @Test
    public void testEmptyKey()
    {
        final KType empty = Intrinsics.<KType> empty();

        map.put(empty, value1);
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertEquals2(value1, map.get(empty));
        assertEquals2(value1, map.getOrDefault(empty, value2));
        assertTrue(map.iterator().hasNext());
        assertEquals2(empty, map.iterator().next().key);
        assertEquals2(value1, map.iterator().next().value);

        assertTrue(map.forEach(new KTypeVTypeProcedure<KType, VType>() {
            boolean hadKeyValue;

            @Override
            public void apply(KType key, VType value) {
                hadKeyValue |= (key == empty && value == value1);
            }
        }).hadKeyValue);

        assertTrue(map.forEach(new KTypeVTypePredicate<KType, VType>() {
            boolean hadKeyValue;

            @Override
            public boolean apply(KType key, VType value) {
                hadKeyValue |= (key == empty && value == value1);
                return true;
            }
        }).hadKeyValue);

        assertEquals(1, map.keys().size());
        assertTrue(map.keys().contains(empty));
        assertEquals2(empty, map.keys().iterator().next().value);
        Assertions.assertThat(map.keys().toArray()).containsOnly(empty);

        assertEquals(1, map.values().size());
        assertTrue(map.values().contains(value1));
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

        KTypeVTypeWormMap<KType, VType> other = newInstance();
        other.put(key2, value1);
        other.put(keyE, value1);
        other.put(key4, value1);

        assertEquals(2, map.removeAll(other.keys()));
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
        KTypeVTypeWormMap<KType, VType> map2 = newInstance();

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
        assertEquals(0, map.size());

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
        assertEquals(0, map.size());

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

    @Test
    public void testHashCodeEquals()
    {
        KTypeVTypeWormMap<KType, VType> l0 = newInstance();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, newInstance());

        KTypeVTypeWormMap<KType, VType> l1 = KTypeVTypeWormMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        KTypeVTypeWormMap<KType, VType> l2 = KTypeVTypeWormMap.from(
            newArray(key2, key1, key3),
            newvArray(value2, value1, value3));

        KTypeVTypeWormMap<KType, VType> l3 = KTypeVTypeWormMap.from(
            newArray(key1, key2),
            newvArray(value2, value1));

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertNotEquals(l1, l3);
        assertNotEquals(l2, l3);
    }

    @Test
    public void testBug_HPPC37()
    {
        KTypeVTypeWormMap<KType, VType> l1 = KTypeVTypeWormMap.from(
            newArray(key1),
            newvArray(value1));

        KTypeVTypeWormMap<KType, VType> l2 = KTypeVTypeWormMap.from(
            newArray(key2),
            newvArray(value1));

        assertNotEquals(l1, l2);
        assertNotEquals(l2, l1);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        assertNull(map.put(key1, null));
        assertNull(map.get(key1));
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
                    VType previousValue;
                    if (rnd.nextBoolean()) {
                        int index = map.indexOf(key);
                        if (map.indexExists(index)) {
                            previousValue = map.indexReplace(index, value);
                        } else {
                            map.indexInsert(index, key, value);
                            previousValue = null;
                        }
                    } else {
                        previousValue = map.put(key, value);
                    }
                    assertEquals(other.put(key, value), previousValue);

                    assertEquals(value, map.get(key));
                    assertEquals(value, map.indexGet(map.indexOf(key)));
                    assertTrue(map.containsKey(key));
                    assertTrue(map.indexExists(map.indexOf(key)));
                }
                else
                {
                    assertEquals(other.containsKey(key), map.containsKey(key));
                    VType previousValue = map.containsKey(key) && rnd.nextBoolean() ?
                            map.indexRemove(map.indexOf(key))
                            : map.remove(key);
                    assertEquals(other.remove(key), previousValue);
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

        KTypeVTypeWormMap<KType, VType> cloned = map.clone();
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
        KTypeVTypeWormMap<KType, VType> l1 = newInstance();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeWormMap<KType, VType> l2 = new KTypeVTypeWormMap<>(l1);
        l2.putAll(l1);
  
        KTypeVTypeWormMap<KType, VType> l3 = new KTypeVTypeWormMap<>(l2);
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
        class Sub extends KTypeVTypeWormMap<KType, VType> {}

        KTypeVTypeWormMap<KType, VType> l1 = newInstance();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeWormMap<KType, VType> l2 = new Sub();
        l2.putAll(l1);
        l2.put(k4, value3);
  
        KTypeVTypeWormMap<KType, VType> l3 = new Sub();
        l3.putAll(l2);

        Assertions.assertThat(l1).isNotEqualTo(l2);
        Assertions.assertThat(l2.hashCode()).isEqualTo(l3.hashCode());
        Assertions.assertThat(l2).isEqualTo(l3);
    }

    /* */
    @Test
    public void testIndexOf()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);
        assertTrue(map.indexOf(key1) >= 0);
        assertTrue(map.indexOf(key2) >= 0);
        assertTrue(map.indexOf(key3) >= 0);
        assertTrue(map.indexOf(key4) < 0);
        assertTrue(map.indexOf(key5) < 0);

        KTypeVTypeWormMap<KType, VType> sameHashMap = new KTypeVTypeWormMap<KType, VType>() {
            @Override
            public int hashKey(KType key) {
                return 0;
            }
        };
        sameHashMap.put(key1, value3);
        sameHashMap.put(key2, value2);
        sameHashMap.put(key3, value1);
        assertTrue(sameHashMap.indexOf(key1) >= 0);
        assertTrue(sameHashMap.indexOf(key2) >= 0);
        assertTrue(sameHashMap.indexOf(key3) >= 0);
        assertTrue(sameHashMap.indexOf(key4) < 0);
        assertTrue(sameHashMap.indexOf(key5) < 0);
    }

    /* */
    @Test
    public void testIndexReplace()
    {
        KTypeVTypeWormMap<KType, VType> sameHashMap = new KTypeVTypeWormMap<KType, VType>() {
            @Override
            public int hashKey(KType key) {
                return 0;
            }
        };
        sameHashMap.put(key1, value1);
        sameHashMap.put(key2, value2);
        sameHashMap.put(key3, value3);

        int index = sameHashMap.indexOf(key2);
        assertTrue(index >= 0);
        sameHashMap.indexReplace(index, value4);

        assertEquals2(value1, sameHashMap.get(key1));
        assertEquals2(value4, sameHashMap.get(key2));
        assertEquals2(value3, sameHashMap.get(key3));
    }

    /* */
    @Test
    public void testIndexInsert()
    {
        KTypeVTypeWormMap<KType, VType> sameHashMap = new KTypeVTypeWormMap<KType, VType>() {
            @Override
            public int hashKey(KType key) {
                return 0;
            }
        };
        sameHashMap.put(key1, value1);
        sameHashMap.put(key2, value2);
        sameHashMap.put(key3, value3);

        int index = sameHashMap.indexOf(key4);
        assertTrue(index < 0);
        sameHashMap.indexInsert(index, key4, value4);

        assertEquals2(value1, sameHashMap.get(key1));
        assertEquals2(value2, sameHashMap.get(key2));
        assertEquals2(value3, sameHashMap.get(key3));
        assertEquals2(value4, sameHashMap.get(key4));
    }

    /*! #if ($TemplateOptions.AllGeneric) !*/
    private static final int MIN_RANDOM_KEY = -50;
    private static final int MAX_RANDOM_KEY = 50;

    @Test
    public void testAgainstHashMap2()
    {
        final int NUM_OPERATIONS = 1000000;
        KTypeVTypeWormMap<Integer, Integer> wormMap = randomBoolean() ? new KTypeVTypeWormMap<Integer, Integer>() : new KTypeVTypeWormMap<Integer, Integer>(randomIntBetween(0, 100));
        Map<Integer, Integer> jdkMap = new HashMap<>();
        for (int i = 0; i < NUM_OPERATIONS; i++) {
            try {
                Operation op = Operation.random();
                op.apply(wormMap, jdkMap);
                checkEquals(jdkMap, wormMap);
            } catch (Throwable t) {
                throw new AssertionError((t.getMessage() == null ? "" : t.getMessage() + " ") + "for i=" + i + ", jdkSet=" + jdkMap + ", wormMap=" + wormMap, t);
            }
        }
    }

    private void checkEquals(final Map<Integer, Integer> jdkMap, final KTypeVTypeWormMap<Integer, Integer> wormMap) {
        assertEquals(jdkMap.size(), wormMap.size());
        assertEquals(jdkMap.size(), wormMap.keys().size());
        assertEquals(jdkMap.size(), wormMap.values().size());
        for (Map.Entry<Integer, Integer> entry : jdkMap.entrySet()) {
            assertEquals(entry.getValue(), wormMap.get(entry.getKey()));
        }
        if (randomBoolean()) {
            wormMap.forEach(new KTypeVTypePredicate<Integer, Integer>() {
                @Override
                public boolean apply(Integer key, Integer value) {
                    assertEquals(value, wormMap.get(key));
                    assertEquals(jdkMap.get(key), value);
                    return true;
                }
            });
        } else {
            wormMap.keys().forEach(new KTypePredicate<Integer>() {
                @Override
                public boolean apply(Integer key) {
                    assertEquals(jdkMap.get(key), wormMap.get(key));
                    return true;
                }
            });
        }
    }

    private enum Operation {
        CLEAR(1, new Application() {
            @Override
            public void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap) {
                wormMap.clear(); jdkMap.clear();
            }}),
        PUT(50, new Application() {
            @Override
            public void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap) {
                int e = randomKey(); wormMap.put(e, value(e)); jdkMap.put(e, value(e));
            }}),
        GET(30, new Application() {
            @Override
            public void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap) {
                int e = randomKey(); assertEquals(jdkMap.get(e), wormMap.get(e));
            }}),
        REMOVE(20, new Application() {
            @Override
            public void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap) {
                int e = randomKey(); wormMap.remove(e); jdkMap.remove(e);
            }}),
        ;
        final int randomRange;
        final Application application;

        Operation(int randomRange, Application application) {
            this.randomRange = randomRange;
            this.application = application;
        }

        void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap) {
            application.apply(wormMap, jdkMap);
        }

        static Operation random() {
            int randomValue = randomIntBetween(0, SUM_RANGES - 1);
            int cumulativeRange = 0;
            for (Operation op : OPERATIONS) {
                cumulativeRange += op.randomRange;
                if (randomValue < cumulativeRange) {
                    return op;
                }
            }
            throw new IllegalStateException();
        }

        static int randomKey() {
            return randomIntBetween(MIN_RANDOM_KEY, MAX_RANDOM_KEY);
        }

        static int value(int key) {
            return key + 2 * MAX_RANDOM_KEY;
        }

        static final Operation[] OPERATIONS = Operation.values();
        static final int SUM_RANGES;
        static {
            int sumRanges = 0;
            for (Operation op : OPERATIONS) {
                sumRanges += op.randomRange;
            }
            SUM_RANGES = sumRanges;
        }

        interface Application {
            void apply(KTypeVTypeWormMap<Integer, Integer> wormMap, Map<Integer, Integer> jdkMap);
        }
    }
    /*! #end !*/
}
