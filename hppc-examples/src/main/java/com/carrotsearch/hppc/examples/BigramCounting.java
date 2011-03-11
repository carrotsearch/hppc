package com.carrotsearch.hppc.examples;

import java.util.*;

import org.junit.Test;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.hash.MurmurHash3.IntMurmurHash;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

public class BigramCounting extends AbstractBenchmark
{
    /* Prepare some test data */
    private static final char [] DATA = new char [1024 * 1024];
    static
    {
        final Random random = new Random(0);
        for (int i = 0; i < DATA.length; i++)
        {
            DATA[i] = (char) random.nextInt(Character.MAX_VALUE);
        }
    }

    @Test
    public void characterCountingWithHppc()
    {
        // [[[start:bigram-counting]]]
        // Some character data
        final char [] CHARS = DATA;
        
        /* 
         * We'll use a int -> int map for counting. A bigram can be encoded
         * as an int by shifting one of the bigram's characters by 16 bits
         * and then ORing the other character to form a 32-bit int.
         * 
         * The input data is specific; it has low variance on lower bits and
         * high variance overall. We need to use a better hashing function
         * than simple identity. MurmurHash is good enough.
         */ 
        final IntIntOpenHashMap counts = new IntIntOpenHashMap(
            IntIntOpenHashMap.DEFAULT_CAPACITY, 
            IntIntOpenHashMap.DEFAULT_LOAD_FACTOR, 
            new IntMurmurHash());

        for (int i = 0; i < CHARS.length - 1; i++)
        {
            counts.putOrAdd((CHARS[i] << 16 | CHARS[i+1]), 1, 1);
        }
        // [[[end:bigram-counting]]]
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
}
