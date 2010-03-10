package com.carrotsearch.hppc;

import org.junit.*;

import com.carrotsearch.hppc.cursors.ByteCursor;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.hppc.procedures.ByteProcedure;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkMethodChart;

/**
 * Various iteration approaches on an integer list.
 */
@BenchmarkHistoryChart(filePrefix="CLASSNAME.history", maxRuns=50)
@BenchmarkMethodChart(filePrefix="CLASSNAME.methods")
@BenchmarkOptions(callgc = false, warmupRounds = 10, benchmarkRounds = 10)

public class IterationSpeedBenchmark extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 200;
    private static ByteArrayList list;
    public volatile int guard; 

    /* */
    @BeforeClass
    public static void before()
    {
        list = new ByteArrayList();
        list.resize(CELLS);
    }

    @AfterClass
    public static void after()
    {
        list = null;
    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        int count = 0;
        for (int i = 0; i < list.size(); i++)
        {
            count += list.get(i);
        }

        this.guard = count;
    }

    /* */
    @Test
    public void testDirectBufferLoop() throws Exception
    {
        final int size = list.size();
        final byte [] buffer = list.buffer;
        int count = 0;
        for (int i = 0; i < size; i++)
        {
            count += buffer[i];
        }
        this.guard = count;
    }
    
    /* */
    @Test
    public void testIterableCursor() throws Exception
    {
        int count = 0;
        for (ByteCursor c : list)
        {
            count += c.value;
        }
        this.guard = count;
    }

    /* */
    @Test
    public void testWithProcedureClosure()
    {
        final IntHolder holder = new IntHolder();
        list.forEach(new ByteProcedure() {
            public void apply(byte v)
            {
                holder.value += v;
            }
        });
        this.guard = holder.value;
    }

    /* */
    @Test
    public void testDirectBufferWithNewFor() throws Exception
    {
        int count = 0;
        for (int c : list.buffer)
        {
            count += c;
        }
        this.guard = count;
    }
}
