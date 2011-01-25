package com.carrotsearch.hppc;

import java.util.ArrayDeque;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;

/**
 * A micro-benchmark test case for comparing {@link ArrayDeque} (used as a {@link Stack}
 * against {@link ObjectStack}.
 */
public class ArrayDequeBenchmark extends AbstractBenchmark
{
    private ArrayDeque<Integer> jre;

    /* */
    @Before
    public void before()
    {
        jre = new ArrayDeque<Integer>();
    }

    /**
     * Test sequential push and pops from the stack (first a lot of pushes, then a lot of
     * pops).
     */
    @Test
    public void testPushPops() throws Exception
    {
        for (int i = 0; i < ObjectStackBenchmark.COUNT; i++)
            jre.push(i);

        while (jre.size() > 0)
            jre.pop();
    }
}
