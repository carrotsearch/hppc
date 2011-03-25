package com.carrotsearch.hppc.structs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.*;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;

import com.carrotsearch.hppc.annotations.Struct;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
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
     * Apt types utilities.
     */
    private Types types;

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
        types = this.processingEnv.getTypeUtils();

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
                validateType(e);
                processType(e);
            }
            catch (Exception e1)
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

    /**
     * Validate if the structure type has all the required properties.
     */
    private void validateType(TypeElement e) throws IOException
    {
        @SuppressWarnings("unchecked")
        List<Predicate<TypeElement>> predicates = Lists.newArrayList(
            validateTypePublic, 
            validateTypeFinal,
            validateTypeExtendsObject,
            validateTypeHasArgLessPublicConstructor);

        Predicate<TypeElement> combined = Predicates.and(predicates);
        if (!combined.apply(e))
        {
            final StringBuilder b = new StringBuilder("Type " + e.getQualifiedName()
                + " does not validate as a structure:\n");
            for (Predicate<TypeElement> p : predicates)
            {
                if (!p.apply(e))
                {
                    b.append(p.toString());
                    b.append("\n");
                }
            }
            
            throw new IOException(b.toString());
        }
    }

    /* */
    private Predicate<TypeElement> validateTypeHasArgLessPublicConstructor = new Predicate<TypeElement>()
    {
        public boolean apply(TypeElement type)
        {
            for (ExecutableElement constructor : ElementFilter.constructorsIn(type.getEnclosedElements()))
            {
                if (constructor.getModifiers().contains(Modifier.PUBLIC)
                    && constructor.getParameters().isEmpty())
                    return true;
            }
            return false;
        }

        public String toString()
        {
            return "Structure class must have a parameterless constructor."; 
        }
    };

    /* */
    private Predicate<TypeElement> validateTypeExtendsObject = new Predicate<TypeElement>()
    {
        public boolean apply(TypeElement type)
        {
            List<? extends TypeMirror> directSupertypes = types.directSupertypes(type.asType());
            if (directSupertypes.size() == 1)
            {
                TypeMirror typeMirror = directSupertypes.get(0);
                return "java.lang.Object".equals(typeMirror.toString());
            }

            return false;
        }

        public String toString()
        {
            return "Structure class must extend java.lang.Object directly."; 
        }
    };

    /* */
    private Predicate<TypeElement> validateTypePublic = new Predicate<TypeElement>()
    {
        public boolean apply(TypeElement type)
        {
            return type.getModifiers().contains(Modifier.PUBLIC);
        }

        public String toString()
        {
            return "Structure class must be public."; 
        }
    };

    /* */
    private Predicate<TypeElement> validateTypeFinal = new Predicate<TypeElement>()
    {
        public boolean apply(TypeElement type)
        {
            return type.getModifiers().contains(Modifier.FINAL);
        }

        public String toString()
        {
            return "Structure class must be final."; 
        }
    };

    /*
     * 
     */
    private void processType(TypeElement type) throws IOException
    {
        String fullTypeName = elements.getBinaryName(type).toString();
        String packageName = elements.getPackageOf(type).getQualifiedName().toString();

        PrintWriter w = null;
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try
        {
            final Struct struct = type.getAnnotation(Struct.class);

            final RuntimeInstance velocity = createInstance(messager);
            final VelocityContext context = createContext();
            context.put("packageName", packageName);
            context.put("fullTypeName", fullTypeName);
            context.put("sourceType", type);
            switch (struct.storage())
            {
                case PARALLEL_ARRAYS:
                    for (int dimension : struct.dimensions())
                    {
                        generateParallelArraysArray(type, dimension, velocity, context);
                    }
                    break;
                default:
                    throw new RuntimeException("Not supported by the generator: "
                        + struct.storage());
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(ccl);
            if (w != null) Closeables.closeQuietly(w);
        }
    }

    /**
     * Field helper class for template generator.
     */
    public class FieldHelper
    {
        private List<VariableElement> fields;

        public FieldHelper(TypeElement type)
        {
            fields = Lists.newArrayList();
            for (VariableElement field : ElementFilter.fieldsIn(type
                .getEnclosedElements()))
            {
                Set<Modifier> modifiers = field.getModifiers();
                if (modifiers.contains(Modifier.PUBLIC)
                    && !modifiers.contains(Modifier.FINAL)
                    && !modifiers.contains(Modifier.STATIC)
                    && !modifiers.contains(Modifier.TRANSIENT))
                {
                    fields.add(field);
                }
            }
        }

        public boolean isOfArrayType(VariableElement field)
        {
            return field.asType().getKind() == TypeKind.ARRAY;
        }

        public List<VariableElement> getFields()
        {
            return fields;
        }

        public String getterName(VariableElement field)
        {
            // Ignore Bean spec. and use getX instead of isX.
            return "get" + StringUtils.capitalize(field.getSimpleName().toString());
        }

        public String setterName(VariableElement field)
        {
            return "set" + StringUtils.capitalize(field.getSimpleName().toString());
        }

        public String pluralize(String fieldName)
        {
            return new org.jvnet.inflector.lang.en.NounPluralizer().pluralize(fieldName);
        }
    }

    /**
     * Generate a parallel-array array type with a given dimension.
     */
    private void generateParallelArraysArray(TypeElement type, int dimension,
        RuntimeInstance velocity, VelocityContext context) throws IOException
    {
        if (dimension < 1) throw new IOException("Invalid dimension: " + dimension);

        String packageName = elements.getPackageOf(type).getQualifiedName().toString();
        String outputTypeName = type.getSimpleName().toString() + "Array"
            + (dimension > 1 ? dimension + "D" : "");

        context.put("outputTypeName", outputTypeName);
        context.put("fieldHelper", new FieldHelper(type));
        context.put("dimensions", generateDimensions(dimension));

        PrintWriter w = new PrintWriter(filer.createSourceFile(
            packageName + "." + outputTypeName, type).openWriter());

        final Template template = velocity.getTemplate("StructArray.template", "UTF-8");
        template.merge(context, w);

        w.close();
    }

    /**
     * (1..dimension).to_s
     */
    private List<Integer> generateDimensions(int dimension)
    {
        ArrayList<Integer> newArrayList = Lists.newArrayList();
        for (int i = 1; i <= dimension; i++)
            newArrayList.add(i);
        return newArrayList;
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
        context.put("esc", StringEscapeUtils.class);
        context.put("stringutils", new StringUtils());
        return context;
    }
}
