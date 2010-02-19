package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.rules.MethodRule;

/**
 * Unit tests for {@link ObjectArrayList}.
 */
public class ObjectArrayListTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectArrayList<Object> list;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* */
    @Before
    public void initialize()
    {
        list = new ObjectArrayList<Object>();
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (list != null)
            for (int i = list.elementsCount; i < list.buffer.length; i++)
                assertTrue(Intrinsics.<KType>defaultKTypeValue() == list.buffer[i]);
    }

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
        list.add(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddTwoArgs()
    {
        list.add(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2);
        list.add(
            /* intrinsic:ktypecast */ 3, 
            /* intrinsic:ktypecast */ 4);
        assertListEquals(list.toArray(), 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddArray()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3), 1, 2);
        assertListEquals(list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddVarArg()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3));
        list.add(
            /* intrinsic:ktypecast */ 4, 
            /* intrinsic:ktypecast */ 5, 
            /* intrinsic:ktypecast */ 6,
            /* intrinsic:ktypecast */ 7);
        assertListEquals(list.toArray(), 0, 1, 2, 3, 4, 5, 6, 7);
    }

    /* */
    @Test
    public void testAddAll()
    {
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 1, 2));

        list.addAll(list2.iterator());
        list.addAll(list2);

        assertListEquals(list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /* removeIf:primitive */
    @Test
    public void testAddAll_subclass()
    {
        class A {
        }

        class B extends A {
        }

        ObjectArrayList<B> list2 = new ObjectArrayList<B>();
        list2.add(new B());

        ObjectArrayList<A> list3 = new ObjectArrayList<A>();
        list3.add(new B());
        list3.add(new A());

        list3.addAll(list2.iterator());

        assertEquals(3, list3.size());
    }
    /* end:removeIf */

    /* */
    @Test
    public void testInsert()
    {
        list.insert(0, /* intrinsic:ktypecast */ 1);
        list.insert(0, /* intrinsic:ktypecast */ 2);
        list.insert(2, /* intrinsic:ktypecast */ 3);
        list.insert(1, /* intrinsic:ktypecast */ 4);

        assertListEquals(list.toArray(), 2, 4, 1, 3);
    }

    /* */
    @Test
    public void testSet()
    {
        list.add(newArray(list.buffer, 0, 1, 2));

        assertEquals2(0, list.set(0, /* intrinsic:ktypecast */ 3));
        assertEquals2(1, list.set(1, /* intrinsic:ktypecast */ 4));
        assertEquals2(2, list.set(2, /* intrinsic:ktypecast */ 5));

        assertListEquals(list.toArray(), 3, 4, 5);
    }
    
    /* */
    @Test
    public void testRemove()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3, 4));

        list.remove(0);
        list.remove(2);
        list.remove(1);

        assertListEquals(list.toArray(), 1, 4);
    }

    /* */
    @Test
    public void testRemoveRange()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3, 4));

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
        list.add(newArray(list.buffer, 0, 1, 2, 1, 0));

        assertEquals(-1, list.removeFirst(/* intrinsic:ktypecast */ 5));
        assertEquals(-1, list.removeLast(/* intrinsic:ktypecast */ 5));
        assertListEquals(list.toArray(), 0, 1, 2, 1, 0);

        assertEquals(1, list.removeFirst(/* intrinsic:ktypecast */ 1));
        assertListEquals(list.toArray(), 0, 2, 1, 0);
        assertEquals(3, list.removeLast(/* intrinsic:ktypecast */ 0));
        assertListEquals(list.toArray(), 0, 2, 1);
        assertEquals(0, list.removeLast(/* intrinsic:ktypecast */ 0));
        assertListEquals(list.toArray(), 2, 1);
        assertEquals(-1, list.removeLast(/* intrinsic:ktypecast */ 0));
        
        /* removeIf:primitive */
        list.clear();
        list.add(newArray(list.buffer, 0, null, 2, null, 0));
        assertEquals(1, list.removeFirst(null));
        assertEquals(2, list.removeLast(null));
        assertListEquals(list.toArray(), 0, 2, 0);
        /* end:removeIf */
    }

    /* */
    @Test
    public void testRemoveAll()
    {
        list.add(newArray(list.buffer, 0, 1, 0, 1, 0));

        assertEquals(0, list.removeAll(/* intrinsic:ktypecast */ 2));
        assertEquals(3, list.removeAll(/* intrinsic:ktypecast */ 0));
        assertListEquals(list.toArray(), 1, 1);

        assertEquals(2, list.removeAll(/* intrinsic:ktypecast */ 1));
        assertTrue(list.isEmpty());

        /* removeIf:primitive */
        list.clear();
        list.add(newArray(list.buffer, 0, null, 2, null, 0));
        assertEquals(2, list.removeAll((Object) null));
        assertEquals(0, list.removeAll((Object) null));
        assertListEquals(list.toArray(), 0, 2, 0);
        /* end:removeIf */
    }

    /* */
    @Test
    public void testRemoveAllIn()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 1, 0));
        
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 2));

        assertEquals(3, list.removeAllIn(list2));
        assertEquals(0, list.removeAllIn(list2.iterator()));

        assertListEquals(list.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testIndexOf()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 1, 0));
        /* removeIf:primitive */ 
        list.add((Object) null);
        /* end:removeIf */        

        /* removeIf:primitive */
        assertEquals(5, list.indexOf(null));
        /* end:removeIf */
        assertEquals(0, list.indexOf(/* intrinsic:ktypecast */ 0));
        assertEquals(-1, list.indexOf(/* intrinsic:ktypecast */ 3));
        assertEquals(2, list.indexOf(/* intrinsic:ktypecast */ 2));
    }
    
    /* */
    @Test
    public void testLastIndexOf()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 1, 0));
        /* removeIf:primitive */ 
        list.add((Object) null);
        /* end:removeIf */        

        /* removeIf:primitive */
        assertEquals(5, list.lastIndexOf(null));
        /* end:removeIf */
        assertEquals2(4, list.lastIndexOf(/* intrinsic:ktypecast */ 0));
        assertEquals2(-1, list.lastIndexOf(/* intrinsic:ktypecast */ 3));
        assertEquals2(2, list.lastIndexOf(/* intrinsic:ktypecast */ 2));
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        list.ensureCapacity(100);
        assertTrue(list.buffer.length >= 100);

        list.ensureCapacity(1000);
        list.ensureCapacity(1000);
        assertTrue(list.buffer.length >= 1000);
    }

    /* Test resizing and cleaning of buffer content. */
    @Test
    public void testResize()
    {
        list.ensureCapacity(20);
        Arrays.fill(list.buffer, /* intrinsic:ktypecast */ 1);

        list.resize(10);
        assertEquals(10, list.size());
        for (int i = 0; i < list.size(); i++) 
            assertEquals2(Intrinsics.<KType>defaultKTypeValue(), list.get(i));

        Arrays.fill(list.buffer, Intrinsics.<KType>defaultKTypeValue());
        for (int i = 5; i < list.size(); i++)
            list.set(i, /* intrinsic:ktypecast */ 1);
        list.resize(5);
        assertEquals(5, list.size());
        for (int i = list.size(); i < list.buffer.length; i++) 
            assertEquals2(Intrinsics.<KType>defaultKTypeValue(), list.buffer[i]);
    }

    /* */
    @Test
    public void testTrimToSize()
    {
        list.add(newArray(list.buffer, 1, 2));
        list.trimToSize();
        assertEquals(2, list.buffer.length);
    }

    /* */
    @Test
    public void testRelease()
    {
        list.add(newArray(list.buffer, 1, 2));
        list.release();
        assertEquals(0, list.size());
        list.add(newArray(list.buffer, 1, 2));
        assertEquals(2, list.size());
    }

    /* */
    @Test
    public void testGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        list = new ObjectArrayList<Object>(0, new BoundedProportionalArraySizingStrategy(
            5, maxGrowth, 2));

        for (int i = 0; i < count; i++)
            list.add(/* intrinsic:ktypecast */ i);

        assertEquals(count, list.size());

        for (int i = 0; i < count; i++)
            assertEquals2(/* intrinsic:ktypecast */ i, list.get(i));

        assertTrue("Buffer size: 510 <= " + list.buffer.length,
            list.buffer.length <= count + maxGrowth);
    }

    /* */
    @Test
    public void testIterable()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3));
        int count = 0;
        for (ObjectCursor<Object> cursor : list)
        {
            count++;
            assertEquals2(cursor.value, list.get(cursor.index));
        }
        assertEquals(count, list.size());

        count = 0;
        list.resize(0);
        for (@SuppressWarnings("unused") ObjectCursor<Object> cursor : list)
        {
            count++;
        }
        assertEquals(0, count);
    }
    
    /* */
    @Test
    public void testIterator()
    {
        list.add(newArray(list.buffer, 0, 1, 2, 3));
        Iterator<ObjectCursor<Object>> iterator = list.iterator();
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
    /* removeIf:primitive */
    @SuppressWarnings({"unchecked"})
    /* end:removeIf */
    public void testForEachWithProcedure()
    {
        list.add(newArray(list.buffer, 1, 2, 3));
        final AtomicInteger holder = new AtomicInteger();
        ((ObjectArrayList<KType>) list).forEach(new ObjectProcedure<KType>() {
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
    public void testClear()
    {
        list.add(newArray(list.buffer, 1, 2, 3));
        list.clear();

        /* removeIf:primitive */
        checkTrailingSpaceUninitialized();
        /* end:removeIf */

        // We don't care about the initialization for primitive types.
        // Clean anyway for @After assertions.
        Arrays.fill(list.buffer, Intrinsics.<KType>defaultKTypeValue());
    }
    
    /* */
    @Test
    public void testFrom()
    {
        ObjectArrayList<Integer> variable = ObjectArrayList.from(
            /* intrinsic:ktypecast */ 1, 
            /* intrinsic:ktypecast */ 2, 
            /* intrinsic:ktypecast */ 3);

        assertEquals(3, variable.size());
        assertListEquals(variable.toArray(), 1, 2, 3);
    }
}
