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
    public boolean incremental = false;
    public File templatesDir;
    public File outputDir;

    private final RuntimeInstance velocity;

    public TemplateProcessor()
    {
        final ExtendedProperties p = new ExtendedProperties();
        final RuntimeInstance velocity = new RuntimeInstance();
        p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "false");
        velocity.setConfiguration(p);
        this.velocity = velocity;
    }

    public void execute()
    {
        // Collect files/ checksums from the output folder.
        List<OutputFile> outputs = collectOutputFiles(new ArrayList<OutputFile>(),
            outputDir);

        // Collect template files in the input folder.
        List<TemplateFile> inputs = collectTemplateFiles(new ArrayList<TemplateFile>(),
            templatesDir);

        // Apply KType templates
        applyKType(inputs, outputs);

        // Apply KTypeVType templates

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
    private void applyKType(List<TemplateFile> inputs, List<OutputFile> outputs)
    {
        for (TemplateFile f : inputs)
        {
            if (!f.file.getName().contains("VType"))
            {
                for (Type t : Type.values())
                {
                    generate(f, outputs, new TemplateOptions(t));
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

        String input = readFile(f.file);
        input = filterVelocity(f, input, templateOptions);
        input = filterIntrinsics(f, input, templateOptions);
        input = filterKType(f, input, templateOptions);
        input = filterComments(f, input, templateOptions);

        if (!incremental || !output.file.exists()
            || output.file.lastModified() <= f.file.lastModified())
        {
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
                                params.add(input.substring(last, i));
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
                    sb.append(templateOptions.isKTypeGeneric() ? "null" : "0");
                }
                else if ("newKTypeArray".equals(method))
                {
                    sb.append(
                        templateOptions.isKTypeGeneric() 
                        ? "new Object [" + params.get(0) + "]"
                        : "new " + templateOptions.getKType().getType() + " [" + params.get(0) + "]");
                }
                else if ("equals".equals(method))
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

    private String filterKType(TemplateFile f, String input,
        TemplateOptions templateOptions)
    {
        Pattern p = Pattern.compile("(KType)([A-Z][A-Za-z]*)?(<[^/][^>]+>)?",
            Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(input);
        m.reset();

        boolean result = m.find();
        if (result)
        {
            StringBuffer sb = new StringBuffer();
            do
            {
                String replacement;
                if (m.group(2) == null)
                {
                    if (templateOptions.isKTypeGeneric()) replacement = "KType";
                    else replacement = templateOptions.getKType().getType();
                }
                else
                {
                    if (templateOptions.isKTypeGeneric())
                    {
                        replacement = templateOptions.getKType().getBoxedType()
                            + strOrEmpty(m.group(2)) + strOrEmpty(m.group(3));
                    }
                    else
                    {
                        replacement = templateOptions.getKType().getBoxedType()
                            + strOrEmpty(m.group(2));
                    }
                }
                m.appendReplacement(sb, replacement);
                result = m.find();
            }
            while (result);
            m.appendTail(sb);
            return sb.toString();
        }
        return input;
    }

    private String strOrEmpty(String str)
    {
        if (str != null) return str;
        else return "";
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
        processor.templatesDir = new File(args[0]);
        processor.outputDir = new File(args[1]);
        processor.execute();
    }
}