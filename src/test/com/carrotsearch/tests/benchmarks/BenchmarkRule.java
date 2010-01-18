package com.carrotsearch.tests.benchmarks;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A benchmark rule (causes tests to be repeated and measures). 
 */
public final class BenchmarkRule implements MethodRule
{
    public Statement apply(Statement base, FrameworkMethod method, Object object)
    {
        return new BenchmarkStatement(base, method, object);
    }
}