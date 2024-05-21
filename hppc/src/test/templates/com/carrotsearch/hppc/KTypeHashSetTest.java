/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import static org.junit.Assert.*;
import static com.carrotsearch.hppc.TestUtils.*;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

/**
 * Unit tests for {@link KTypeHashSet}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeHashSetTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeHashSet<KType> set;

    public final KType EMPTY_KEY = Intrinsics.<KType> empty();

    /* */
    @Before
    public void initialize()
    {
        set = new KTypeHashSet<>();
    }

    @Test
    public void testVisualizeKeys()
    {
      set.clear();
      
      Assertions.assertThat(set.visualizeKeyDistribution(20).trim()).matches("\\.+");

      set.add(keyE);
      Assertions.assertThat(set.visualizeKeyDistribution(20).trim()).matches("\\.+");

      set.add(key1);
      Assertions.assertThat(set.visualizeKeyDistribution(20).trim()).matches("\\.*X\\.*");
      Assertions.assertThat(set.visualizeKeyDistribution(20)).hasSize(20);
      
      for (int i = 0; i < 60; i++) {
        set.add(cast(i));
      }
      Assertions.assertThat(set.visualizeKeyDistribution(20)).hasSize(20);
    }

    @Test
    public void testAddAllViaInterface()
    {
      set.addAll(key1, key2);

      KTypeSet<KType> iface = new KTypeHashSet<>();
      iface.clear();
      iface.addAll(set);
      Assertions.assertThat(iface.toArray()).containsOnly(key1, key2);
    }

    @Test
    public void testIndexMethods()
    {
      set.add(keyE);
      set.add(key1);

      Assertions.assertThat(set.indexOf(keyE)).isNotNegative();
      Assertions.assertThat(set.indexOf(key1)).isNotNegative();
      Assertions.assertThat(set.indexOf(key2)).isNegative();

      Assertions.assertThat(set.indexExists(set.indexOf(keyE))).isTrue();
      Assertions.assertThat(set.indexExists(set.indexOf(key1))).isTrue();
      Assertions.assertThat(set.indexExists(set.indexOf(key2))).isFalse();

      Assertions.assertThat(set.indexGet(set.indexOf(keyE))).isEqualTo(keyE);
      Assertions.assertThat(set.indexGet(set.indexOf(key1))).isEqualTo(key1);
      boolean expectedExceptionThrown = false;
      try {
        set.indexGet(set.indexOf(key2));
      } catch (AssertionError e) {
        expectedExceptionThrown = true;
      }
      if (!expectedExceptionThrown) {
        fail();
      }

      Assertions.assertThat(set.indexReplace(set.indexOf(keyE), keyE)).isEqualTo(keyE);
      Assertions.assertThat(set.indexReplace(set.indexOf(key1), key1)).isEqualTo(key1);

      set.indexInsert(set.indexOf(key2), key2);
      Assertions.assertThat(set.indexGet(set.indexOf(key2))).isEqualTo(key2);
      Assertions.assertThat(set.size()).isEqualTo(3);

      set.indexRemove(set.indexOf(keyE));
      Assertions.assertThat(set.size()).isEqualTo(2);
      set.indexRemove(set.indexOf(key2));
      Assertions.assertThat(set.size()).isEqualTo(1);
      Assertions.assertThat(set.indexOf(keyE)).isNegative();
      Assertions.assertThat(set.indexOf(key1)).isNotNegative();
      Assertions.assertThat(set.indexOf(key2)).isNegative();
    }

    @Test
    public void testCursorIndexIsValid()
    {
      set.add(keyE);
      set.add(key1);
      set.add(key2);

      for (KTypeCursor<KType> c : set) {
        Assertions.assertThat(set.indexExists(c.index)).isTrue();
        Assertions.assertThat(set.indexGet(c.index)).isEqualTo(c.value);
      }
    }

    /* */
    @Test
    public void testEmptyKey()
    {
        KTypeHashSet<KType> set = new KTypeHashSet<KType>();

        boolean b = set.add(EMPTY_KEY);

        Assertions.assertThat(b).isTrue();
        Assertions.assertThat(set.add(EMPTY_KEY)).isFalse();
        Assertions.assertThat(set.size()).isEqualTo(1);
        Assertions.assertThat(set.isEmpty()).isFalse();
        Assertions.assertThat(set.toArray()).containsOnly(EMPTY_KEY);
        Assertions.assertThat(set.contains(EMPTY_KEY)).isTrue();
        int index = set.indexOf(EMPTY_KEY);
        Assertions.assertThat(set.indexExists(index)).isTrue();
        Assertions.assertThat(set.indexGet(index)).isEqualTo(EMPTY_KEY);
        Assertions.assertThat(set.indexReplace(index, EMPTY_KEY)).isEqualTo(EMPTY_KEY);

        if (randomBoolean()) {
            b = set.remove(EMPTY_KEY);
            Assertions.assertThat(b).isTrue();
        } else {
            set.indexRemove(index);
        }

        Assertions.assertThat(set.remove(EMPTY_KEY)).isFalse();
        Assertions.assertThat(set.size()).isEqualTo(0);
        Assertions.assertThat(set.isEmpty()).isTrue();
        Assertions.assertThat(set.toArray()).isEmpty();
        Assertions.assertThat(set.contains(EMPTY_KEY)).isFalse();
        index = set.indexOf(EMPTY_KEY);
        Assertions.assertThat(set.indexExists(index)).isFalse();

        set.indexInsert(index, EMPTY_KEY);
        set.add(key1);
        Assertions.assertThat(set.size()).isEqualTo(2);
        Assertions.assertThat(set.contains(EMPTY_KEY)).isTrue();
        index = set.indexOf(EMPTY_KEY);
        Assertions.assertThat(set.indexExists(index)).isTrue();
        Assertions.assertThat(set.indexGet(index)).isEqualTo(EMPTY_KEY);
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        final AtomicInteger expands = new AtomicInteger();
        KTypeHashSet<KType> set = new KTypeHashSet<KType>(0) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            expands.incrementAndGet();
          }
        };

        // Add some elements.
        final int max = rarely() ? 0 : randomIntBetween(0, 250);
        for (int i = 0; i < max; i++) {
          set.add(cast(i));
        }

        final int additions = randomIntBetween(max, max + 5000);
        set.ensureCapacity(additions + set.size());
        final int before = expands.get();
        for (int i = 0; i < additions; i++) {
          set.add(cast(i));
        }
        assertEquals(before, expands.get());
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
        set.addAll(key1, key1);
        assertEquals(1, set.size());
        assertEquals(1, set.addAll(key1, key2));
        assertEquals(2, set.size());
    }

    /* */
    @Test
    public void testAddVarArgs()
    {
        set.addAll(asArray(0, 1, 2, 1, 0));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        KTypeHashSet<KType> set2 = new KTypeHashSet<KType>();
        set2.addAll(asArray(1, 2));
        set.addAll(asArray(0, 1));

        assertEquals(1, set.addAll(set2));
        assertEquals(0, set.addAll(set2));

        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemove()
    {
        set.addAll(asArray(0, 1, 2, 3, 4));

        assertTrue(set.remove(k2));
        assertFalse(set.remove(k2));
        assertEquals(4, set.size());
        assertSortedListEquals(set.toArray(), 0, 1, 3, 4);
    }

    /* */
    @Test
    public void testInitialCapacityAndGrowth()
    {
        for (int i = 0; i < 256; i++)
        {
            KTypeHashSet<KType> set = new KTypeHashSet<KType>(i);
            
            for (int j = 0; j < i; j++)
            {
                set.add(cast(j));
            }

            assertEquals(i, set.size());
        }
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        final AtomicInteger reallocations = new AtomicInteger();
        final int elements = 0x7F;
        set = new KTypeHashSet<KType>(elements, 1f) {
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
            set.add(cast(i));
        }

        // Non-existent key.
        KType outOfSet = cast(elements + 1);
        set.remove(outOfSet);
        assertFalse(set.contains(outOfSet));
        assertEquals(reallocationsBefore, reallocations.get());

        // Should not expand because we're replacing an existing element.
        assertFalse(set.add(k1));
        assertEquals(reallocationsBefore, reallocations.get());

        // Remove from a full set.
        set.remove(k1);
        assertEquals(reallocationsBefore, reallocations.get());
        set.add(k1);

        // Check expand on "last slot of a full map" condition.
        set.add(outOfSet);
        assertEquals(reallocationsBefore + 1, reallocations.get());
    }
    
    /* */
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        set.addAll(asArray(0, 1, 2, 3, 4));

        KTypeHashSet<KType> list2 = new KTypeHashSet<KType>();
        list2.addAll(asArray(1, 3, 5));

        assertEquals(2, set.removeAll(list2));
        assertEquals(3, set.size());
        assertSortedListEquals(set.toArray(), 0, 2, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        set.addAll(newArray(k0, k1, k2));

        assertEquals(1, set.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
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
        set.addAll(newArray(k0, k1, k2, k3, k4, k5));

        assertEquals(4, set.retainAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
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
        set.addAll(asArray(1, 2, 3));
        set.clear();
        assertEquals(0, set.size());
    }

    /* */
    @Test
    public void testRelease()
    {
        set.addAll(asArray(1, 2, 3));
        set.release();
        assertEquals(0, set.size());
        set.addAll(asArray(1, 2, 3));
        assertEquals(3, set.size());
    }

    /* */
    @Test
    public void testIterable()
    {
        set.addAll(asArray(1, 2, 2, 3, 4));
        set.remove(k2);
        assertEquals(3, set.size());

        int count = 0;
        for (KTypeCursor<KType> cursor : set)
        {
            count++;
            assertTrue(set.contains(cursor.value));
        }
        assertEquals(count, set.size());

        set.clear();
        assertFalse(set.iterator().hasNext());
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        assertTrue(set.add((KType) null));
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertTrue(set.remove(null));
        assertEquals(0, set.size());
        assertFalse(set.contains(null));
    }
    /*! #end !*/
    
    /**
     * Runs random insertions/deletions/clearing and compares the results against {@link HashSet}.
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testAgainstHashSet()
    {
        final Random rnd = RandomizedTest.getRandom();
        final HashSet other = new HashSet();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            set.clear();

            for (int round = 0; round < size * 20; round++)
            {
                KType key = cast(rnd.nextInt(size));
                if (rnd.nextInt(50) == 0) {
                  key = Intrinsics.empty();
                }

                if (rnd.nextBoolean())
                {
                    if (rnd.nextBoolean()) {
                        int index = set.indexOf(key);
                        if (set.indexExists(index)) {
                            set.indexReplace(index, key);
                        } else {
                            set.indexInsert(index, key);
                        }
                    } else {
                        set.add(key);
                    }
                    other.add(key);

                    assertTrue(set.contains(key));
                    assertTrue(set.indexExists(set.indexOf(key)));
                }
                else
                {
                    assertEquals(other.contains(key), set.contains(key));
                    boolean removed;
                    if (set.contains(key) && rnd.nextBoolean()) {
                        set.indexRemove(set.indexOf(key));
                        removed = true;
                    } else {
                        removed = set.remove(key);
                    }
                    assertEquals(other.remove(key), removed);
                }

                assertEquals(other.size(), set.size());
            }
        }
    }

    /* */
    @Test
    public void testHashCodeEquals()
    {
        KTypeHashSet<KType> l0 = new KTypeHashSet<>();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, new KTypeHashSet<>());

        KTypeHashSet<KType> l1 = KTypeHashSet.from(k1, k2, k3);
        KTypeHashSet<KType> l2 = KTypeHashSet.from(k1, k2);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testHashCodeWithNulls()
    {
        KTypeHashSet<KType> l1 = KTypeHashSet.from(k1, null, k3);
        KTypeHashSet<KType> l2 = KTypeHashSet.from(k1, null);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }
    /*! #end !*/

    @Test
    public void testClone()
    {
        this.set.addAll(key1, key2, key3);

        KTypeHashSet<KType> cloned = set.clone();
        cloned.removeAll(key1);

        assertSortedListEquals(set.toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.toArray(), key2, key3);
    }
    
    /* */
    @Test
    public void testEqualsSameClass()
    {
      KTypeHashSet<KType> l1 = KTypeHashSet.from(k1, k2, k3);
      KTypeHashSet<KType> l2 = KTypeHashSet.from(k1, k2, k3);
      KTypeHashSet<KType> l3 = KTypeHashSet.from(k1, k2, k4);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeHashSet<KType> {
        };

        KTypeHashSet<KType> l1 = KTypeHashSet.from(k1, k2, k3);
        KTypeHashSet<KType> l2 = new Sub();
        KTypeHashSet<KType> l3 = new Sub();
        l2.addAll(l1);
        l3.addAll(l1);

        Assertions.assertThat(l2).isEqualTo(l3);
        Assertions.assertThat(l1).isNotEqualTo(l2);
    }
    
    /* */
    @Test
    public void testForEachPredicate()
    {
      final KTypeHashSet<KType> set = KTypeHashSet.from(keyE, k1, k2, k3);
      final KTypeHashSet<KType> other = new KTypeHashSet<>();
      set.forEach(new KTypePredicate<KType>() {
        @Override
        public boolean apply(KType value) {
          other.add(value);
          return true;
        }
      });
      Assertions.assertThat(other).isEqualTo(set);
    }
    
    /* */
    @Test
    public void testForEachProcedure()
    {
      final KTypeHashSet<KType> set = KTypeHashSet.from(keyE, k1, k2, k3);
      final KTypeHashSet<KType> other = new KTypeHashSet<>();
      set.forEach(new KTypeProcedure<KType>() {
        @Override
        public void apply(KType value) {
          other.add(value);
        }
      });
      Assertions.assertThat(other).isEqualTo(set);
    }
}
