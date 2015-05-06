package com.carrotsearch.hppc.examples;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.procedures.IntIntProcedure;

public class HppcExample_005_ForEachValues
{
    public void accessAnonymousClassFieldInForEach()
    {
        // [[[start:foreach-counting]]]
        // Create a map.
        final IntIntHashMap map = new IntIntHashMap();
        map.put(1, 2);
        map.put(2, 5);
        map.put(3, 10);

        int count = map.forEach(new IntIntProcedure()
        {
           int count;
           public void apply(int key, int value)
           {
               if (value >= 5) count++;
           }
        }).count;
        System.out.println("There are " + count + " values >= 5");
        // [[[end:foreach-counting]]]
    }
}
