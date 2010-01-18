package com.carrotsearch.tests.benchmarks;

/**
 * A result of a single test.
 */
class Result
{
    public final long startTime;
    public final long afterGC;
    public final long endTime;

    public Result(long startTime, long afterGC, long endTime)
    {
        this.startTime = startTime;
        this.afterGC = afterGC;
        this.endTime = endTime;
    }

    public long gcTime()
    {
        return afterGC - startTime;
    }

    public long evaluationTime()
    {
        return endTime - afterGC;
    }
}