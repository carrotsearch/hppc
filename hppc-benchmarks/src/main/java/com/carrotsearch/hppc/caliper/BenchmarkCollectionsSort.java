package com.carrotsearch.hppc.caliper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.common.collect.Lists;

/**
 * Benchmark {@link Collections#sort(java.util.List)}, in particular for differences
 * in the new JDK 1.7 (TimSort).
 */
public class BenchmarkCollectionsSort extends SimpleBenchmark
{
    @Param("100000")
    public int size;
    
    private ArrayList<String> data;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.data = Lists.newArrayList();
        for (int i = 0; i < size; i++)
            data.add(Integer.toString(i));
    }

    public int timeSort(int reps)
    {
        final List<String> dataClone = Lists.newArrayList(data);

        int count = 0;
        for (int i = 0; i < reps; i++)
        {
            Collections.sort(dataClone);
            count += Integer.parseInt(dataClone.get(0));
        }

        return count;
    }
    
    public static void main(String [] args)
    {
        Runner.main(BenchmarkCollectionsSort.class, args);
    }
}
