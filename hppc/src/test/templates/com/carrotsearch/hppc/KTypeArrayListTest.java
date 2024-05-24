package com.carrotsearch.hppc;

import static org.junit.Assert.*;
import static com.carrotsearch.hppc.TestUtils.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

/**
 * Unit tests for {@link KTypeArrayList}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayListTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayList<KType> list;

    /* */
    @Before
    public void initialize()
    {
        list = new KTypeArrayList<>();
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (list != null)
        {
            for (int i = list.elementsCount; i < list.buffer.length; i++)
                assertTrue(Intrinsics.<KType> empty() == list.buffer[i]);
        }
    }
    /*! #end !*/

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        assertEquals(0, list.size());
    }

    /* */
    @Test
    public void testAdd()
    {
        list.add(key1, key2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddTwoArgs()
    {
        list.add(key1, key2);
        list.add(key3, key4);
        assertListEquals(list.toArray(), 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddArray()
    {
        list.add(asArray(0, 1, 2, 3), 1, 2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddVarArg()
    {
        list.add(asArray(0, 1, 2, 3));
        list.add(key4, key5, key6, key7);
        assertListEquals(list.toArray(), 0, 1, 2, 3, 4, 5, 6, 7);
    }

    /* */
    @Test
    public void testAddAll()
    {
        KTypeArrayList<KType> list2 = new KTypeArrayList<>();
        list2.add(asArray(0, 1, 2));

        list.addAll(list2);
        list.addAll(list2);

        assertListEquals(list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /* */
    @Test
    public void testInsert()
    {
        list.insert(0, k1);
        list.insert(0, k2);
        list.insert(2, k3);
        list.insert(1, k4);

        assertListEquals(list.toArray(), 2, 4, 1, 3);
    }

    /* */
    @Test
    public void testSet()
    {
        list.add(asArray(0, 1, 2));

        assertEquals2(0, list.set(0, k3));
        assertEquals2(1, list.set(1, k4));
        assertEquals2(2, list.set(2, k5));

        assertListEquals(list.toArray(), 3, 4, 5);
    }
    
    /* */
    @Test
    public void testRemoveAt()
    {
        list.add(asArray(0, 1, 2, 3, 4));

        list.removeAt(0);
        list.removeAt(2);
        list.removeAt(1);

        assertListEquals(list.toArray(), 1, 4);
    }

    /* */
    @Test
    public void testRemoveLast() {
        list.add(asArray(0, 1, 2, 3, 4));

        assertEquals2(4, list.removeLast());
        assertEquals2(4, list.size());
        assertListEquals(list.toArray(), 0, 1, 2, 3);
        assertEquals2(3, list.removeLast());
        assertEquals2(3, list.size());
        assertListEquals(list.toArray(), 0, 1, 2);
        assertEquals2(2, list.removeLast());
        assertEquals2(1, list.removeLast());
        assertEquals2(0, list.removeLast());
        assertTrue(list.isEmpty());
    }

    /* */
    @Test
    public void testRemoveRange()
    {
        list.add(asArray(0, 1, 2, 3, 4));

        list.removeRange(0, 2);
        assertListEquals(list.toArray(), 2, 3, 4);

        list.removeRange(2, 3);
        assertListEquals(list.toArray(), 2, 3);
        
        list.removeRange(1, 1);
        assertListEquals(list.toArray(), 2, 3);

        list.removeRange(0, 1);
        assertListEquals(list.toArray(), 3);
    }

    /* */
    @Test
    public void testRemoveFirstLast()
    {
        list.add(asArray(0, 1, 2, 1, 0));

        assertEquals(-1, list.removeFirst(k5));
        assertEquals(-1, list.removeLast(k5));
        assertListEquals(list.toArray(), 0, 1, 2, 1, 0);

        assertEquals(1, list.removeFirst(k1));
        assertListEquals(list.toArray(), 0, 2, 1, 0);
        assertEquals(3, list.removeLast(k0));
        assertListEquals(list.toArray(), 0, 2, 1);
        assertEquals(0, list.removeLast(k0));
        assertListEquals(list.toArray(), 2, 1);
        assertEquals(-1, list.removeLast(k0));
        
        list.clear();
        list.add(newArray(k1, Intrinsics.empty(), k2, Intrinsics.empty(), k1));
        assertEquals(1, list.removeFirst(Intrinsics.empty()));
        assertEquals(2, list.removeLast(Intrinsics.empty()));
        assertListEquals(list.toArray(), 1, 2, 1);
    }

    /* */
    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAll()
    {
        list.add(asArray(0, 1, 0, 1, 0));

        assertEquals(0, list.removeAll(k2));
        assertEquals(3, list.removeAll(k0));
        assertListEquals(list.toArray(), 1, 1);

        assertEquals(2, list.removeAll(k1));
        assertTrue(list.isEmpty());

        list.clear();
        list.add(newArray(k1, Intrinsics.empty(), k2, Intrinsics.empty(), k1));
        assertEquals(2, list.removeAll((KType) Intrinsics.empty()));
        assertEquals(0, list.removeAll((KType) Intrinsics.empty()));
        assertListEquals(list.toArray(), 1, 2, 1);
    }

    /*! #if (not $TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE")) !*/
    @Test
    public void testRemoveAllFromLookupContainer()
    {
        list.add(asArray(0, 1, 2, 1, 0));

        KTypeHashSet<KType> set = new KTypeHashSet<>();
        set.addAll(asArray(0, 2));

        assertEquals(3, list.removeAll(set));
        assertEquals(0, list.removeAll(set));

        assertListEquals(list.toArray(), 1, 1);
    }
    /*! #end !*/

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        list.add(newArray(k0, k1, k2, k1, k4));

        assertEquals(3, list.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertListEquals(list.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRetainAllWithPredicate()
    {
        list.add(newArray(k0, k1, k2, k1, k0));

        assertEquals(2, list.retainAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertListEquals(list.toArray(), 1, 2, 1);
    }
    
    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        list.add(newArray(k0, k1, k2, k1, k4));

        final RuntimeException t = new RuntimeException(); 
        try
        {
            assertEquals(3, list.removeAll(new KTypePredicate<KType>()
            {
                public boolean apply(KType v)
                {
                    if (v == key2) throw t;
                    return v == key1;
                };
            }));
            fail();
        }
        catch (RuntimeException e)
        {
            // Make sure it's really our exception...
            if (e != t) throw e;
        }

        // And check if the list is in consistent state.
        assertListEquals(list.toArray(), 0, key2, key1, 4);
        assertEquals(4, list.size());
    }
    
    /* */
    @Test
    @SuppressWarnings("unchecked")
    public void testIndexOf()
    {
        list.add(asArray(3, 1, 2, 1, 3));

        list.add((KType) Intrinsics.empty());
        assertEquals(5, list.indexOf(Intrinsics.empty()));

        assertEquals(0, list.indexOf(k3));
        assertEquals(-1, list.indexOf(k4));
        assertEquals(2, list.indexOf(k2));
    }

    /* */
    @Test
    @SuppressWarnings("unchecked")
    public void testLastIndexOf()
    {
        list.add(asArray(3, 1, 2, 1, 3));

        list.add((KType) Intrinsics.empty());
        assertEquals(5, list.lastIndexOf(Intrinsics.empty()));

        assertEquals2(4, list.lastIndexOf(k3));
        assertEquals2(-1, list.lastIndexOf(k4));
        assertEquals2(2, list.lastIndexOf(k2));
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        TightRandomResizingStrategy resizer = new TightRandomResizingStrategy(0);
        KTypeArrayList<KType> list = new KTypeArrayList<>(0, resizer);
        assertEquals(list.size(), list.buffer.length);

        // Add some elements.
        final int max = rarely() ? 0 : randomIntBetween(0, 1000);
        for (int i = 0; i < max; i++) {
          list.add(cast(i));
        }

        final int additions = randomIntBetween(1, 5000);
        list.ensureCapacity(additions + list.size());
        final int before = resizer.growCalls;
        for (int i = 0; i < additions; i++) {
          list.add(cast(i));
        }
        assertEquals(before, resizer.growCalls);
    }

    @Test
    public void testResizeAndCleanBuffer()
    {
        list.ensureCapacity(20);
        Arrays.fill(list.buffer, k1);

        list.resize(10);
        assertEquals(10, list.size());
        for (int i = 0; i < list.size(); i++) { 
            assertEquals2(Intrinsics.<KType> empty(), list.get(i));
        }

        Arrays.fill(list.buffer, Intrinsics.<KType> empty());
        for (int i = 5; i < list.size(); i++) {
            list.set(i, k1);
        }
        list.resize(5);
        assertEquals(5, list.size());
        for (int i = list.size(); i < list.buffer.length; i++) {
            assertEquals2(Intrinsics.<KType> empty(), list.buffer[i]);
        }
    }

    /* */
    @Test
    public void testTrimToSize()
    {
        list.add(asArray(1, 2));
        list.trimToSize();
        assertEquals(2, list.buffer.length);
    }

    /* */
    @Test
    public void testRelease()
    {
        list.add(asArray(1, 2));
        list.release();
        assertEquals(0, list.size());
        list.add(asArray(1, 2));
        assertEquals(2, list.size());
    }

    /* */
    @Test
    public void testGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        list = new KTypeArrayList<KType>(0, 
            new BoundedProportionalArraySizingStrategy(5, maxGrowth, 2));
        assertEquals(list.size(), list.buffer.length);

        for (int i = 0; i < count; i++)
            list.add(cast(i));

        assertEquals(count, list.size());

        for (int i = 0; i < count; i++)
            assertEquals2(cast(i), list.get(i));

        assertTrue("Buffer size: 510 <= " + list.buffer.length,
            list.buffer.length <= count + maxGrowth);
    }

    /* */
    @Test
    public void testIterable()
    {
        list.add(asArray( 0, 1, 2, 3));
        int count = 0;
        for (KTypeCursor<KType> cursor : list)
        {
            count++;
            assertEquals2(list.get(cursor.index), cursor.value);
            assertEquals2(list.buffer[cursor.index], cursor.value);
        }
        assertEquals(count, list.size());

        count = 0;
        list.resize(0);
        for (@SuppressWarnings("unused") KTypeCursor<KType> cursor : list)
        {
            count++;
        }
        assertEquals(0, count);
    }
    
    /* */
    @Test
    public void testIterator()
    {
        list.add(asArray( 0, 1, 2, 3));
        Iterator<KTypeCursor<KType>> iterator = list.iterator();
        int count = 0;
        while (iterator.hasNext())
        {
            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        assertEquals(count, list.size());

        list.resize(0);
        assertFalse(list.iterator().hasNext());
    }

    /* */
    @Test
    public void testForEachWithProcedure()
    {
        list.add(asArray( 1, 2, 3));
        final AtomicInteger holder = new AtomicInteger();
        list.forEach(new KTypeProcedure<KType>() {
            int index = 0;
            public void apply(KType v)
            {
                assertEquals2(v, list.get(index++));
                holder.set(index);
            }
        });
        assertEquals(holder.get(), list.size());
    }

    /* */
    @Test
    public void testForEachReturnValueFromAnonymousClass()
    {
        list.add(asArray( 1, 2, 3));
        int result = list.forEach(new KTypeProcedure<KType>() {
            int index = 0;
            public void apply(KType v)
            {
                assertEquals2(v, list.get(index++));
            }
        }).index;
        assertEquals(result, list.size());
    }

    /* */
    @Test
    public void testClear()
    {
        list.add(asArray( 1, 2, 3));
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(-1, list.indexOf(cast(1)));
    }

    /* */
    @Test
    public void testFrom()
    {
        list = KTypeArrayList.from(k1, k2, k3);
        assertEquals(3, list.size());
        assertListEquals(list.toArray(), 1, 2, 3);
        assertEquals(list.size(), list.buffer.length);
    }

    /* */
    @Test
    public void testCopyContainer()
    {
        list.add(asArray( 1, 2, 3));
        KTypeArrayList<KType> copy = new KTypeArrayList<KType>(list);
        assertEquals(3, copy.size());
        assertListEquals(copy.toArray(), 1, 2, 3);
        assertEquals(copy.size(), copy.buffer.length);
    }

    /* */
    @Test
    public void testHashCodeEquals()
    {
        KTypeArrayList<KType> l0 = KTypeArrayList.from();
        assertEquals(1, l0.hashCode());
        assertEquals(l0, KTypeArrayList.from());

        KTypeArrayList<KType> l1 = KTypeArrayList.from(k1, k2, k3);
        KTypeArrayList<KType> l2 = KTypeArrayList.from(k1, k2);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }

    /* */
    @Test
    public void testEqualElements()
    {
        KTypeStack<KType> l1 = KTypeStack.from(k1, k2, k3);
        KTypeArrayList<KType> l2 = KTypeArrayList.from(k1, k2);
        l2.add(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertTrue(l2.equalElements(l1));
    }

    @Test
    public void testHashCodeWithEmptyKeys()
    {
        KTypeArrayList<KType> l1 = KTypeArrayList.from(k1, Intrinsics.empty(), k3);
        KTypeArrayList<KType> l2 = KTypeArrayList.from(k1, Intrinsics.empty(), k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAddAll_subclass()
    {
        class A {
        }

        class B extends A {
        }

        KTypeArrayList<B> list2 = new KTypeArrayList<>();
        list2.add(new B());

        KTypeArrayList<A> list3 = new KTypeArrayList<>();
        list3.add(new B());
        list3.add(new A());
        list3.addAll(list2);
        assertEquals(3, list3.size());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        KTypeArrayList<Integer> l1 = KTypeArrayList.from(1, 2, 3);
        Integer[] result = l1.toArray(Integer.class);
        assertArrayEquals(new Integer [] {1, 2, 3}, result); // dummy
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArray()
    {
        list = KTypeArrayList.from(k1, k2, k3);
        Object[] result = list.toArray();
        assertArrayEquals(new Object [] {k1, k2, k3}, result);
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.isKTypeAnyOf("GENERIC", "INT", "LONG", "DOUBLE")) !*/
    @Test
    public void testStream() {
        list.add(k1, k2, k3);
        assertEquals2(k1, list.stream().findFirst().orElseThrow());
        assertEquals2(k2, list.stream().toArray()[1]);
    }
    /*! #end !*/

    @Test
    public void testSort() {
        list = KTypeArrayList.from(key3, key1, key3, key2);
        KTypeArrayList<KType> list2 = new KTypeArrayList<KType>();
        list2.ensureCapacity(30);
        list2.addAll(list);
        assertSame(list2, list2.sort());
        assertEquals2(KTypeArrayList.from(key1, key2, key3, key3), list2);
    }

    @Test
    public void testReverse() {
        for (int size = 0; size < 10; size++) {
            KTypeArrayList<KType> list = new KTypeArrayList<KType>();
            list.ensureCapacity(30);
            for (int j = 0; j < size; j++) {
                list.add(cast(j));
            }
            assertSame(list, list.reverse());
            assertEquals(size, list.size());
            int reverseIndex = size - 1;
            for (KTypeCursor<KType> cursor : list) {
                assertEquals2(cast(reverseIndex--), cursor.value);
            }
        }
    }

    /* */
    @Test
    public void testClone()
    {
        list.add(k1, k2, k3);

        KTypeArrayList<KType> cloned = list.clone();
        cloned.removeAll(key1);

        assertSortedListEquals(list.toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.toArray(), key2, key3);
    }

    /* */
    @Test
    public void testToString()
    {
        assertEquals("[" 
            + key1 + ", "
            + key2 + ", "
            + key3 + "]", KTypeArrayList.from(k1, k2, k3).toString());
    }

    /* */
    @Test
    public void testEqualsSameClass()
    {
        KTypeArrayList<KType> l1 = KTypeArrayList.from(k1, k2, k3);
        KTypeArrayList<KType> l2 = KTypeArrayList.from(k1, k2, k3);
        KTypeArrayList<KType> l3 = KTypeArrayList.from(k1, k3, k2);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeArrayList<KType> {
        };

        KTypeArrayList<KType> l1 = KTypeArrayList.from(k1, k2, k3);
        KTypeArrayList<KType> l2 = new Sub();
        KTypeArrayList<KType> l3 = new Sub();
        l2.addAll(l1);
        l3.addAll(l1);

        Assertions.assertThat(l2).isEqualTo(l3);
        Assertions.assertThat(l1).isNotEqualTo(l2);
    }    
}
