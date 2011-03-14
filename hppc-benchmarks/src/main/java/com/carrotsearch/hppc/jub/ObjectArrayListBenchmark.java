package com.carrotsearch.hppc.jub;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.hppc.procedures.ObjectProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.*;

/**
 * Benchmark tests for {@link ObjectArrayList}.
 */
@BenchmarkHistoryChart(filePrefix="CLASSNAME.history", maxRuns=50)
@BenchmarkMethodChart(filePrefix="CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 5, benchmarkRounds = 10)
public class ObjectArrayListBenchmark extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 50;
    private static ObjectArrayList<Object> list;

    private static final Object defValue = null;

    /* */
    @BeforeClass
    public static void before()
    {
        list = new ObjectArrayList<Object>();
        list.resize(CELLS);
    }
    
    @AfterClass
    public static void cleanup()
    {
        list = null;
    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        final ObjectArrayList<Object> list = ObjectArrayListBenchmark.list;
        final int max = list.size();
        int count = 0;
        for (int i = 0; i < max; i++)
        {
            if (list.get(i) != defValue)
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testDirectBufferLoop() throws Exception
    {
        final int size = list.size();
        final Object [] buffer = list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            if (buffer[i] != defValue) 
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testIterableCursor() throws Exception
    {
        int count = 0;
        for (ObjectCursor<Object> c : list)
        {
            if (c.value != defValue)
                count++;
        }
        Assert.assertEquals(0, count);
    }

    /* */
    @Test
    public void testWithProcedureClosure()
    {
        final IntHolder count = new IntHolder();
        list.forEach(new ObjectProcedure<Object>() {
            public void apply(Object v)
            {
                if (v != defValue)
                    count.value++;
            }
        });
        Assert.assertEquals(0, count.value);
    }

    /* */
    @Test
    public void testDirectBufferWithNewFor() throws Exception
    {
        int count = 0;
        for (Object c : list.buffer)
        {
            if (defValue != c)
                count++;
        }
        Assert.assertEquals(0, count);
    }
}
