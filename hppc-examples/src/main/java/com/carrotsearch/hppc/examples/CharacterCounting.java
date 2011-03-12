package com.carrotsearch.hppc.examples;

import java.util.*;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.hppc.CharIntOpenHashMap;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.hppc.procedures.CharIntProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

@BenchmarkOptions(callgc = false, warmupRounds = 3)
public class CharacterCounting extends AbstractBenchmark
{
    /* Prepare some test data */
    private static final char [] DATA = new char [1024 * 1024 * 64];
    static
    {
        final Random random = new Random(0);
        for (int i = 0; i < DATA.length; i++)
        {
            DATA[i] = (char) (random.nextInt(127 - 32) + 32);
        }
    }

    @Test
    public void characterCountingWithHppc()
    {
        // [[[start:character-counting]]]
        // Some characters to count
        final char [] CHARS = DATA;
        
        // We'll use a char -> int map for counting. If you know the likely
        // number of distinct key values in the map, pass it to the constructor
        // to avoid resizing of the map.
        final CharIntOpenHashMap counts = new CharIntOpenHashMap(256);
        
        // Loop over the characters and increase their corresponding entries in the map
        for (int i = 0; i < CHARS.length; i++)
        {
            // Adds 1 if the entry exists, sets 1 if the entry does not exist
            counts.putOrAdd(CHARS[i], 1, 1);
        }
        // [[[end:character-counting]]]

        final TotalCounter counter = new TotalCounter();
        counts.forEach(counter);
        Assert.assertEquals(counter.total, DATA.length);
    }

    @Test
    @Ignore
    public void characterCountingMinimal()
    {
        int [] counts = new int [Character.MAX_VALUE];

        for (int i = DATA.length; --i >= 0;)
        {
            counts[DATA[i]]++;
        }

        int total = 0;
        for (int i = counts.length; --i >= 0;)
        {
            total += counts[i];
        }
        Assert.assertEquals(total, DATA.length);
    }

    @Test
    @Ignore
    public void characterCountingWithJcfNaive()
    {
        final Map<Character, Integer> counts = new HashMap<Character, Integer>(256);
        for (int i = 0; i < DATA.length; i++)
        {
            final char currentChar = DATA[i];
            final Integer currentCount = counts.get(currentChar);
            if (currentCount == null)
            {
                counts.put(currentChar, 1);
            }
            else
            {
                counts.put(currentChar, currentCount + 1);
            }
        }

        int total = 0;
        for (Integer count : counts.values())
        {
            total += count;
        }
        Assert.assertEquals(total, DATA.length);
    }

    @Test
    public void characterCountingWithJcfSmarter()
    {
        final Map<Character, IntHolder> counts = new HashMap<Character, IntHolder>(256);
        for (int i = 0; i < DATA.length; i++)
        {
            final char currentChar = DATA[i];
            final IntHolder currentCount = counts.get(currentChar);
            if (currentCount == null)
            {
                counts.put(currentChar, new IntHolder(1));
            }
            else
            {
                currentCount.value++;
            }
        }

        int total = 0;
        for (IntHolder count : counts.values())
        {
            total += count.value;
        }
        Assert.assertEquals(total, DATA.length);
    }

    private final class TotalCounter implements CharIntProcedure
    {
        public int total = 0;

        public void apply(char key, int value)
        {
            total += value;
        }
    }
}
