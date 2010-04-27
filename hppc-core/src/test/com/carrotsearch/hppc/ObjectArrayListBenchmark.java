package com.carrotsearch.hppc;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.hppc.procedures.ObjectProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkMethodChart;

/**
 * Benchmark tests for {@link ObjectArrayList}.
 */
@BenchmarkHistoryChart(filePrefix="CLASSNAME.history", maxRuns=50)
@BenchmarkMethodChart(filePrefix="CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 5, benchmarkRounds = 10)
/* removeIf:primitive */ 
@SuppressWarnings("unchecked")
/* end:removeIf */
public class ObjectArrayListBenchmark<KType> extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 50;
    private static ObjectArrayList<Object> singleton;
    private ObjectArrayList<KType> list = (ObjectArrayList<KType>) singleton; 

    private static final /* replaceIf:primitiveKType KType */ 
        Object /* end:replaceIf */ defValue = Intrinsics.defaultKTypeValue();

    /* */
    @BeforeClass
    public static void before()
    {
        singleton = new ObjectArrayList<Object>();
        singleton.resize(CELLS);
    }
    
    @AfterClass
    public static void cleanup()
    {
        singleton = null;
    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        final ObjectArrayList<KType> list = this.list;
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
        final KType [] buffer = list.buffer;
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
        for (ObjectCursor<KType> c : list)
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
        list.forEach(new ObjectProcedure<KType>() {
            public void apply(KType v)
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
        for (KType c : (KType[]) list.buffer)
        {
            if (defValue != (KType) c)
                count++;
        }
        Assert.assertEquals(0, count);
    }
}
