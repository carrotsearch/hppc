package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Various API expectations from generated classes.
 */
public class APIExpectationsTest extends RandomizedTest
{
    public volatile int [] t1;

    @Test
    public void testToArrayWithClass()
    {
        ObjectArrayDeque<Integer> l1 = ObjectArrayDeque.from(1, 2, 3);
        Integer[] result = l1.toArray(Integer.class);
        assertArrayEquals(new Integer [] {1, 2, 3}, result); // dummy
    }

    @Test
    public void testEqualElementsDifferentGenericType()
    {
        ObjectArrayList<Integer> l1 = new ObjectArrayList<Integer>();
        ObjectArrayList<Number> l2 = new ObjectArrayList<Number>();
        
        Assertions.assertThat(l1.equalElements(l2)).isTrue();
        Assertions.assertThat(l2.equalElements(l1)).isTrue();
    }

    @Test
    public void testArrayListEqualsWithOverridenComparisonMethod()
    {
        class IntegerIdentityList extends ObjectArrayList<Integer> {
          @Override
          protected boolean sameKeys(Integer k1, Integer k2) {
            return k1 == k2;
          }
        };

        IntegerIdentityList l1 = new IntegerIdentityList();
        IntegerIdentityList l2 = new IntegerIdentityList();
        IntegerIdentityList l3 = new IntegerIdentityList();

        l1.add(1, 2, 3);
        l2.add(1, 2, 3);
        l3.add(1, 2, new Integer(3));

        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1.hashCode()).isEqualTo(l3.hashCode());
        Assertions.assertThat(l1.equalElements(l2)).isTrue();
        Assertions.assertThat(l1.equalElements(l3)).isFalse();
    }

    @Test
    public void testArrayDequeEqualsWithOverridenComparisonMethod()
    {
        class IntegerIdentityDeque extends ObjectArrayDeque<Integer> {
          @Override
          protected boolean sameKeys(Integer k1, Integer k2) {
            return k1 == k2;
          }
        };

        IntegerIdentityDeque l1 = new IntegerIdentityDeque();
        IntegerIdentityDeque l2 = new IntegerIdentityDeque();
        IntegerIdentityDeque l3 = new IntegerIdentityDeque();

        l1.addLast(1, 2, 3);
        l2.addLast(1, 2, 3);
        l3.addLast(1, 2, new Integer(3));

        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1.hashCode()).isEqualTo(l3.hashCode());
        Assertions.assertThat(l1.equalElements(l2)).isTrue();
        Assertions.assertThat(l1.equalElements(l3)).isFalse();
    }

    @Test
    public void testPrimitiveToArray()
    {
        t1 = IntArrayList.from(1, 2, 3).toArray();
        t1 = IntStack.from(1, 2, 3).toArray();
        t1 = IntArrayDeque.from(1, 2, 3).toArray();
        t1 = IntOpenHashSet.from(1, 2, 3).toArray();

        t1 = IntObjectOpenHashMap.from(
            new int [] {1, 2}, new Long [] {1L, 2L}).keys().toArray();
    }

    @Test
    @RequiresLargeMemory
    public void testSizeLimitByteArrayList() {
        ByteArrayList list = new ByteArrayList(0, new ArraySizingStrategy()
        {
            final BoundedProportionalArraySizingStrategy delegate = new BoundedProportionalArraySizingStrategy();

            @Override
            public int round(int capacity)
            {
                return delegate.round(capacity);
            }
            
            @Override
            public int grow(int currentBufferLength, int elementsCount, int expectedAdditions)
            {
                int grow = delegate.grow(currentBufferLength, elementsCount, expectedAdditions);
                // System.out.println("Resizing to: " + Integer.toHexString(grow) + " " + grow);
                return grow;
            }
        });

        try {
            while (true) {
                list.add((byte) 0);
            }
        } catch (AssertionError e) {
            assertEquals(0x7fffffff, list.size());
        }
    }

    @Test
    @RequiresLargeMemory
    public void testSizeLimitByteQueue() {
        ByteArrayDeque queue = new ByteArrayDeque(1, new ArraySizingStrategy()
        {
            final BoundedProportionalArraySizingStrategy delegate = new BoundedProportionalArraySizingStrategy();

            @Override
            public int round(int capacity)
            {
                return delegate.round(capacity);
            }
            
            @Override
            public int grow(int currentBufferLength, int elementsCount, int expectedAdditions)
            {
                int grow = delegate.grow(currentBufferLength, elementsCount, expectedAdditions);
                // System.out.println("Resizing to: " + Integer.toHexString(grow) + " " + grow);
                return grow;
            }
        });

        try {
            while (true) {
                queue.addLast((byte) 0);
            }
        } catch (AssertionError e) {
            assertEquals(0x7fffffff /* Account for an empty slot. */ - 1, queue.size());
        }
    }

    @Test
    @SuppressWarnings("unused")
    public void testNewInstance()
    {
        IntArrayList v1 = IntArrayList.newInstance();
        ObjectArrayList<Integer> v2 = ObjectArrayList.newInstance();
        ObjectArrayList<Long> v3 = ObjectArrayList.newInstance();
        
        IntStack v4 = IntStack.newInstance();
        ObjectStack<Integer> v5 = ObjectStack.newInstance();
        ObjectStack<Long> v6 = ObjectStack.newInstance();
        
        IntOpenHashSet v7 = new IntOpenHashSet();
        ObjectOpenHashSet<Integer> v8 = new ObjectOpenHashSet<>();
        ObjectOpenHashSet<Long> v9 = new ObjectOpenHashSet<>();
        
        IntArrayDeque v10 = IntArrayDeque.newInstance();
        ObjectArrayDeque<Integer> v11 = ObjectArrayDeque.newInstance();
        ObjectArrayDeque<Long> v12 = ObjectArrayDeque.newInstance();

        IntIntOpenHashMap v13 = IntIntOpenHashMap.newInstance();
        ObjectIntOpenHashMap<Integer> v14 = ObjectIntOpenHashMap.newInstance();
        IntObjectOpenHashMap<Integer> v15 = IntObjectOpenHashMap.newInstance();
    }

    @Test
    public void testObjectToArray()
    {
        isObjectArray(ObjectArrayList.from(1, 2, 3).toArray());
        isObjectArray(ObjectStack.from(1, 2, 3).toArray());
        isObjectArray(ObjectArrayDeque.from(1, 2, 3).toArray());
        isObjectArray(ObjectOpenHashSet.from(1, 2, 3).toArray());

        isObjectArray(ObjectObjectOpenHashMap.from(
            new Integer [] {1, 2}, new Long [] {1L, 2L}).keys().toArray());
    }

    @Test
    public void testWithClassToArray()
    {
        isIntegerArray(ObjectArrayList.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectStack.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectArrayDeque.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectOpenHashSet.from(1, 2, 3).toArray(Integer.class));

        isIntegerArray(ObjectObjectOpenHashMap.from(
            new Integer [] {1, 2}, new Long [] {1L, 2L}).keys().toArray(Integer.class));
    }
    
    @Test
    public void testWildcards()
    {
        ObjectArrayList<? extends Number> t = ObjectArrayList.from(1, 2, 3);
        isTypeArray(Number.class, t.toArray(Number.class));

        t = ObjectArrayList.from(1L, 2L, 3L);
        isTypeArray(Number.class, t.toArray(Number.class));
    }

    @Test
    public void testPutOrAddOnEqualKeys()
    {
    	ObjectIntOpenHashMap<Integer> map = ObjectIntOpenHashMap.newInstance();

    	Integer k1  = 1;
    	Integer k1b = new Integer(k1.intValue()); 

    	assertTrue(k1 != k1b);
        assertEquals2(1, map.putOrAdd(k1, 1, 2));
        assertTrue(map.containsKey(k1b));
        assertEquals2(3, map.putOrAdd(k1b, 1, 2));
    }    

    /*
     * Even with two different hash distribution keys the
     * result of hashCode() should be the same. 
     */
    @Test
    public void testHashCodeOverflowIdentical()
    {
        IntOpenHashSet l0 = new IntOpenHashSet(0, 0.5, HashOrderMixing.constant(0xcafe));
        IntOpenHashSet l1 = new IntOpenHashSet(0, 0.5, HashOrderMixing.constant(0xbabe));

        for (int i = 100000 + randomIntBetween(0, 100000); i-- > 0;) {
            l0.add(i);
            l1.add(i);
        }

        assertEquals(l0.hashCode(), l1.hashCode());
        assertEquals(l0, l1);
    }
    
    /**
     * Check if the array is indeed of Object component type.
     */
    private void isObjectArray(Object [] array)
    {
        isTypeArray(Object.class, array);
    }
    
    /**
     * 
     */
    private void isTypeArray(Class<?> clazz, Object [] array)
    {
        assertEquals(clazz, array.getClass().getComponentType());
    }

    /**
     * Check if the array is indeed of Integer component type.
     */
    private void isIntegerArray(Integer [] array)
    {
        isTypeArray(Integer.class, array);
    }
}
