package com.carrotsearch.hppc.caliper;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Benchmark perturbations vs. no-perturbations.
 */
public class BenchmarkPerturbedVsHashedOnly extends SimpleBenchmark
{
    @Param(
    {
        "1000000"
    })
    public int size;

    public int timePerturbed(int reps)
    {
        int count = 0;
        while (reps-- > 0) {
            IntOpenHashSet set = new IntOpenHashSet();
            for (int i = size; --i >= 0;)
            {
                set.add(i);
            }
            count += set.size();
        }
        return count;
    }

    public int timeUnperturbed(int reps)
    {
        int count = 0;
        while (reps-- > 0) {
            IntOpenHashSet set = new IntOpenHashSet() {
                protected int computePerturbationValue(int capacity)
                {
                    return 0;
                }
            };
            for (int i = size; --i >= 0;)
            {
                set.add(i);
            }
            count += set.size();
        }
        return count;
    }

    public static void main(String [] args)
    {
        Runner.main(BenchmarkPerturbedVsHashedOnly.class, args);
    }
}