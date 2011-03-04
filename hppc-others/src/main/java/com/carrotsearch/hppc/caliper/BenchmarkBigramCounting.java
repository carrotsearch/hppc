package com.carrotsearch.hppc.caliper;

import com.carrotsearch.hppc.BigramCounting;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Caliper version of {@link BigramCounting}.
 */
public class BenchmarkBigramCounting extends SimpleBenchmark
{
    private BigramCounting bc;

    @Param public Library library;

    public static enum Library
    {
        HPPC,
        TROVE,
        FASTUTIL_OPEN,
        FASTUTIL_LINKED,
        PCJ_OPEN,
        PCJ_CHAINED,
        JCF,
        JCF_HOLDER,
        MAHOUT
    }

    static
    {
        try
        {
            BigramCounting.prepareData();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        bc = new BigramCounting();
    }

    public int timeLibrary(int reps)
    {
        for (int i = 0; i < reps; i++)
        {
            switch (library)
            {
                case HPPC: bc.hppc(); break;
                case TROVE: bc.trove(); break;
                case FASTUTIL_LINKED: bc.fastutilLinkedOpenHashMap(); break;
                case FASTUTIL_OPEN: bc.fastutilOpenHashMap(); break;
                case PCJ_CHAINED: bc.pcjChainedHashMap(); break;
                case PCJ_OPEN: bc.pcjOpenHashMap(); break;
                case JCF: bc.jcf(); break;
                case JCF_HOLDER: bc.jcfWithHolder(); break;
                case MAHOUT: bc.mahoutCollections(); break;
            }
        }

        // No need to return computation result because BigramCounting saves a guard (?).
        return 1;
    }
    
    public static void main(String [] args)
    {
        Runner.main(BenchmarkBigramCounting.class, args);
    }
}
