package com.carrotsearch.hppc.examples;

import org.junit.Test;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;

public class IteratingOverDeques
{
    IntArrayDeque prepare(int size)
    {
        final IntArrayDeque deque = new IntArrayDeque(size);
        for (int i = 0; i < size; i++)
        {
            deque.addFirst(i);
        }
        return deque;
    }
    
    @Test
    public void testIterableCursor()
    {
        // [[[start:iteration-deques-using-iterator]]]
        // Prepare some deque to iterate over
        final IntArrayDeque deque = prepare(10);
        
        // Deques implement the Iterable interface that returns [type]Cursor elements.
        // The cursor contains the index and value of the current element. 
        
        // Please note that the for-each loop as below will always
        // iterate from the deque's head to the deque's tail.
        for (IntCursor c : deque)
        {
            System.out.println(c.index + ": " + c.value);
        }
        // [[[end:iteration-deques-using-iterator]]]
    }
    
    @Test
    public void testWithProcedureClosure()
    {
        // [[[start:iteration-deques-using-procedures]]]
        final IntArrayDeque deque = prepare(10);

        // Deques also support iteration through [type]Procedure interfaces.
        // The apply() method will be called once for each element in the deque.
        
        // Iteration from head to tail
        deque.forEach(new IntProcedure()
        {
            public void apply(int value)
            {
                System.out.println(value);
            }
        });
        // [[[end:iteration-deques-using-procedures]]]
    }

    @Test
    public void testDirectBufferLoop() throws Exception
    {
        // [[[start:iteration-deques-using-direct-buffer-access]]]
        final IntArrayDeque deque = prepare(10);
        
        // For the fastest iteration, you can access the deque's data buffer directly.
        // Note that this time it's a little more complicated than with array lists.
        final int [] buffer = deque.buffer;
        final int bufferSize = buffer.length;
        
        // Iteration from head to tail
        final int forwardStart = deque.head;
        final int forwardStop = forwardStart + deque.size();
        for (int i = forwardStart; i < forwardStop; i++)
        {
            System.out.println(buffer[i % bufferSize]);
        }
        
        // Iteration from tail to head
        final int backwardStart = deque.tail + bufferSize - 1;
        final int backwardStop = backwardStart - deque.size();
        for (int i = backwardStart; i > backwardStop; i--)
        {
            System.out.println(buffer[i % bufferSize]);
        }
        // [[[end:iteration-deques-using-direct-buffer-access]]]
    }
}
