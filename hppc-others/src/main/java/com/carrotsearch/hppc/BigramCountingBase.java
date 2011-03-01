package com.carrotsearch.hppc;

import gnu.trove.map.hash.TIntIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import bak.pcj.map.IntKeyIntChainedHashMap;
import bak.pcj.map.IntKeyIntOpenHashMap;

import com.carrotsearch.hppc.hash.IntMurmurHash;

public class BigramCountingBase
{
    /* Prepare some test data */
    public static char [] DATA;

    /* Prevent dead code removal. */
    public volatile int guard;

    @BeforeClass
    public static void prepareData() throws IOException
    {
        byte [] dta = IOUtils.toByteArray(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("books-polish.txt"));
        DATA = new String(dta, "UTF-8").toCharArray();
    }

    public static final class IntHolder
    {
        public int value;
    
        public IntHolder(int initial)
        {
            value = initial;
        }
    }

    @Test
    public void hppc()
    {
        // [[[start:bigram-counting]]]
        // Some character data
        final char [] CHARS = DATA;
        
        // We'll use a int -> int map for counting. A bigram can be encoded
        // as an int by shifting one of the bigram's characters by 16 bits
        // and then ORing the other character to form a 32-bit int.
    
        /* 
         * The input data is specific; it has low variance on lower bits and
         * high variance overall. We need to use a better hashing function
         * than simple identity. MurmurHash is good enough.
         */ 
        final IntIntOpenHashMap map = new IntIntOpenHashMap(
            IntIntOpenHashMap.DEFAULT_CAPACITY, 
            IntIntOpenHashMap.DEFAULT_LOAD_FACTOR, 
            new IntMurmurHash());
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.putOrAdd(bigram, 1, 1);
        }
        // [[[end:bigram-counting]]]
    
        guard = map.size();
    }

    @Test
    public void trove()
    {
        final char [] CHARS = DATA;
        final TIntIntHashMap map = new TIntIntHashMap();
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.adjustOrPutValue(bigram, 1, 1);
        }
    
        guard = map.size();
    }

    @Test
    public void fastutilOpenHashMap()
    {
        final char [] CHARS = DATA;
        final Int2IntOpenHashMap map = new Int2IntOpenHashMap(); 
        map.defaultReturnValue(0);
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.put(bigram, map.get(bigram) + 1);
        }
    
        guard = map.size();
    }

    @Test
    public void fastutilLinkedOpenHashMap()
    {
        final char [] CHARS = DATA;
        final Int2IntLinkedOpenHashMap map = new Int2IntLinkedOpenHashMap(); 
        map.defaultReturnValue(0);
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.put(bigram, map.get(bigram) + 1);
        }
    
        guard = map.size();
    }

    @Test
    public void pcjOpenHashMap()
    {
        final char [] CHARS = DATA;
        final IntKeyIntOpenHashMap map = new IntKeyIntOpenHashMap();
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.put(bigram, map.get(bigram) + 1);
        }
    
        guard = map.size();
    }

    @Test
    public void pcjChainedHashMap()
    {
        final char [] CHARS = DATA;
        final IntKeyIntChainedHashMap map = new IntKeyIntChainedHashMap();
    
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.put(bigram, map.get(bigram) + 1);
        }
    
        guard = map.size();
    }

    @Test
    public void jcf()
    {
        final char [] CHARS = DATA;
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            final Integer currentCount = map.get(bigram);
            map.put(bigram, currentCount == null ? 1 : currentCount + 1);
        }
        
        guard = map.size();
    }

    @Test
    public void jcfWithHolder()
    {
        final char [] CHARS = DATA;
        final Map<Integer, IntHolder> map = new HashMap<Integer, IntHolder>();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            final IntHolder currentCount = map.get(bigram);
            if (currentCount == null)
            {
                map.put(bigram, new IntHolder(1));
            }
            else
            {
                currentCount.value++;
            }
        }
    
        guard = map.size();
    }

    @Test
    public void mahoutCollections()
    {
        final char [] CHARS = DATA;
        final org.apache.mahout.math.map.OpenIntIntHashMap map = 
            new org.apache.mahout.math.map.OpenIntIntHashMap();
        for (int i = 0; i < CHARS.length - 1; i++)
        {
            final int bigram = CHARS[i] << 16 | CHARS[i+1];
            map.adjustOrPutValue(bigram, 1, 1);
        }

        guard = map.size();
    }
    
    public BigramCountingBase()
    {
        super();
    }

}