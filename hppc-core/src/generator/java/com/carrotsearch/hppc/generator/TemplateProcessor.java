package com.carrotsearch.hppc.generator;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;

/**
 * Template processor for HPPC templates.
 */
public final class TemplateProcessor
{
    public boolean verbose = false;
    public boolean incremental = true;
    public File templatesDir;
    public File outputDir;

    private final RuntimeInstance velocity;

    /**
     * 
     */
    public TemplateProcessor()
    {
        final ExtendedProperties p = new ExtendedProperties();
        final RuntimeInstance velocity = new RuntimeInstance();
        p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "false");
        velocity.setConfiguration(p);
        this.velocity = velocity;
    }

    /**
     * 
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    /**
     * 
     */
    public void setIncremental(boolean incremental)
    {
        this.incremental = incremental;
    }
    
    /**
     * 
     */
    public void setDestDir(File dir)
    {
        this.outputDir = dir;
    }

    /**
     * 
     */
    public void setTemplatesDir(File dir)
    {
        this.templatesDir = dir;
    }

    /**
     * 
     */
    public void execute()
    {
        // Collect files/ checksums from the output folder.
        List<OutputFile> outputs = collectOutputFiles(new ArrayList<OutputFile>(),
            outputDir);

        // Collect template files in the input folder.
        List<TemplateFile> inputs = collectTemplateFiles(new ArrayList<TemplateFile>(),
            templatesDir);

        // Process templates
        System.out.println("Processing " + inputs.size() + " templates to: " + outputDir.getPath());
        long start = System.currentTimeMillis();
        processTemplates(inputs, outputs);
        long end = System.currentTimeMillis();
        System.out.println(String.format(Locale.ENGLISH, "Processed in %.2f sec.", (end - start) / 1000.0));

        // Remove non-marked files.
        int generated = 0;
        int updated = 0;
        int deleted = 0;
        for (OutputFile f : outputs)
        {
            if (!f.generated)
            {
                deleted++;
                if (verbose) System.out.println("Deleted: " + f.file);
                f.file.delete();
            }

            if (f.generated) generated++;

            if (f.updated)
            {
                updated++;
                if (verbose) System.out.println("Updated: "
                    + relativePath(f.file, this.outputDir));
            }
        }

        System.out.println("Generated " + generated + " files (" + updated + " updated, "
            + deleted + " deleted).");
    }

    /**
     * Apply templates to <code>.ktype</code> files (single-argument).
     */
    private void processTemplates(List<TemplateFile> inputs, List<OutputFile> outputs)
    {
        for (TemplateFile f : inputs)
        {
            String fileName = f.file.getName();
            if (!fileName.contains("VType") && fileName.contains("KType"))
            {
                for (Type t : Type.values())
                {
                    TemplateOptions options = new TemplateOptions(t);
                    options.sourceFile = f.file;
                    generate(f, outputs, options);
                }
            }
            if (fileName.contains("KTypeVType"))
            {
                for (Type ktype : Type.values())
                {
                    for (Type vtype : Type.values())
                    {
                        TemplateOptions options = new TemplateOptions(ktype, vtype);
                        options.sourceFile = f.file;
                        generate(f, outputs, options);
                    }
                }
            }
        }
    }

    /**
     * Apply templates.
     */
    private void generate(TemplateFile f, List<OutputFile> outputs,
        TemplateOptions templateOptions)
    {
        String targetFileName = targetFileName(relativePath(f.file, templatesDir),
            templateOptions);
        OutputFile output = findOrCreate(targetFileName, outputs);

        if (!incremental || !output.file.exists()
            || output.file.lastModified() <= f.file.lastModified())
        {
            String input = readFile(f.file);
            input = filterVelocity(f, input, templateOptions);
            input = filterIntrinsics(f, input, templateOptions);
            input = filterTypeClassRefs(f, input, templateOptions);
            input = filterComments(f, input, templateOptions);
            
            output.updated = true;
            saveFile(output.file, input);
        }
    }

    private String filterIntrinsics(TemplateFile f, String input,
        TemplateOptions templateOptions)
    {
        Pattern p = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?([a-zA-Z]+)",
            Pattern.MULTILINE | Pattern.DOTALL);

        StringBuffer sb = new StringBuffer();

        while (true)
        {
            Matcher m = p.matcher(input);
            if (m.find())
            {
                sb.append(input.substring(0, m.start()));

                String method = m.group(3);

                int bracketCount = 0;
                int last = m.end() + 1;
                ArrayList<String> params = new ArrayList<String>();
                outer: for (int i = m.end(); i < input.length(); i++)
                {
                    switch (input.charAt(i))
                    {
                        case '(':
                            bracketCount++;
                            break;
                        case ')':
                            bracketCount--;
                            if (bracketCount == 0)
                            {
                                params.add(input.substring(last, i).trim());
                                input = input.substring(i + 1);
                                break outer;
                            }
                            break;
                        case ',':
                            if (bracketCount == 1)
                            {
                                params.add(input.substring(last, i));
                                last = i + 1;
                            }
                            break;
                    }
                }

                if ("defaultKTypeValue".equals(method))
                {
                    sb.append(templateOptions.isKTypeGeneric() 
                        ? "null" 
                        : "((" + templateOptions.getKType().getType() + ") 0)");
                }
                else if ("defaultVTypeValue".equals(method))
                {
                    sb.append(templateOptions.isVTypeGeneric() 
                        ? "null" 
                        : "((" + templateOptions.getVType().getType() + ") 0)");
                }
                else if ("newKTypeArray".equals(method))
                {
                    sb.append(
                        templateOptions.isKTypeGeneric() 
                        ? "Internals.<KType[]>newArray(" + params.get(0) + ")"
                        : "new " + templateOptions.getKType().getType() + " [" + params.get(0) + "]");
                }
                else if ("newVTypeArray".equals(method))
                {
                    sb.append(
                        templateOptions.isVTypeGeneric() 
                        ? "Internals.<VType[]>newArray(" + params.get(0) + ")"
                        : "new " + templateOptions.getVType().getType() + " [" + params.get(0) + "]");
                }
                else if ("equalsKType".equals(method))
                {
                    if (templateOptions.isKTypeGeneric())
                    {
                        sb.append(
                            String.format("((%1$s) == null ? (%2$s) == null : (%1$s).equals((%2$s)))",
                                params.toArray()));
                    }
                    else
                    {
                        sb.append(
                            String.format("((%1$s) == (%2$s))",
                                params.toArray()));
                    }
                }
                else if ("equalsVType".equals(method))
                {
                    if (templateOptions.isVTypeGeneric())
                    {
                        sb.append(
                            String.format("((%1$s) == null ? (%2$s) == null : (%1$s).equals((%2$s)))",
                                params.toArray()));
                    }
                    else
                    {
                        sb.append(
                            String.format("((%1$s) == (%2$s))",
                                params.toArray()));
                    }
                }
                else
                {
                    throw new RuntimeException("Unrecognized Intrinsic call: " + method);
                }
            }
            else
            {
                sb.append(input);
                break;
            }
        }

        return sb.toString();
    }

    private String filterComments(TemplateFile f, String input,
        TemplateOptions templateOptions)
    {
        Pattern p = Pattern.compile("(/\\*!\\s*)|(\\s*!\\*/)", Pattern.MULTILINE
            | Pattern.DOTALL);
        return p.matcher(input).replaceAll("");
    }

    private String filterTypeClassRefs(TemplateFile f, String input, TemplateOptions options)
    {
        input = rewriteSignatures(f, input, options);
        input = rewriteLiterals(f, input, options);
        return input;
    }

    private String rewriteSignatures(TemplateFile f, String input, TemplateOptions options)
    {
        Pattern p = Pattern.compile("<[\\?A-Z]");
        Matcher m = p.matcher(input);
        
        StringBuilder sb = new StringBuilder();
        int fromIndex = 0;
        while (m.find(fromIndex))
        {
            int next = m.start();
            int end = next + 1;
            int bracketCount = 1;
            while (bracketCount > 0 && end < input.length())
            {
                switch (input.charAt(end++)) {
                    case '<': bracketCount++; break;
                    case '>': bracketCount--; break;
                }
            }
            sb.append(input.substring(fromIndex, next));
            sb.append(rewriteSignature(input.substring(next, end), options));
            fromIndex = end;
        }
        sb.append(input.substring(fromIndex, input.length()));
        return sb.toString();
    }

    private String rewriteSignature(String signature, TemplateOptions options)
    {
        if (!signature.contains("KType") && !signature.contains("VType"))
            return signature;

        Pattern p = Pattern.compile("<[^<>]*>", Pattern.MULTILINE | Pattern.DOTALL);

        StringBuilder sb = new StringBuilder(signature);
        Matcher m = p.matcher(sb);
        while (m.find())
        {
            String group = m.group();
            group = group.substring(1, group.length() - 1);
            List<String> args = new ArrayList<String>(Arrays.asList(group.split(",")));
            StringBuilder b = new StringBuilder();
            for (Iterator<String> i = args.iterator(); i.hasNext();)
            {
                String arg = i.next().trim();

                if (options.isKTypePrimitive())
                {
                    if (isGenericOnly(arg, "KType"))
                        arg = "";
                    else
                        arg = arg.replace("KType", options.getKType().getBoxedType());
                }

                if (options.hasVType() && options.isVTypePrimitive())
                {
                    if (isGenericOnly(arg, "VType"))
                        arg = "";
                    else
                        arg = arg.replace("VType", options.getVType().getBoxedType());
                }

                if (arg.length() > 0)
                {
                    if (b.length() > 0) b.append(", ");
                    b.append(arg.trim());
                }
            }
            
            if (b.length() > 0)
            {
                b.insert(0, '{');
                b.append('}');
            }
            
            sb.replace(m.start(), m.end(), b.toString());
            m = p.matcher(sb);
        }
        return sb.toString().replace('{', '<').replace('}', '>');
    }

    private boolean isGenericOnly(String arg, String type)
    {
        return arg.equals(type) || arg.equals("? super " + type) || arg.equals("? extends " + type);
    }

    private String rewriteLiterals(TemplateFile f, String input, TemplateOptions options)
    {
        Type k = options.getKType();

        if (options.hasVType())
        {
            Type v = options.getVType();
            
            input = input.replaceAll("(KTypeVType)([A-Z][a-zA-Z]*)(<.+?>)?",
                (k.isGeneric() ? "Object" : k.getBoxedType()) +
                (v.isGeneric() ? "Object" : v.getBoxedType()) +
                "$2" +
                (options.isAnyGeneric() ? "$3" : ""));
            
            input = input.replaceAll("(VType)([A-Z][a-zA-Z]*)",
                (v.isGeneric() ? "Object" : v.getBoxedType()) +  "$2");

            if (!v.isGeneric())
                input = input.replaceAll("VType", v.getType());
        }
        
        input = input.replaceAll("(KType)([A-Z][a-zA-Z]*)(<.+?>)?",
            k.isGeneric() ? "Object" + "$2$3": k.getBoxedType() + "$2");

        if (!k.isGeneric())
            input = input.replaceAll("KType", k.getType());

        return input;
    }

    private void saveFile(File file, String input)
    {
        try
        {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(input.getBytes("UTF-8"));
            fos.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apply velocity to the input.
     */
    private String filterVelocity(TemplateFile f, String template, TemplateOptions options)
    {
        final VelocityContext ctx = new VelocityContext();
        ctx.put("TemplateOptions", options);

        StringWriter sw = new StringWriter();
        velocity.evaluate(ctx, sw, f.file.getName(), template);
        return sw.toString();
    }

    /**
     * 
     */
    private String readFile(File file)
    {
        try
        {
            byte [] contents = new byte [(int) file.length()];
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(
                file));
            dataInputStream.readFully(contents);
            dataInputStream.close();
            return new String(contents, "UTF-8");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private OutputFile findOrCreate(String targetFileName, List<OutputFile> outputs)
    {
        File candidate = canonicalFile(new File(this.outputDir, targetFileName));
        for (OutputFile o : outputs)
        {
            if (o.file.equals(candidate))
            {
                o.generated = true;
                return o;
            }
        }

        OutputFile o = new OutputFile(candidate, true);
        outputs.add(o);
        return o;
    }

    private String targetFileName(String relativePath, TemplateOptions templateOptions)
    {
        if (templateOptions.hasVType()) relativePath = relativePath.replace("KTypeVType",
            templateOptions.getKType().getBoxedType()
                + templateOptions.getVType().getBoxedType());

        relativePath = relativePath.replace("KType", templateOptions.getKType()
            .getBoxedType());
        return relativePath;
    }

    /**
     * Relative path name.
     */
    private String relativePath(File sub, File parent)
    {
        try
        {
            return sub.getCanonicalPath().toString()
                .substring(parent.getCanonicalPath().length());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect files present in the output.
     */
    private List<OutputFile> collectOutputFiles(final List<OutputFile> list, File dir)
    {
        if (!dir.exists()) return list;

        for (File file : dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                File f = new File(dir, name);
                if (f.isDirectory())
                {
                    collectOutputFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        }))
        {
            list.add(new OutputFile(file, false));
        }
        return list;
    }

    /**
     * Collect all template files from this and subdirectories.
     */
    private List<TemplateFile> collectTemplateFiles(final List<TemplateFile> list,
        File dir)
    {
        for (File file : dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                File f = new File(dir, name);
                if (f.isDirectory())
                {
                    collectTemplateFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        }))
        {
            list.add(new TemplateFile(file));
        }
        return list;
    }

    static File canonicalFile(File target)
    {
        try
        {
            return target.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args)
    {
        final TemplateProcessor processor = new TemplateProcessor();
        processor.setTemplatesDir(new File(args[0]));
        processor.setDestDir(new File(args[1]));
        processor.execute();
    }
}