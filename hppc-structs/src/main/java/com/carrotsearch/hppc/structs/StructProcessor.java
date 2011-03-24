package com.carrotsearch.hppc.structs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.*;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.tools.generic.EscapeTool;

import com.carrotsearch.hppc.annotations.Struct;
import com.google.common.io.Closeables;

/**
 * Generate extra utilities for a structure class.
 */
@SupportedAnnotationTypes("com.carrotsearch.hppc.annotations.Struct")
@SupportedSourceVersion(SourceVersion.RELEASE_5)
public class StructProcessor extends AbstractProcessor
{
    /**
     * Ignored Eclipse compilation phases.
     */
    private final static Set<String> ignoredPhases = new HashSet<String>(Arrays.asList(
        "RECONCILE", "OTHER"));

    /**
     * Mirror element utilities.
     */
    private Elements elements;

    /**
     * Apt filer utilities.
     */
    private Filer filer;

    /**
     * Round number.
     */
    private int round;

    /**
     * Messager.
     */
    private Messager messager;

    /**
     * Initialize processing environment.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);

        elements = this.processingEnv.getElementUtils();
        filer = this.processingEnv.getFiler();
        messager = this.processingEnv.getMessager();

        round = 0;
    }

    /*
     * 
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env)
    {
        // Check for Eclipse reconciliation phase and skip it.
        final String phase = super.processingEnv.getOptions().get("phase");
        if (phase != null && ignoredPhases.contains(phase))
        {
            return false;
        }

        // Check for any previous errors and skip.
        if (env.errorRaised())
        {
            return false;
        }

        // Clear any previous junk.
        final long start = System.currentTimeMillis();

        // Scan for all types marked with @Bindable and processed in this round.
        round++;

        int count = 0;
        for (TypeElement e : ElementFilter.typesIn(env
            .getElementsAnnotatedWith(Struct.class)))
        {
            // e can be null in Eclipse, so check for this case.
            if (e == null) continue;
            try
            {
                processType(e);
            }
            catch (IOException e1)
            {
                messager.printMessage(
                    Kind.ERROR,
                    "Could not process type " + e.getQualifiedName() + ": "
                        + e1.getMessage());
            }
            count++;
        }

        round++;
        if (count > 0)
        {
            System.out.println(String.format(Locale.ENGLISH,
                "%d classes processed in round %d (%.2f secs.)", count, round,
                (System.currentTimeMillis() - start) / 1000.0));
        }

        return false;
    }

    /*
     * 
     */
    private void processType(TypeElement type) throws IOException
    {
        String fullTypeName = elements.getBinaryName(type).toString();
        String packageName = elements.getPackageOf(type).getQualifiedName().toString();
        String outputTypeName = type.getSimpleName().toString() + "Vector";

        PrintWriter w = null;
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try
        {
            w = new PrintWriter(filer.createSourceFile(
                packageName + "." + outputTypeName, type).openWriter());

            final RuntimeInstance velocity = createInstance(messager);
            final VelocityContext context = createContext();
            context.put("packageName", packageName);
            context.put("fullTypeName", fullTypeName);
            context.put("sourceType", type);
            context.put("outputTypeName", outputTypeName);
            final Template template = velocity.getTemplate("TypeVector.template", "UTF-8");
            template.merge(context, w);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not serialize metadata for: "
                + type.toString(), e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(ccl);
            if (w != null) Closeables.closeQuietly(w);
        }        
    }
    
    /**
     * Initialize Velocity engine instance, disables logging, sets bundle-relative
     * resource loader.
     */
    private RuntimeInstance createInstance(final Messager msg)
    {
        try
        {
            final ExtendedProperties p = new ExtendedProperties();
            p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "true");
            p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());

            p.setProperty("resource.loader", "apt");
            p.setProperty("apt.resource.loader.instance",
                new ClassRelativeResourceLoader(msg, this.getClass()));

            final RuntimeInstance velocity = new RuntimeInstance();
            velocity.setConfiguration(p);
            return velocity;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Velocity initialization failed.", e);
        }
    }

    /**
     * Create Velocity context and place default tools into it.
     */
    private VelocityContext createContext()
    {
        final VelocityContext context = new VelocityContext();
        context.put("esc", new EscapeTool());
        context.put("stringutils", new StringUtils());
        return context;
    }
}
