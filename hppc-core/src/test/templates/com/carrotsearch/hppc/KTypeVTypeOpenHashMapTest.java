package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;

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
        if (this.map != null)
        {
            int occupied = 0;
            for (int i = 0; i < this.map.keys.length; i++)
            {
                if (Intrinsics.equalsKTypeDefault(this.map.keys[i]))
                {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.<KType> defaultKTypeValue(), this.map.keys[i]);
                    /*! #end !*/
                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.values[i]);
                    /*! #end !*/
                }
                else
                {
                    occupied++;
                }
            }

            if (this.map.allocatedDefaultKey) {

                //try to reach the key by contains()
                Assert.assertTrue(this.map.containsKey(Intrinsics.<KType> defaultKTypeValue()));

                //check slot
                Assert.assertEquals(-2, this.map.lslot());

                occupied++;
            }

            Assert.assertEquals(occupied, this.map.size());
        }
    }

    /**
     * Convert to target type from an integer used to test stuff.
     */
    protected VType vcast(final int value)
    {
        /*! #if ($TemplateOptions.VTypePrimitive)
            return (VType) value;
            #else !*/
        @SuppressWarnings("unchecked")
        final VType v = (VType) (Object) value;
        return v;
        /*! #end !*/
    }

    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    protected VType[] newvArray(@SuppressWarnings("unchecked")
    final VType... elements)
    {
        return elements;
    }

    private void assertSameMap(
            final KTypeVTypeMap<KType, VType> c1,
            final KTypeVTypeMap<KType, VType> c2)
    {
        Assert.assertEquals(c1.size(), c2.size());

        c1.forEach(new KTypeVTypeProcedure<KType, VType>()
                {
            @Override
            public void apply(final KType key, final VType value)
            {
                Assert.assertTrue(c2.containsKey(key));
                TestUtils.assertEquals2(value, c2.get(key));
            }
                });
    }

    /* */
    @Test
    public void testCloningConstructor()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        assertSameMap(this.map, KTypeVTypeOpenHashMap.from(this.map));
        assertSameMap(this.map, new KTypeVTypeOpenHashMap<KType, VType>(this.map));
    }

    /* */
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testFromArrays()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeOpenHashMap<KType, VType> map2 = KTypeVTypeOpenHashMap.from(
                newArray(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        assertSameMap(this.map, map2);
    }

    @Test
    public void testGetOrDefault()
    {
        this.map.put(this.key2, this.value2);
        Assert.assertTrue(this.map.containsKey(this.key2));

        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.getOrDefault(this.key1, this.value3));
        TestUtils.assertEquals2(this.value3, this.map.getOrDefault(this.key3, this.value3));
        this.map.remove(this.key1);
        TestUtils.assertEquals2(this.value3, this.map.getOrDefault(this.key1, this.value3));

        // Make sure lslot wasn't touched.
        TestUtils.assertEquals2(this.value2, this.map.lget());
    }

    /* */
    @Test
    public void testPut()
    {
        this.map.put(this.key1, this.value1);

        Assert.assertTrue(this.map.containsKey(this.key1));
        TestUtils.assertEquals2(this.value1, this.map.lget());
        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testLPut()
    {
        this.map.put(this.key1, this.value2);
        if (this.map.containsKey(this.key1))
            this.map.lset(this.value3);

        Assert.assertTrue(this.map.containsKey(this.key1));
        TestUtils.assertEquals2(this.value3, this.map.lget());
        TestUtils.assertEquals2(this.value3, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.put(this.key1, this.value3));
        TestUtils.assertEquals2(this.value3, this.map.get(this.key1));
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(RandomizedTest.randomLong());
        final HashSet<Object> values = new HashSet<Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final int v = rnd.nextInt();
            final boolean hadKey = values.contains(cast(v));
            values.add(cast(v));

            Assert.assertEquals(hadKey, this.map.containsKey(cast(v)));
            this.map.put(cast(v), vcast(v));
            Assert.assertEquals(values.size(), this.map.size());
        }
        Assert.assertEquals(values.size(), this.map.size());
    }

    /* */
    @Test
    public void testPutAll()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);

        final KTypeVTypeOpenHashMap<KType, VType> map2 =
                new KTypeVTypeOpenHashMap<KType, VType>();

        map2.put(this.key2, this.value2);
        map2.put(this.key3, this.value1);

        // One new key (key3).
        Assert.assertEquals(1, this.map.putAll(map2));

        // Assert the value under key2 has been replaced.
        TestUtils.assertEquals2(this.value2, this.map.get(this.key2));

        // And key3 has been added.
        TestUtils.assertEquals2(this.value1, this.map.get(this.key3));
        Assert.assertEquals(3, this.map.size());
    }

    /* */
    @Test
    public void testPutIfAbsent()
    {
        Assert.assertTrue(this.map.putIfAbsent(this.key1, this.value1));
        Assert.assertFalse(this.map.putIfAbsent(this.key1, this.value2));
        TestUtils.assertEquals2(this.value1, this.map.get(this.key1));
    }

    /*! #if ($TemplateOptions.VTypePrimitive)
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value1 + value2, map.putOrAdd(key1, value1, value2));
    }
    #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive)
    @Test
    public void testAddTo()
    {
        assertEquals2(value1, map.addTo(key1, value1));
        assertEquals2(value1 + value2, map.addTo(key1, value2));
    }
    #end !*/

    /* */
    @Test
    public void testRemove()
    {
        this.map.put(this.key1, this.value1);
        TestUtils.assertEquals2(this.value1, this.map.remove(this.key1));
        TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.remove(this.key1));
        Assert.assertEquals(0, this.map.size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);
    }

    /* */
    @Test
    public void testRemoveAllWithContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(newArray(this.key2, this.key3, this.key4));

        this.map.removeAll(list2);
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        this.map.removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType value)
            {
                return value == KTypeVTypeOpenHashMapTest.this.key2 || value == KTypeVTypeOpenHashMapTest.this.key3;
            }
                });
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testRemoveViaKeySetView()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        this.map.keys().removeAll(new KTypePredicate<KType>()
                {
            @Override
            public boolean apply(final KType value)
            {
                return value == KTypeVTypeOpenHashMapTest.this.key2 || value == KTypeVTypeOpenHashMapTest.this.key3;
            }
                });
        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key1));
    }

    /* */
    @Test
    public void testMapsIntersection()
    {
        final KTypeVTypeOpenHashMap<KType, VType> map2 =
                KTypeVTypeOpenHashMap.newInstance();

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.put(this.key3, this.value1);

        map2.put(this.key2, this.value1);
        map2.put(this.key4, this.value1);

        Assert.assertEquals(2, this.map.keys().retainAll(map2.keys()));

        Assert.assertEquals(1, this.map.size());
        Assert.assertTrue(this.map.containsKey(this.key2));
    }

    /* */
    @Test
    public void testMapKeySet()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.key1, this.key2, this.key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<KType> c : this.map.keys())
        {
            if (c.index == -1) {

                TestUtils.assertEquals2(Intrinsics.<KType> defaultKTypeValue(), c.value);
                counted++;
                continue;
            }

            TestUtils.assertEquals2(this.map.keys[c.index], c.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());
    }

    /* */
    @Test
    public void testClear()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value1);
        this.map.clear();
        Assert.assertEquals(0, this.map.size());

        // These are internals, but perhaps worth asserting too.
        Assert.assertEquals(0, this.map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testRoundCapacity()
    {
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE));
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE / 2 + 1));
        Assert.assertEquals(0x40000000, HashContainerUtils.roundCapacity(Integer.MAX_VALUE / 2));
        Assert.assertEquals(KTypeVTypeOpenHashMap.MIN_CAPACITY, HashContainerUtils.roundCapacity(0));
        Assert.assertEquals(Math.max(4, KTypeVTypeOpenHashMap.MIN_CAPACITY), HashContainerUtils.roundCapacity(3));
    }

    /* */
    @Test
    public void testIterable()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);
        this.map.remove(this.key2);

        int count = 0;
        for (final KTypeVTypeCursor<KType, VType> cursor : this.map)
        {
            if (cursor.index == -1) {

                TestUtils.assertEquals2(Intrinsics.<KType> defaultKTypeValue(), cursor.key);
                TestUtils.assertEquals2(this.map.defaultKeyValue, cursor.value);
                count++;
                continue;
            }
            count++;
            Assert.assertTrue(this.map.containsKey(cursor.key));
            TestUtils.assertEquals2(cursor.value, this.map.get(cursor.key));

            TestUtils.assertEquals2(cursor.value, this.map.values[cursor.index]);
            TestUtils.assertEquals2(cursor.key, this.map.keys[cursor.index]);

        }
        Assert.assertEquals(count, this.map.size());

        this.map.clear();
        Assert.assertFalse(this.map.iterator().hasNext());
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        this.map = new KTypeVTypeOpenHashMap<KType, VType>(1, 1f);

        // Fit in the byte key range.
        final int capacity = 0x80;
        final int max = capacity - 1;
        for (int i = 1; i <= max; i++)
        {
            this.map.put(cast(i), this.value1);
        }

        // Still not expanded.
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);
        // Won't expand (existing key).
        this.map.put(cast(1), this.value2);
        Assert.assertEquals(capacity, this.map.keys.length);
        // Expanded.
        this.map.put(cast(0xff), this.value2);
        Assert.assertEquals(2 * capacity, this.map.keys.length);
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        this.map = new KTypeVTypeOpenHashMap<KType, VType>(1, 1f);
        final int capacity = 0x80;
        final int max = capacity - 2;
        for (int i = 1; i <= max; i++)
        {
            this.map.put(cast(i), this.value1);
        }
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        // Non-existent key.
        this.map.remove(cast(max + 1));
        Assert.assertFalse(this.map.containsKey(cast(max + 1)));
        TestUtils.assertEquals2(Intrinsics.defaultVTypeValue(), this.map.get(cast(max + 1)));

        // Should not expand because we're replacing an existing element.
        this.map.put(cast(1), this.value2);
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        this.map.putIfAbsent(cast(1), this.value3);
        Assert.assertEquals(max, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);

        // Remove from a full map.
        this.map.remove(cast(1));
        Assert.assertEquals(max - 1, this.map.size());
        Assert.assertEquals(capacity, this.map.keys.length);
    }

    /* */
    @Test
    public void testHalfLoadFactor()
    {
        this.map = new KTypeVTypeOpenHashMap<KType, VType>(1, 0.5f);

        final int capacity = 0x80;
        final int max = capacity - 1;
        for (int i = 1; i <= max; i++)
        {
            this.map.put(cast(i), this.value1);
        }

        Assert.assertEquals(max, this.map.size());
        // Still not expanded.
        Assert.assertEquals(2 * capacity, this.map.keys.length);
        // Won't expand (existing key);
        this.map.put(cast(1), this.value2);
        Assert.assertEquals(2 * capacity, this.map.keys.length);
        // Expanded.
        this.map.put(cast(0xff), this.value1);
        Assert.assertEquals(4 * capacity, this.map.keys.length);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final KTypeVTypeOpenHashMap<KType, VType> l0 =
                new KTypeVTypeOpenHashMap<KType, VType>();
        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0, new KTypeVTypeOpenHashMap<KType, VType>());

        final KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
                newArray(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        final KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
                newArray(this.key2, this.key1, this.key3),
                newvArray(this.value2, this.value1, this.value3));

        final KTypeVTypeOpenHashMap<KType, VType> l3 = KTypeVTypeOpenHashMap.from(
                newArray(this.key1, this.key2),
                newvArray(this.value2, this.value1));

        Assert.assertEquals(l1.hashCode(), l2.hashCode());
        Assert.assertEquals(l1, l2);

        Assert.assertFalse(l1.equals(l3));
        Assert.assertFalse(l2.equals(l3));
    }

    /* */
    @Test
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public void testHashCodeEqualsDifferentPerturbance()
    {
        final KTypeVTypeOpenHashMap<KType, VType> l0 =
                new KTypeVTypeOpenHashMap<KType, VType>() {
                    @Override
                    protected int computePerturbationValue(final int capacity)
                    {
                        return 0xDEADBEEF;
                    }
                };

        final KTypeVTypeOpenHashMap<KType, VType> l1 =
                new KTypeVTypeOpenHashMap<KType, VType>() {
                    @Override
                    protected int computePerturbationValue(final int capacity)
                    {
                        return 0xCAFEBABE;
                    }
                };

        Assert.assertEquals(0, l0.hashCode());
        Assert.assertEquals(l0.hashCode(), l1.hashCode());
        Assert.assertEquals(l0, l1);

        final KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
                newArray(this.key1, this.key2, this.key3),
                newvArray(this.value1, this.value2, this.value3));

        l0.putAll(l2);
        l1.putAll(l2);

        Assert.assertEquals(l0.hashCode(), l1.hashCode());
        Assert.assertEquals(l0, l1);
    }

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testBug_HPPC37()
    {
        final KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
                newArray(this.key1),
                newvArray(this.value1));

        final KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
                newArray(this.key2),
                newvArray(this.value1));

        Assert.assertFalse(l1.equals(l2));
        Assert.assertFalse(l2.equals(l1));
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        this.map.put(null, vcast(10));
        TestUtils.assertEquals2(vcast(10), this.map.get(null));
        Assert.assertTrue(this.map.containsKey(null));
        TestUtils.assertEquals2(vcast(10), this.map.lget());
        TestUtils.assertEquals2(null, this.map.lkey());
        this.map.remove(null);
        Assert.assertEquals(0, this.map.size());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    @SuppressWarnings("unchecked")
    public void testLkey()
    {
        this.map.put(this.key1, vcast(10));
        Assert.assertTrue(this.map.containsKey(this.key1));
        Assert.assertSame(this.key1, this.map.lkey());
        final KType key1_ = (KType) new Integer(1);
        Assert.assertNotSame(this.key1, key1_);
        Assert.assertEquals(this.key1, key1_);
        Assert.assertTrue(this.map.containsKey(key1_));
        Assert.assertSame(this.key1, this.map.lkey());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        this.map.put(this.key1, null);
        Assert.assertEquals(null, this.map.get(this.key1));
        Assert.assertTrue(this.map.containsKey(this.key1));
        this.map.remove(this.key1);
        Assert.assertFalse(this.map.containsKey(this.key1));
        Assert.assertEquals(0, this.map.size());
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
        final Random rnd = new Random(RandomizedTest.randomLong());
        final java.util.HashMap<KType, VType> other =
                new java.util.HashMap<KType, VType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            this.map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                final KType key = cast(rnd.nextInt(size));
                final VType value = vcast(rnd.nextInt());

                if (rnd.nextBoolean())
                {
                    this.map.put(key, value);
                    other.put(key, value);

                    Assert.assertEquals(value, this.map.get(key));
                    Assert.assertTrue(this.map.containsKey(key));
                    Assert.assertEquals(value, this.map.lget());
                }
                else
                {
                    Assert.assertEquals(other.remove(key), this.map.remove(key));
                }

                Assert.assertEquals(other.size(), this.map.size());
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
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value3);

        final KTypeVTypeOpenHashMap<KType, VType> cloned = this.map.clone();
        cloned.remove(this.key1);

        TestUtils.assertSortedListEquals(this.map.keys().toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.keys().toArray(), this.key2, this.key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(
                (int[].class.isInstance(this.map.keys) ||
                        short[].class.isInstance(this.map.keys) ||
                        byte[].class.isInstance(this.map.keys) ||
                        long[].class.isInstance(this.map.keys) ||
                        Object[].class.isInstance(this.map.keys)) &&
                        (int[].class.isInstance(this.map.values) ||
                                byte[].class.isInstance(this.map.values) ||
                                short[].class.isInstance(this.map.values) ||
                                long[].class.isInstance(this.map.values) ||
                        Object[].class.isInstance(this.map.values)));

        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);

        String asString = this.map.toString();
        asString = asString.replaceAll("[^0-9]", "");
        final char[] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        Assert.assertEquals("1122", new String(asCharArray));
    }

    @Test
    public void testAddRemoveSameHashCollision()
    {
        // This test is only applicable to selected key types.
        Assume.assumeTrue(
                int[].class.isInstance(this.map.keys) ||
                        long[].class.isInstance(this.map.keys) ||
                        Object[].class.isInstance(this.map.keys));

        final IntArrayList hashChain = TestUtils.generateMurmurHash3CollisionChain(0x1fff, 0x7e, 0x1fff / 3);

        /*
         * Add all of the conflicting keys to a map.
         */
        for (final IntCursor c : hashChain)
            this.map.put(cast(c.value), this.value1);

        Assert.assertEquals(hashChain.size(), this.map.size());

        /*
         * Add some more keys (random).
         */
        final Random rnd = new Random(RandomizedTest.randomLong());
        final IntSet chainKeys = IntOpenHashSet.from(hashChain);
        final IntSet differentKeys = new IntOpenHashSet();
        while (differentKeys.size() < 500)
        {
            final int k = rnd.nextInt();
            if (!chainKeys.contains(k) && !differentKeys.contains(k))
                differentKeys.add(k);
        }

        for (final IntCursor c : differentKeys)
            this.map.put(cast(c.value), this.value2);

        Assert.assertEquals(hashChain.size() + differentKeys.size(), this.map.size());

        /*
         * Verify the map contains all of the conflicting keys.
         */
        for (final IntCursor c : hashChain)
            TestUtils.assertEquals2(this.value1, this.map.get(cast(c.value)));

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys)
            TestUtils.assertEquals2(this.value2, this.map.get(cast(c.value)));

        /*
         * Iteratively remove the keys, from first to last.
         */
        for (final IntCursor c : hashChain)
            TestUtils.assertEquals2(this.value1, this.map.remove(cast(c.value)));

        Assert.assertEquals(differentKeys.size(), this.map.size());

        /*
         * Verify the map contains all the other keys.
         */
        for (final IntCursor c : differentKeys)
            TestUtils.assertEquals2(this.value2, this.map.get(cast(c.value)));
    }

    /* */
    @Test
    public void testMapValues()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value3);

        this.map.clear();
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value2);
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);
    }

    /* */
    @Test
    public void testMapValuesIterator()
    {
        this.map.put(this.key1, this.value3);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value1);

        int counted = 0;
        for (final KTypeCursor<VType> c : this.map.values())
        {
            if (c.index == -1) {

                TestUtils.assertEquals2(this.map.defaultKeyValue, c.value);
                counted++;
                continue;
            }
            TestUtils.assertEquals2(this.map.values[c.index], c.value);
            counted++;
        }
        Assert.assertEquals(counted, this.map.size());
    }

    /* */
    @Test
    public void testMapValuesContainer()
    {
        this.map.put(this.key1, this.value1);
        this.map.put(this.key2, this.value2);
        this.map.put(this.key3, this.value2);

        // contains()
        for (final KTypeVTypeCursor<KType, VType> c : this.map)
            Assert.assertTrue(this.map.values().contains(c.value));
        Assert.assertFalse(this.map.values().contains(this.value3));

        Assert.assertEquals(this.map.isEmpty(), this.map.values().isEmpty());
        Assert.assertEquals(this.map.size(), this.map.values().size());

        final KTypeArrayList<VType> values = new KTypeArrayList<VType>();
        this.map.values().forEach(new KTypeProcedure<VType>()
        {
            @Override
            public void apply(final VType value)
            {
                values.add(value);
            }
        });
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);

        values.clear();
        this.map.values().forEach(new KTypePredicate<VType>()
        {
            @Override
            public boolean apply(final VType value)
            {
                values.add(value);
                return true;
            }
        });
        TestUtils.assertSortedListEquals(this.map.values().toArray(), this.value1, this.value2, this.value2);
    }

    /**
     * Tests that instances created with the <code>newInstanceWithExpectedSize</code>
     * static factory methods do not have to resize to hold the expected number of elements.
     */
    @Test
    public void testExpectedSizeInstanceCreation()
    {
        final KTypeVTypeOpenHashMap<KType, VType> fixture =
                KTypeVTypeOpenHashMap.newInstanceWithExpectedSize(KTypeVTypeOpenHashMap.DEFAULT_CAPACITY);

        Assert.assertEquals(KTypeVTypeOpenHashMap.DEFAULT_CAPACITY, this.map.keys.length);
        Assert.assertEquals(KTypeVTypeOpenHashMap.DEFAULT_CAPACITY * 2, fixture.keys.length);

        for (int i = 0; i < KTypeOpenHashSet.DEFAULT_CAPACITY; i++)
        {
            final KType key = cast(i);
            final VType value = vcast(i);
            this.map.put(key, value);
            fixture.put(key, value);
        }

        Assert.assertEquals(KTypeVTypeOpenHashMap.DEFAULT_CAPACITY * 2, this.map.keys.length);
        Assert.assertEquals(KTypeVTypeOpenHashMap.DEFAULT_CAPACITY * 2, fixture.keys.length);
    }
}
