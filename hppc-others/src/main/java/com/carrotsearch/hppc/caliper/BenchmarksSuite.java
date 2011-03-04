package com.carrotsearch.hppc.caliper;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;

import com.google.common.collect.Lists;

/**
 * Runs the entire suite of benchmarks.
 */
public class BenchmarksSuite
{
    public static void main(String [] args) throws Exception
    {
        if (args.length == 0)
        {
            System.out.println("Args: [all | class-name, class-name, ...]");
            return;
        }

        List<Class<?>> classes = Lists.newArrayList();
        if (args.length == 1 && "all".equals(args[0]))
        {
            Class<?> [] benchmarkClasses = new Class<?> []
            {
                BenchmarkBigramCounting.class, BenchmarkGetWithRemoved.class,
                BenchmarkPut.class
            };

            classes.addAll(Arrays.asList(benchmarkClasses));
        }
        else
        {
            final ClassLoader clLoader = Thread.currentThread().getContextClassLoader();

            for (String clz : args)
            {
                try
                {
                    classes.add(Class.forName(clz, true, clLoader));
                }
                catch (ClassNotFoundException e)
                {
                    System.out.println("Class not found: " + clz);
                    System.exit(-1);
                }
            }
        }

        printSystemInfo();

        header("Benchmarks");
        runBenchmarks(classes);
    }

    /**
     * 
     */
    private static void runBenchmarks(List<Class<?>> classes)
    {
    }

    /**
     * 
     */
    private static void printSystemInfo() throws IOException
    {
        System.out.println("Benchmarks suite starting.");
        System.out.println("Date now: " + new Date() + "\n");

        header("System properties");
        Properties p = System.getProperties();
        for (Object key : new TreeSet<Object>(p.keySet()))
        {
            System.out.println(key + ": "
                + StringEscapeUtils.escapeJava((String) p.getProperty((String) key)));
        }

        header("CPU");

        // Try to determine CPU.
        ExecTask task = new ExecTask();
        task.setVMLauncher(true);
        if (SystemUtils.IS_OS_WINDOWS)
        {
            task.setExecutable("cmd");
            task.createArg().setLine("/c set");
        }
        else
        {
            task.setExecutable("cat");
            task.createArg().setLine("/proc/cpuinfo");
        }
        
        task.setOutputproperty("stdout");
        task.setErrorProperty("stderr");
        
        task.setFailIfExecutionFails(true);
        task.setFailonerror(true);
        
        Project project = new Project();
        task.setProject(project);
        task.execute();

        System.out.println(project.getProperty("stdout"));
    }

    private static void header(String msg)
    {
        System.out.println(StringUtils.repeat("=", 80));
        System.out.println(StringUtils.center(" " + msg + " ", 80, "-"));
        System.out.println(StringUtils.repeat("=", 80));
    }
}
