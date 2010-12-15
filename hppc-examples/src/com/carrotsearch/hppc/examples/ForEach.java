package com.carrotsearch.hppc.examples;

import java.util.*;

import org.junit.Test;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.hash.IntMurmurHash;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

public class ForEach
{
    public void accessAnonymousClassFieldInForEach()
    {
        // [[[start:foreach-counting]]]
        // Create a map.
        final IntIntOpenHashMap map = new IntIntOpenHashMap();
        map.put(1, 2);
        map.put(2, 5);
        map.put(3, 10);

        int count = map.forEach(new IntIntProcedure() {
           int count;
           public void apply(int key, int value)
           {
               if (value >= 5) count++;
           }
        }).count;
        System.out.println("There are " + count + " values >= 5");
        // [[[end:foreach-counting]]]
    }

    @Test
    public void characterCountingWithJcfSmarter()
    {
        final char [] CHARS = DATA;
        final Map<Integer, IntHolder> counts = new HashMap<Integer, IntHolder>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            final IntHolder currentCount = counts.get(bigram);
            if (currentCount == null)
            {
                counts.put(bigram, new IntHolder(1));
            }
            else
            {
                currentCount.value++;
            }
        }
    }
    
    @Test
    public void characterCountingWithJcfNaive()
    {
        final char [] CHARS = DATA;
        final Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            final Integer currentCount = counts.get(bigram);
            if (currentCount == null)
            {
                counts.put(bigram, 1);
            }
            else
            {
                counts.put(bigram, currentCount + 1);
            }
        }
    }

    private static final class IntHolder
    {
        int value;

        IntHolder(int initial)
        {
            value = initial;
        }
    }
}
