package com.carrotsearch.hppc.examples;

import org.junit.Test;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.procedures.IntProcedure;

public class IteratingOverSets
{
    IntOpenHashSet prepare(int size)
    {
        final IntOpenHashSet set = new IntOpenHashSet(size);
        for (int i = 0; i < size / 2; i++)
        {
            set.add(i);
        }
        return set;
    }
    
    @Test
    public void testIterableCursor()
    {
        // [[[start:iteration-sets-using-iterator]]]
        // Prepare some set to iterate over
        final IntOpenHashSet set = prepare(10);
        
        // Sets implement the Iterable interface that returns [type]Cursor elements.
        // The cursor contains the index and value of the current element. 
        for (IntCursor c : set)
        {
            System.out.println(c.index + ": " + c.value);
        }
        // [[[end:iteration-sets-using-iterator]]]
    }
    
    @Test
    public void testWithProcedureClosure()
    {
        // [[[start:iteration-sets-using-procedures]]]
        final IntOpenHashSet set = prepare(10);

        // Sets also support iteration through [type]Procedure interfaces.
        // The apply() method will be called once for each element in the set.
        
        // Iteration from head to tail
        set.forEach(new IntProcedure()
        {
            public void apply(int value)
            {
                System.out.println(value);
            }
        });
        // [[[end:iteration-sets-using-procedures]]]
    }

    @Test
    public void testDirectBufferLoop() throws Exception
    {
        // [[[start:iteration-sets-using-direct-buffer-access]]]
        final IntOpenHashSet set = prepare(10);
        
        // For the fastest iteration, you can access the sets's data buffers directly.
        final int [] keys = set.keys;
        final byte [] states = set.states;
        
        // Note that the loop is bounded by states.length, not keys.length. This
        // can make the code faster due to range check elimination
        // (http://wikis.sun.com/display/HotSpotInternals/RangeCheckElimination).
        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == IntOpenHashSet.ASSIGNED) {
                System.out.println(keys[i]);
            }
        }
        // [[[end:iteration-sets-using-direct-buffer-access]]]
    }
}
