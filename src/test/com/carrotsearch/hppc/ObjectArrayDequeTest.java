package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.rules.MethodRule;

/**
 * Unit tests for {@link ObjectArrayDeque}.
 */
public class ObjectArrayDequeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public ObjectArrayDeque<Object> deque;

    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /**
     * Some sequence values for tests.
     */
    private ObjectArrayList<Object> sequence = new ObjectArrayList<Object>();

    /* */
    @Before
    public void initialize()
    {
        deque = new ObjectArrayDeque<Object>();

        sequence.clear();
        for (int i = 0; i < 10000; i++)
            sequence.add(/* intrinsic:ktypecast */ i);
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (deque != null)
        {
            for (int i = deque.tail; i < deque.head; i = ObjectArrayDeque.oneRight(i, deque.buffer.length))
                assertTrue(Intrinsics.<KType>defaultKTypeValue() == deque.buffer[i]);
        }
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        assertEquals(0, deque.size());
    }

    /* */
    @Test
    public void testAddFirst()
    {
        deque.addFirst(/* intrinsic:ktypecast */ 1);
        deque.addFirst(/* intrinsic:ktypecast */ 2);
        deque.addFirst(/* intrinsic:ktypecast */ 3);
        assertListEquals(deque.toArray(), 3, 2, 1);
        assertEquals(3, deque.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        deque.addLast(/* intrinsic:ktypecast */ 3);
        assertListEquals(deque.toArray(), 1, 2, 3);
        assertEquals(3, deque.size());
    }
    
    /* */
    @Test
    public void testAddWithGrowth()
    {
        for (int i = 0; i < sequence.size(); i++)
        {
            deque.addFirst(sequence.buffer[i]);
        }

        assertListEquals(reverse(sequence.toArray()), deque.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < sequence.size(); i++)
            deque.addLast(sequence.buffer[i]);

        assertListEquals(sequence.toArray(), deque.toArray());
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 1, 2));

        deque.addAllFirst(list2.iterator());
        assertListEquals(deque.toArray(), 2, 1, 0);
        deque.addAllFirst(list2);
        assertListEquals(deque.toArray(), 2, 1, 0, 2, 1, 0);

        deque.clear();
        ObjectArrayDeque<Object> deque2 = new ObjectArrayDeque<Object>();
        deque2.addLast(newArray(deque2.buffer, 0, 1, 2));
        deque.addAllFirst(deque2);
        assertListEquals(deque.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 1, 2));

        deque.addAllLast(list2.iterator());
        assertListEquals(deque.toArray(), 0, 1, 2);
        deque.addAllLast(list2);
        assertListEquals(deque.toArray(), 0, 1, 2, 0, 1, 2);

        deque.clear();
        ObjectArrayDeque<Object> deque2 = new ObjectArrayDeque<Object>();
        deque2.addLast(newArray(deque2.buffer, 0, 1, 2));
        deque.addAllLast(deque2);
        assertListEquals(deque.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemoveFirst()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        deque.addLast(/* intrinsic:ktypecast */ 3);

        deque.removeFirst();
        assertListEquals(deque.toArray(), 2, 3);
        assertEquals(2, deque.size());

        deque.addFirst(/* intrinsic:ktypecast */ 4);
        assertListEquals(deque.toArray(), 4, 2, 3);
        assertEquals(3, deque.size());

        deque.removeFirst();
        deque.removeFirst();
        deque.removeFirst();
        assertEquals(0, deque.toArray().length);
        assertEquals(0, deque.size());    
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveFirstEmpty()
    {
        deque.removeFirst();
    }

    /* */
    @Test
    public void testRemoveLast()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        deque.addLast(/* intrinsic:ktypecast */ 3);

        deque.removeLast();
        assertListEquals(deque.toArray(), 1, 2);
        assertEquals(2, deque.size());

        deque.addLast(/* intrinsic:ktypecast */ 4);
        assertListEquals(deque.toArray(), 1, 2, 4);
        assertEquals(3, deque.size());
        
        deque.removeLast();
        deque.removeLast();
        deque.removeLast();
        assertEquals(0, deque.toArray().length);
        assertEquals(0, deque.size());    
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveLastEmpty()
    {
        deque.removeLast();
    }

    /* */
    @Test
    public void testGetFirst()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        assertEquals2(/* intrinsic:ktypecast */ 1, deque.getFirst());
        deque.addFirst(/* intrinsic:ktypecast */ 3);
        assertEquals2(/* intrinsic:ktypecast */ 3, deque.getFirst());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetFirstEmpty()
    {
        deque.getFirst();
    }
    
    /* */
    @Test
    public void testGetLast()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        assertEquals2(/* intrinsic:ktypecast */ 2, deque.getLast());
        deque.addLast(/* intrinsic:ktypecast */ 3);
        assertEquals2(/* intrinsic:ktypecast */ 3, deque.getLast());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetLastEmpty()
    {
        deque.getLast();
    }

    /* */
    @Test
    public void testRemoveFirstOccurrence()
    {
        int modulo = 10;
        int count = 10000;
        sequence.clear();
        for (int i = 0; i < count; i++)
        {
            deque.addLast(/* intrinsic:ktypecast */ (i % modulo));
            sequence.add( /* intrinsic:ktypecast */ (i % modulo));
        }

        Random rnd = new Random(0x11223344);
        for (int i = 0; i < 500; i++)
        {
            /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ k = 
                /* intrinsic:ktypecast */ rnd.nextInt(modulo);
            assertEquals(deque.removeFirstOccurrence(k), sequence.removeFirst(k) >= 0);
        }

        assertListEquals(deque.toArray(), sequence.toArray());

        assertFalse(deque.removeFirstOccurrence(/* intrinsic:ktypecast */ (modulo + 1)));
        deque.addLast(/* intrinsic:ktypecast */ (modulo + 1));
        assertTrue(deque.removeFirstOccurrence(/* intrinsic:ktypecast */ (modulo + 1)));
    }

    /* */
    @Test
    public void testRemoveLastOccurrence()
    {
        int modulo = 10;
        int count = 10000;
        sequence.clear();
        for (int i = 0; i < count; i++)
        {
            deque.addLast(/* intrinsic:ktypecast */ (i % modulo));
            sequence.add( /* intrinsic:ktypecast */ (i % modulo));
        }

        Random rnd = new Random(0x11223344);
        for (int i = 0; i < 500; i++)
        {
            /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ k = 
                /* intrinsic:ktypecast */ rnd.nextInt(modulo);
            assertEquals(deque.removeLastOccurrence(k), sequence.removeLast(k) >= 0);
        }

        assertListEquals(deque.toArray(), sequence.toArray());

        assertFalse(deque.removeLastOccurrence(/* intrinsic:ktypecast */ (modulo + 1)));
        deque.addFirst(/* intrinsic:ktypecast */ (modulo + 1));
        assertTrue(deque.removeLastOccurrence(/* intrinsic:ktypecast */ (modulo + 1)));
    }
    
    /* */
    @Test
    public void testRemoveAllOccurrences()
    {
        deque.addLast(newArray(deque.buffer, 0, 1, 2, 1, 0, 3, 0));
        
        assertEquals(0, deque.removeAllOccurrences(/* intrinsic:ktypecast */ 4));
        assertEquals(3, deque.removeAllOccurrences(/* intrinsic:ktypecast */ 0));
        assertListEquals(deque.toArray(), 1, 2, 1, 3);
        assertEquals(1, deque.removeAllOccurrences(/* intrinsic:ktypecast */ 3));
        assertListEquals(deque.toArray(), 1, 2, 1);
        assertEquals(2, deque.removeAllOccurrences(/* intrinsic:ktypecast */ 1));
        assertListEquals(deque.toArray(), 2);
        assertEquals(1, deque.removeAllOccurrences(/* intrinsic:ktypecast */ 2));
        assertEquals(0, deque.size());
    }

    /* */
    @Test
    public void testRemoveAllIn()
    {
        deque.addLast(newArray(deque.buffer, 0, 1, 2, 1, 0));
        
        ObjectArrayList<Object> list2 = new ObjectArrayList<Object>();
        list2.add(newArray(list2.buffer, 0, 2));

        assertEquals(3, deque.removeAllIn(list2));
        assertEquals(0, deque.removeAllIn(list2.iterator()));

        assertListEquals(deque.toArray(), 1, 1);
    }
    
    /* */
    @Test
    public void testClear()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        deque.addFirst(/* intrinsic:ktypecast */ 3);
        deque.clear();
        assertEquals(0, deque.size());
        assertEquals(0, deque.head);
        assertEquals(0, deque.tail);

        deque.addLast(/* intrinsic:ktypecast */ 1);
        assertListEquals(deque.toArray(), 1);
    }
    
    /* */
    @Test
    public void testRelease()
    {
        deque.addLast(/* intrinsic:ktypecast */ 1);
        deque.addLast(/* intrinsic:ktypecast */ 2);
        deque.addFirst(/* intrinsic:ktypecast */ 3);
        deque.release();
        assertEquals(0, deque.size());
        assertEquals(0, deque.head);
        assertEquals(0, deque.tail);

        deque.addLast(/* intrinsic:ktypecast */ 1);
        assertListEquals(deque.toArray(), 1);
    }

    /* */
    @Test
    public void testIterable()
    {
        deque.addLast(sequence.buffer);

        int count = 0;
        for (ObjectCursor<Object> cursor : deque)
        {
            assertEquals2(sequence.buffer[count], cursor.value);
            count++;
        }
        assertEquals(count, deque.size());

        count = 0;
        deque.clear();
        for (@SuppressWarnings("unused") ObjectCursor<Object> cursor : deque)
        {
            count++;
        }
        assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterator()
    {
        deque.addLast(newArray(deque.buffer, 0, 1, 2, 3));
        
        Iterator<ObjectCursor<Object>> iterator = deque.iterator();
        int count = 0;
        while (iterator.hasNext())
        {
            iterator.hasNext();
            iterator.hasNext();
            iterator.hasNext();
            iterator.next();
            count++;
        }
        assertEquals(count, deque.size());

        deque.clear();
        assertFalse(deque.iterator().hasNext());
    }

    /* */
    @Test
    /* removeIf:primitive */
    @SuppressWarnings({"unchecked"})
    /* end:removeIf */
    public void testForEachWithProcedure()
    {
        deque.addLast(sequence.buffer);

        final AtomicInteger holder = new AtomicInteger();
        ((ObjectArrayDeque<KType>) deque).forEach(new ObjectProcedure<KType>() {
            int index = 0;
            public void apply(KType v)
            {
                assertEquals2(sequence.buffer[index], v);
                holder.set(++index);
            }
        });
        assertEquals(holder.get(), deque.size());
    }

    /* removeIf:primitive */
    /* */
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random(0x11223344);
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<Object> ad = new ArrayDeque<Object>();
        for (int i = 0; i < rounds; i++)
        {
            /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ k = 
                /* intrinsic:ktypecast */ rnd.nextInt(modulo);

            final int op = rnd.nextInt(8); 
            if (op < 2)
            {
                deque.addFirst(k);
                ad.addFirst(k);
            }
            else if (op < 4)
            {
                deque.addLast(k);
                ad.addLast(k);
            }
            else if (op < 5 && ad.size() > 0)
            {
                deque.removeLast();
                ad.removeLast();
            }
            else if (op < 6 && ad.size() > 0)
            {
                deque.removeLast();
                ad.removeLast();
            }
            else if (op < 7)
            {
                assertEquals(
                    ad.removeFirstOccurrence(k), deque.removeFirstOccurrence(k));
            }
            else if (op < 8)
            {
                assertEquals(
                    ad.removeLastOccurrence(k), deque.removeLastOccurrence(k));
            }
            assertEquals(ad.size(), deque.size());
        }

        assertArrayEquals(ad.toArray(), deque.toArray());
    }

    @Test
    public void testAgainstArrayDequeVariousTailHeadPositions()
    {
        this.deque.clear();
        this.deque.head = this.deque.tail = 2;
        testAgainstArrayDeque();
        
        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length - 2;
        testAgainstArrayDeque();
        
        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length / 2;
        testAgainstArrayDeque();
    }
    /* end:removeIf */
}
