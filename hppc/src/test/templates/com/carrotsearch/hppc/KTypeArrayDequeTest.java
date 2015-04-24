package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

/*! #if ($TemplateOptions.KTypeGeneric) !*/
import java.util.ArrayDeque;
/*! #end !*/
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;


/**
 * Unit tests for {@link KTypeArrayDeque}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayDequeTest<KType> extends AbstractKTypeTest<KType> 
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayDeque<KType> deque;

    /**
     * Some sequence values for tests.
     */
    private KTypeArrayList<KType> sequence;

    /* */
    @Before
    public void initialize()
    {
        deque = new KTypeArrayDeque<>();
        sequence = new KTypeArrayList<>();

        for (int i = 0; i < 10000; i++)
            sequence.add(cast(i));
    }

    @After
    public void checkTrailingSpaceUninitialized()
    {
        if (deque != null)
        {
            for (int i = deque.tail; i < deque.head; i = KTypeArrayDeque.oneRight(i, deque.buffer.length))
                assertTrue(Intrinsics.<KType> empty() == deque.buffer[i]);
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
        deque.addFirst(k1);
        deque.addFirst(k2);
        deque.addFirst(k3);
        assertListEquals(deque.toArray(), 3, 2, 1);
        assertEquals(3, deque.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        deque.addLast(k1);
        deque.addLast(k2);
        deque.addLast(k3);
        assertListEquals(deque.toArray(), 1, 2, 3);
        assertEquals(3, deque.size());
    }
    
    /* */
    @Test
    public void testAddWithGrowth()
    {
        for (int i = 0; i < sequence.size(); i++)
        {
            deque.addFirst(sequence.get(i));
        }

        assertListEquals(reverse(sequence.toArray()), deque.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < sequence.size(); i++)
        {
            deque.addLast(sequence.get(i));
        }

        assertListEquals(sequence.toArray(), deque.toArray());
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        KTypeArrayList<KType> list2 = new KTypeArrayList<>();
        list2.add(asArray(0, 1, 2));

        deque.addFirst(list2);
        assertListEquals(deque.toArray(), 2, 1, 0);
        deque.addFirst(list2);
        assertListEquals(deque.toArray(), 2, 1, 0, 2, 1, 0);

        deque.clear();
        KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        deque.addFirst(deque2);
        assertListEquals(deque.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        KTypeArrayList<KType> list2 = new KTypeArrayList<>();
        list2.add(asArray(0, 1, 2));

        deque.addLast(list2);
        assertListEquals(deque.toArray(), 0, 1, 2);
        deque.addLast(list2);
        assertListEquals(deque.toArray(), 0, 1, 2, 0, 1, 2);

        deque.clear();
        KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        deque.addLast(deque2);
        assertListEquals(deque.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemoveFirst()
    {
        deque.addLast(k1);
        deque.addLast(k2);
        deque.addLast(k3);

        deque.removeFirst();
        assertListEquals(deque.toArray(), 2, 3);
        assertEquals(2, deque.size());

        deque.addFirst(k4);
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
        deque.addLast(k1);
        deque.addLast(k2);
        deque.addLast(k3);

        deque.removeLast();
        assertListEquals(deque.toArray(), 1, 2);
        assertEquals(2, deque.size());

        deque.addLast(k4);
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
        deque.addLast(k1);
        deque.addLast(k2);
        assertEquals2(k1, deque.getFirst());
        deque.addFirst(k3);
        assertEquals2(k3, deque.getFirst());
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
        deque.addLast(k1);
        deque.addLast(k2);
        assertEquals2(k2, deque.getLast());
        deque.addLast(k3);
        assertEquals2(k3, deque.getLast());
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
            deque.addLast(cast(i % modulo));
            sequence.add(cast(i % modulo));
        }

        Random rnd = getRandom();
        for (int i = 0; i < 500; i++)
        {
            KType k = cast(rnd.nextInt(modulo));
            assertEquals(
                deque.removeFirst(k) >= 0, 
                sequence.removeFirst(k) >= 0);
        }

        assertListEquals(deque.toArray(), sequence.toArray());

        assertTrue(0 > deque.removeFirst(cast(modulo + 1)));
        deque.addLast(cast(modulo + 1));
        assertTrue(0 <= deque.removeFirst(cast(modulo + 1)));
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
            deque.addLast(cast(i % modulo));
            sequence.add(cast(i % modulo));
        }

        Random rnd = getRandom();
        for (int i = 0; i < 500; i++)
        {
            KType k = cast(rnd.nextInt(modulo));
            assertEquals(
                deque.removeLast(k) >= 0, 
                sequence.removeLast(k) >= 0);
        }

        assertListEquals(deque.toArray(), sequence.toArray());

        assertTrue(0 > deque.removeLast(cast(modulo + 1)));
        deque.addFirst(cast(modulo + 1));
        assertTrue(0 <= deque.removeLast(cast(modulo + 1)));
    }
    
    /* */
    @Test
    public void testRemoveAll()
    {
        deque.addLast(asArray(0, 1, 2, 1, 0, 3, 0));
        
        assertEquals(0, deque.removeAll(k4));
        assertEquals(3, deque.removeAll(k0));
        assertListEquals(deque.toArray(), 1, 2, 1, 3);
        assertEquals(1, deque.removeAll(k3));
        assertListEquals(deque.toArray(), 1, 2, 1);
        assertEquals(2, deque.removeAll(k1));
        assertListEquals(deque.toArray(), 2);
        assertEquals(1, deque.removeAll(k2));
        assertEquals(0, deque.size());
    }

    /* */
    @Test
    public void testRemoveAllInLookupContainer()
    {
        deque.addLast(asArray(0, 1, 2, 1, 0));

        KTypeHashSet<KType> set = new KTypeHashSet<>();
        set.addAll(asArray(0, 2));

        assertEquals(3, deque.removeAll(set));
        assertEquals(0, deque.removeAll(set));

        assertListEquals(deque.toArray(), 1, 1);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        deque.addLast(newArray(k0, k1, k2, k1, k4));

        assertEquals(3, deque.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType v)
            {
                return v == key1 || v == key2;
            };
        }));

        assertListEquals(deque.toArray(), 0, 4);
    }

    /* */
    @Test
    public void testRemoveAllWithPredicateInterrupted()
    {
        deque.addLast(newArray(k0, k1, k2, k1, k4));

        final RuntimeException t = new RuntimeException(); 

        try
        {
            assertEquals(3, deque.removeAll(new KTypePredicate<KType>()
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

        // And check if the deque is in consistent state.
        assertListEquals(deque.toArray(), 0, key2, key1, 4);
        assertEquals(4, deque.size());
    }

    /* */
    @Test
    public void testClear()
    {
        deque.addLast(k1);
        deque.addLast(k2);
        deque.addFirst(k3);
        deque.clear();
        assertEquals(0, deque.size());
        assertEquals(0, deque.head);
        assertEquals(0, deque.tail);

        deque.addLast(k1);
        assertListEquals(deque.toArray(), 1);
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        TightRandomResizingStrategy resizer = new TightRandomResizingStrategy();
        KTypeArrayDeque<KType> deque = new KTypeArrayDeque<>(0, resizer);

        // Add some elements.
        final int max = rarely() ? 0 : randomIntBetween(0, 1000);
        for (int i = 0; i < max; i++) {
          deque.addLast(cast(i));
        }

        final int additions = randomIntBetween(1, 5000);
        deque.ensureCapacity(additions + deque.size());
        final int before = resizer.growCalls;
        for (int i = 0; i < additions; i++) {
          if (randomBoolean()) { 
            deque.addLast(cast(i));
          } else {
            deque.addFirst(cast(i));
          }
        }
        assertEquals(before, resizer.growCalls);
    }

    /* */
    @Test
    public void testRelease()
    {
        deque.addLast(k1);
        deque.addLast(k2);
        deque.addFirst(k3);
        deque.release();
        assertEquals(0, deque.size());
        assertEquals(0, deque.head);
        assertEquals(0, deque.tail);

        deque.addLast(k1);
        assertListEquals(deque.toArray(), 1);
    }

    /* */
    @Test
    public void testIterable()
    {
        deque.addLast(sequence);

        int count = 0;
        for (KTypeCursor<KType> cursor : deque)
        {
            assertEquals2(sequence.buffer[count], cursor.value);
            assertEquals2(deque.buffer[cursor.index], cursor.value);
            count++;
        }
        assertEquals(count, deque.size());
        assertEquals(count, sequence.size());

        count = 0;
        deque.clear();
        for (@SuppressWarnings("unused") KTypeCursor<KType> cursor : deque)
        {
            count++;
        }
        assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterator()
    {
        deque.addLast(asArray(0, 1, 2, 3));
        
        Iterator<KTypeCursor<KType>> iterator = deque.iterator();
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
    public void testDescendingIterator()
    {
        deque.addLast(sequence);

        int index = sequence.size() - 1;
        for (Iterator<KTypeCursor<KType>> i = deque.descendingIterator(); i.hasNext(); )
        {
            KTypeCursor<KType> cursor = i.next();
            assertEquals2(sequence.buffer[index], cursor.value);
            assertEquals2(deque.buffer[cursor.index], cursor.value);
            index--;
        }
        assertEquals(-1, index);

        deque.clear();
        assertFalse(deque.descendingIterator().hasNext());
    }

    /* */
    @Test
    public void testForEachWithProcedure()
    {
        deque.addLast(sequence);

        final AtomicInteger count = new AtomicInteger();
        deque.forEach(new KTypeProcedure<KType>() {
            int index = 0;
            public void apply(KType v)
            {
                assertEquals2(sequence.buffer[index++], v);
                count.incrementAndGet();
            }
        });
        assertEquals(count.get(), deque.size());
    }

    /* */
    @Test
    public void testDescendingForEachWithProcedure()
    {
        deque.addLast(sequence);

        final AtomicInteger count = new AtomicInteger();
        deque.descendingForEach(new KTypeProcedure<KType>() {
            int index = sequence.size();
            public void apply(KType v)
            {
                assertEquals2(sequence.buffer[--index], v);
                count.incrementAndGet();
            }
        });
        assertEquals(count.get(), deque.size());
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random(randomLong());
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<KType> ad = new ArrayDeque<KType>();
        for (int i = 0; i < rounds; i++)
        {
            KType k = cast(rnd.nextInt(modulo));

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
                    ad.removeFirstOccurrence(k), 
                    deque.removeFirst(k) >= 0);
            }
            else if (op < 8)
            {
                assertEquals(
                    ad.removeLastOccurrence(k), 
                    deque.removeLast(k) >= 0);
            }
            assertEquals(ad.size(), deque.size());
        }

        assertArrayEquals(ad.toArray(), deque.toArray());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
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
    /*! #end !*/
    
    @Test
    public void testHashCodeEquals()
    {
        KTypeArrayDeque<KType> l0 = new KTypeArrayDeque<>();
        assertEquals(1, l0.hashCode());
        assertEquals(l0, KTypeArrayDeque.from());

        KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(k1, k2, k3);
        KTypeArrayDeque<KType> l2 = KTypeArrayDeque.from(k1, k2);
        l2.addLast(k3);

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }
    
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testHashCodeWithNulls()
    {
        KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(k1, null, k3);
        KTypeArrayDeque<KType> l2 = KTypeArrayDeque.from(k1, null, k3);
        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);
    }
    /*! #end !*/
    
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArray()
    {
        KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(k1, k2, k3);
        Object[] result = l1.toArray();
        assertArrayEquals(new Object [] {k1, k2, k3}, result); // dummy
    }
    /*! #end !*/
    
    @Test
    public void testClone()
    {
        this.deque.addLast(key1, key2, key3);

        KTypeArrayDeque<KType> cloned = deque.clone();
        cloned.removeAll(key1);

        assertSortedListEquals(deque.toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.toArray(), key2, key3);
    }

    @Test
    public void testToString()
    {
        assertEquals("[" 
            + key1 + ", "
            + key2 + ", "
            + key3 + "]", KTypeArrayDeque.from(key1, key2, key3).toString());
    }    
    
    /* */
    @Test
    public void testEqualsSameClass()
    {
      KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(k1, k2, k3);
      KTypeArrayDeque<KType> l2 = KTypeArrayDeque.from(k1, k2, k3);
      KTypeArrayDeque<KType> l3 = KTypeArrayDeque.from(k1, k3, k2);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeArrayDeque<KType> {
        };

        KTypeArrayDeque<KType> l1 = KTypeArrayDeque.from(k1, k2, k3);
        KTypeArrayDeque<KType> l2 = new Sub();
        KTypeArrayDeque<KType> l3 = new Sub();
        l2.addLast(k1); l2.removeFirst(); // no-op
        l2.addLast(l1);
        l3.addLast(l1);

        Assertions.assertThat(l2).isEqualTo(l3);
        Assertions.assertThat(l1).isNotEqualTo(l2);
    }    
}
