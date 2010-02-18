package com.carrotsearch.hppc;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

/**
 * Benchmark tests for {@link ObjectArrayList}.
 */
@BenchmarkOptions(callgc = false, warmupRounds = 10, benchmarkRounds = 10)
/* removeIf:primitive */ 
@SuppressWarnings("unchecked")
/* end:removeIf */
public class ObjectArrayListBenchmark<KType> extends AbstractBenchmark
{
    public static final int CELLS = (1024 * 1024) * 10;
    private static ObjectArrayList<Object> singleton;
    private ObjectArrayList<KType> list = (ObjectArrayList<KType>) singleton;
    public static volatile /* replaceIf:primitiveKType KType */ Object /* end:replaceIf */ value; 

    /* */
    @BeforeClass
    public static void before()
    {
        singleton = new ObjectArrayList<Object>();
        singleton.resize(CELLS);
    }

    /* */
    @Test
    public void testSimpleGetLoop() throws Exception
    {
        for (int i = 0; i < list.size(); i++)
        {
            value = list.get(i);
        }
    }
    
    /* */
    @Test
    public void testDirectBufferLoop() throws Exception
    {
        final int size = list.size();
        final KType [] buffer = list.buffer;
        for (int i = 0; i < size; i++)
        {
            value = buffer[i];
        }
    }
    
    /* */
    @Test
    public void testIterableCursor() throws Exception
    {
        for (ObjectCursor<KType> c : list)
        {
            value = c.value;
        }
    }

    /* */
    @Test
    public void testWithProcedureClosure()
    {
        list.forEach(new ObjectProcedure<KType>() {
            public void apply(KType v)
            {
                value = v;
            }
        });
    }

    /* */
    @Test
    public void testDirectBufferWithNewFor() throws Exception
    {
        for (KType c : (KType[]) list.buffer)
        {
            value = (KType) c;
        }
    }
}
