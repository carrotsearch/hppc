package com.carrotsearch.hppc.examples;

import org.junit.Test;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;

public class IteratingOverLists
{
    IntArrayList prepare(int size)
    {
        final IntArrayList list = new IntArrayList(size);
        for (int i = 0; i < size; i++)
        {
            list.add(size - i);
        }
        return list;
    }
    
    @Test
    public void testIterableCursor() throws Exception
    {
        // [[[start:iteration-lists-using-iterator]]]
        // Prepare some list to iterate over
        final IntArrayList list = prepare(10);
        
        // Lists implement the Iterable interface that returns [type]Cursor elements.
        // The cursor contains the index and value of the current element.
        for (IntCursor c : list)
        {
            System.out.println(c.index + ": " + c.value);
        }
        // [[[end:iteration-lists-using-iterator]]]
    }

    @Test
    public void testSimpleGetLoop() throws Exception
    {
        // [[[start:iteration-lists-using-get]]]
        final IntArrayList list = prepare(10);
        
        // Another way to iterate over array list is to access each element
        // of the list using the get() method.
        final int size = list.size();
        for (int i = 0; i < size; i++)
        {
            System.out.println(i + ": " + list.get(i));
        }
        // [[[end:iteration-lists-using-get]]]
    }

    @Test
    public void testWithProcedureClosure()
    {
        // [[[start:iteration-lists-using-procedures]]]
        final IntArrayList list = prepare(10);

        // Lists also support iteration through [type]Procedure interfaces.
        // The apply() method will be called once for each element in the list.
        list.forEach(new IntProcedure()
        {
            public void apply(int value)
            {
                System.out.println(value);
            }
        });
        // [[[end:iteration-lists-using-procedures]]]
    }

    @Test
    public void testDirectBufferLoop() throws Exception
    {
        // [[[start:iteration-lists-using-direct-buffer-access]]]
        final IntArrayList list = prepare(10);

        // For the fastest iteration, you can access the lists' data buffer directly.
        final int [] buffer = list.buffer;
        
        // Make sure you take the list.size() and not the length of the data buffer.
        final int size = list.size();
        
        // Iterate of the the array as usual.
        for (int i = 0; i < size; i++)
        {
            System.out.println(i + ": " + buffer[i]);
        }
        // [[[end:iteration-lists-using-direct-buffer-access]]]
    }
}
