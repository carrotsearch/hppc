package com.carrotsearch.hppc.structs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * In-process invocation of javac (for debugging).
 */
public class RunStructProcessorInProcess
{
    public static void main(String [] args) throws IOException
    {
        File out = new File("target/tmp");
        out.mkdirs();

        JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> dl = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager sm = c.getStandardFileManager(dl, null, null);
        sm.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(out));
        Iterable<? extends JavaFileObject> files = sm.getJavaFileObjects(new File(
            "../hppc-examples/src/main/java/com/carrotsearch/hppc/examples/BattleshipsCell.java")
        .getAbsoluteFile());
        CompilationTask task = c.getTask(null, sm, dl, null, null, files);
        task.setProcessors(Arrays.asList(new StructProcessor()));
        task.call();
        System.out.println("Done: " + dl.getDiagnostics());
        
        // System.out.println(Files.toString(new File( "tmp/examples/PersonArray.java"), Charsets.UTF_8));
        
        java.lang.reflect.Array.newInstance(int[].class, 10);
    }
}
