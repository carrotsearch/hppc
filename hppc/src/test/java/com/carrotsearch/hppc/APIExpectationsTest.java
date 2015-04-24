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
    public void testRemoveAllFromMap()
    {
        ObjectIntHashMap<Integer> list = new ObjectIntHashMap<>();
        list.put(1, 1);
        list.put(2, 2);
        list.put(3, 3);

        // Same type.
        ObjectHashSet<Integer> other1 = new ObjectHashSet<>();
        other1.add(1);
        list.removeAll(other1);

        // Supertype.
        ObjectArrayList<Integer> other2 = new ObjectArrayList<>();
        other2.add(1);
        list.removeAll(other2);

        // Object
        ObjectArrayList<Object> other3 = new ObjectArrayList<>();
        other3.add(1);
        list.removeAll(other3);        
    }

    @Test
    public void testRemoveAllWithLookupContainer()
    {
        ObjectArrayList<Integer> list = new ObjectArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        
        // Same type.
        ObjectHashSet<Integer> other1 = new ObjectHashSet<>();
        other1.add(1);
        list.removeAll(other1);

        // Supertype.
        ObjectHashSet<Number> other2 = new ObjectHashSet<>();
        other2.add(1);
        list.removeAll(other2);

        // Object
        ObjectHashSet<Object> other3 = new ObjectHashSet<>();
        other3.add(1);
        list.removeAll(other3);        
    }

    @Test
    public void testToArrayWithClass()
    {
        ObjectArrayDeque<Integer> l1 = ObjectArrayDeque.from(1, 2, 3);
        Integer[] result1 = l1.toArray(Integer.class);
        assertArrayEquals(new Integer [] {1, 2, 3}, result1);
        
        Number[] result2 = l1.toArray(Number.class);
        Assertions.assertThat(result2).isExactlyInstanceOf(Number[].class);
        assertArrayEquals(new Number [] {1, 2, 3}, result2);
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
          protected boolean equals(Object k1, Object k2) {
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
          protected boolean equals(Object k1, Object k2) {
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
        t1 = IntHashSet.from(1, 2, 3).toArray();

        t1 = IntObjectHashMap.from(
            new int [] {1, 2}, new Long [] {1L, 2L}).keys().toArray();
    }

    @Test
    @SuppressWarnings("unused")
    public void testNewInstance()
    {
        IntArrayList v1 = new IntArrayList();
        ObjectArrayList<Integer> v2 = new ObjectArrayList<>();
        ObjectArrayList<Long> v3 = new ObjectArrayList<>();
        
        IntStack v4 = new IntStack();
        ObjectStack<Integer> v5 = new ObjectStack<>();
        ObjectStack<Long> v6 = new ObjectStack<>();
        
        IntHashSet v7 = new IntHashSet();
        ObjectHashSet<Integer> v8 = new ObjectHashSet<>();
        ObjectHashSet<Long> v9 = new ObjectHashSet<>();
        
        IntArrayDeque v10 = new IntArrayDeque();
        ObjectArrayDeque<Integer> v11 = new ObjectArrayDeque<>();
        ObjectArrayDeque<Long> v12 = new ObjectArrayDeque<>();

        IntIntHashMap v13 = new IntIntHashMap();
        ObjectIntHashMap<Integer> v14 = new ObjectIntHashMap<>();
        IntObjectHashMap<Integer> v15 = new IntObjectHashMap<>();
    }

    @Test
    public void testObjectToArray()
    {
        isObjectArray(ObjectArrayList.from(1, 2, 3).toArray());
        isObjectArray(ObjectStack.from(1, 2, 3).toArray());
        isObjectArray(ObjectArrayDeque.from(1, 2, 3).toArray());
        isObjectArray(ObjectHashSet.from(1, 2, 3).toArray());

        isObjectArray(ObjectObjectHashMap.from(
            new Integer [] {1, 2}, new Long [] {1L, 2L}).keys().toArray());
    }

    @Test
    public void testWithClassToArray()
    {
        isIntegerArray(ObjectArrayList.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectStack.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectArrayDeque.from(1, 2, 3).toArray(Integer.class));
        isIntegerArray(ObjectHashSet.from(1, 2, 3).toArray(Integer.class));

        isIntegerArray(ObjectObjectHashMap.from(
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
    	ObjectIntHashMap<Integer> map = new ObjectIntHashMap<>();

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
        IntHashSet l0 = new IntHashSet(0, 0.5, HashOrderMixing.constant(0xcafe));
        IntHashSet l1 = new IntHashSet(0, 0.5, HashOrderMixing.constant(0xbabe));

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
