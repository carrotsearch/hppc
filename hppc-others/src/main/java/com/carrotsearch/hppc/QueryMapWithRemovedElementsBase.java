package com.carrotsearch.hppc;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Random;

import org.apache.mahout.math.Arrays;

import com.google.caliper.*;

/**
 * Create a large map of int keys, remove portion of the keys and then with
 * half/half keys and a some random values.
 */
public class QueryMapWithRemovedElementsBase extends SimpleBenchmark
{
    /* Prepare some test data */
    public int [] KEYS;
    public int [] QUERY_KEYS;

    public static abstract class TestedMapImpl<T>
    {
        protected T prepared;

        public final void prepare(int [] keys, int removeKeys, Random rnd)
        {
            prepared = prepare0(keys);

            keys = shuffle(Arrays.copyOf(keys, keys.length), rnd);
            for (int i = 0; removeKeys > 0; removeKeys--, i++)
            {
                removeKey(keys[i]);
            }
        }

        public final int query(int [] queryKeys, int reps)
        {
            int count = 0;
            for (int i = 0; i < reps; i++)
            {
                count += query0(queryKeys);
            }
            return count;
        }

        protected abstract int query0(int [] keys);

        protected abstract T prepare0(int [] keys);

        protected abstract void removeKey(int key);

        public final void cleanup()
        {
            prepared = null;
        }
    }

    public static class HppcMap extends TestedMapImpl<IntIntOpenHashMap>
    {
        @Override
        protected void removeKey(int key)
        {
            prepared.remove(key);
        }

        @Override
        protected int query0(int [] keys)
        {
            final IntIntOpenHashMap prepared = this.prepared;
            int count = 0;
            for (int i = 0; i < keys.length; i++)
                count += prepared.containsKey(keys[i]) ? 1 : 0;
            return count;
        }

        protected IntIntOpenHashMap prepare0(int [] keys)
        {
            return IntIntOpenHashMap.from(keys, new int [keys.length]);
        }
    }
    
    public static class FastUtilMap extends TestedMapImpl<Int2IntOpenHashMap>
    {
        @Override
        protected void removeKey(int key)
        {
            prepared.remove(key);
        }

        @Override
        protected int query0(int [] keys)
        {
            final Int2IntOpenHashMap prepared = this.prepared;
            int count = 0;
            for (int i = 0; i < keys.length; i++)
                count += prepared.containsKey(keys[i]) ? 1 : 0;
            return count;
        }

        @Override
        protected Int2IntOpenHashMap prepare0(int [] keys)
        {
            return new Int2IntOpenHashMap(keys, new int [keys.length]);
        }
    }

    public static enum Implementation
    {
        HPPC
        {
            public TestedMapImpl<?> getInstance()
            {
                return new HppcMap();
            }
        },
        
        FASTUTIL
        {
            @Override
            public TestedMapImpl<?> getInstance()
            {
                return new FastUtilMap();
            }
        };

        public abstract TestedMapImpl<?> getInstance();
    }

    @Param({"0", "0.25", "0.5", "0.75", "1"})
    public double removedKeys;

    @Param
    public Implementation implementation;
    public TestedMapImpl<?> testInstance;

    @Param({"1000000", "100000"})
    public int size;

    /*
     * Prepare pseudo-random data from a fixed seed.
     */
    private static int [] prepareData(int len)
    {
        Random rnd = new Random(0x11223344);
        int [] randomData = new int [len];
        for (int i = 0; i < len; i++)
            randomData[i] = rnd.nextInt();

        return randomData;
    }

    @Override
    protected void setUp() throws Exception
    {
        // Random keys
        KEYS = prepareData(size);

        // Half keys, half random. Shuffle order.
        Random rnd = new Random(0x11223344);

        QUERY_KEYS = Arrays.copyOf(KEYS, KEYS.length);
        for (int i = 0; i < QUERY_KEYS.length / 2; i++)
            QUERY_KEYS[i] = rnd.nextInt();

        shuffle(QUERY_KEYS, rnd);

        testInstance = implementation.getInstance();
        testInstance.prepare(KEYS, (int) (removedKeys * KEYS.length), rnd);
    }

    private static int [] shuffle(int [] array, Random rnd)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            int pos = rnd.nextInt(i + 1);
            int t = array[pos];
            array[pos] = array[i];
            array[i] = t;
        }
        return array;
    }

    public int timeQueriesHalfFull(int reps)
    {
        return testInstance.query(QUERY_KEYS, reps);
    }

    @Override
    protected void tearDown() throws Exception
    {
        testInstance.cleanup();
    }

    public static void main(String [] args)
    {
        Runner.main(QueryMapWithRemovedElementsBase.class, 
            "--timeUnit", "ms"
            //"-Dsize=1000000",
            //"-DremovedKeys=0.8,0.9,0.99,1",
            //"-Dimplementation=FASTUTIL"
        );
    }
}