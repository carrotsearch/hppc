package com.carrotsearch.hppc.examples;

import org.junit.Test;

import com.carrotsearch.hppc.IntCharOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCharCursor;
import com.carrotsearch.hppc.procedures.IntCharProcedure;

public class IteratingOverMaps
{
    IntCharOpenHashMap prepare(int size)
    {
        final IntCharOpenHashMap map = new IntCharOpenHashMap(size);
        for (int i = 0; i < size / 2; i++)
        {
            map.put(i, (char)(64 + i));
        }
        return map;
    }
    
    @Test
    public void testIterableCursor()
    {
        // [[[start:iteration-maps-using-iterator]]]
        // Prepare some set to iterate over
        final IntCharOpenHashMap map = prepare(10);
        
        // Maps implement the Iterable interface that returns [keyType][valueType]Cursors 
        // The cursor contains the key, value and internal index of the current element. 
        for (IntCharCursor c : map)
        {
            System.out.println(c.index + ": " + c.key + " -> " + c.value);
        }
        // [[[end:iteration-maps-using-iterator]]]
    }
    
    @Test
    public void testWithProcedureClosure()
    {
        // [[[start:iteration-maps-using-procedures]]]
        final IntCharOpenHashMap map = prepare(10);

        // Maps also support iteration through [keyType][valueType]Procedure interfaces.
        // The apply() method will be called once for each key/value pair in the map.
        
        // Iteration from head to tail
        map.forEach(new IntCharProcedure()
        {
            public void apply(int key, char value)
            {
                System.out.println(key + " -> " + value);
            }
        });
        // [[[end:iteration-maps-using-procedures]]]
    }

    @Test
    public void testDirectBufferLoop() throws Exception
    {
        // [[[start:iteration-maps-using-direct-buffer-access]]]
        final IntCharOpenHashMap map = prepare(10);
        
        // For the fastest iteration, you can access the sets's data buffers directly.
        final int [] keys = map.keys;
        final char [] values = map.values;
        final byte [] states = map.states;
        
        // Note that the loop is bounded by states.length, not keys.length. This
        // can make the code faster due to range check elimination
        // (http://wikis.sun.com/display/HotSpotInternals/RangeCheckElimination).
        for (int i = 0; i < states.length; i++)
        {
            if (states[i] == IntOpenHashSet.ASSIGNED) {
                System.out.println(keys[i] + " -> " + values[i]);
            }
        }
        // [[[end:iteration-maps-using-direct-buffer-access]]]
    }
}
