package com.carrotsearch.hppc.caliper;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.*;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;

import com.google.caliper.Benchmark;
import com.google.caliper.Runner;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

/**
 * Runs the entire suite of benchmarks.
 */
public class BenchmarkSuite
{
    @SuppressWarnings("unchecked")
    private final static Class<? extends Benchmark> [] ALL_BENCHMARKS = new Class []
    {
        BenchmarkBigramCounting.class, BenchmarkGetWithRemoved.class, BenchmarkPut.class
    };

    public static void main(String [] args) throws Exception
    {
        if (args.length == 0)
        {
            System.out.println("Args: [all | class-name, class-name, ...] [-- <benchmark args>]");
            System.out.println("Known benchmark classes: ");
            for (Class<?> clz : ALL_BENCHMARKS)
            {
                System.out.println("\t" + clz.getName());
            }
            return;
        }

        Deque<String> argsList = new ArrayDeque<String>(Arrays.asList(args));
        List<Class<? extends Benchmark>> classes = Lists.newArrayList();
        while (!argsList.isEmpty())
        {
            if ("--".equals(argsList.peekFirst()))
            {
                argsList.removeFirst();
                break;
            }
            else if ("all".equals(argsList.peekFirst()))
            {
                argsList.removeFirst();
                classes.addAll(Arrays.asList(ALL_BENCHMARKS));
            }
            else
            {
                final ClassLoader clLoader = Thread.currentThread()
                    .getContextClassLoader();

                String clz = argsList.removeFirst();
                try
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends Benchmark> clzInstance = 
                        (Class<? extends Benchmark>) Class.forName(clz, true, clLoader);

                    if (!Benchmark.class.isAssignableFrom(clzInstance))
                    {
                        System.out.println("Not a benchmark class: " + clz);
                        System.exit(-1);
                    }

                    classes.add(clzInstance);
                }
                catch (ClassNotFoundException e)
                {
                    System.out.println("Class not found: " + clz);
                    System.exit(-1);
                }
            }
        }

        printSystemInfo();
        runBenchmarks(classes, argsList.toArray(new String [argsList.size()]));
    }

    /**
     * 
     */
    private static void runBenchmarks(List<Class<? extends Benchmark>> classes,
        String [] args) throws Exception
    {
        int i = 0;
        for (Class<? extends Benchmark> clz : classes)
        {
            header(clz.getSimpleName() + " (" + (++i) + "/" + classes.size() + ")");
            new Runner().run(ObjectArrays.concat(args, clz.getName()));
        }
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
        task.setOutputproperty("stdout");
        task.setErrorProperty("stderr");

        task.setFailIfExecutionFails(true);
        task.setFailonerror(true);

        Project project = new Project();
        task.setProject(project);

        String pattern = ".*";
        if (SystemUtils.IS_OS_WINDOWS)
        {
            task.setExecutable("cmd");
            task.createArg().setLine("/c set");
            pattern = "PROCESSOR";
        }
        else
        {
            if (new File("/proc/cpuinfo").exists())
            {
                task.setExecutable("cat");
                task.createArg().setLine("/proc/cpuinfo");
            }
            else
            {
                task.setExecutable("sysctl");
                task.createArg().setLine("-a");
                pattern = "(kern\\..*)|(hw\\..*)|(machdep\\..*)";
            }
        }

        try
        {
            task.execute();

            String property = project.getProperty("stdout");
            // Restrict to processor related data only.
            Pattern patt = Pattern.compile(pattern);
            for (String line : IOUtils.readLines(new StringReader(property)))
            {
                if (patt.matcher(line).find())
                {
                    System.out.println(line);
                }
            }
        }
        catch (Throwable e)
        {
            System.out.println("WARN: CPU information could not be extracted: "
                + e.getMessage());
        }
    }

    private static void header(String msg)
    {
        System.out.println();
        System.out.println(StringUtils.repeat("=", 80));
        System.out.println(StringUtils.center(" " + msg + " ", 80, "-"));
        System.out.println(StringUtils.repeat("=", 80));
        System.out.flush();
    }
}
