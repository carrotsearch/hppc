/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.generator;

import com.carrotsearch.console.jcommander.Parameter;
import com.carrotsearch.console.launcher.Command;
import com.carrotsearch.console.launcher.ExitCode;
import com.carrotsearch.console.launcher.ExitCodes;
import com.carrotsearch.console.launcher.Launcher;
import com.carrotsearch.console.launcher.Loggers;
import com.carrotsearch.hppc.generator.intrinsics.Add;
import com.carrotsearch.hppc.generator.intrinsics.Cast;
import com.carrotsearch.hppc.generator.intrinsics.Empty;
import com.carrotsearch.hppc.generator.intrinsics.Equals;
import com.carrotsearch.hppc.generator.intrinsics.IsEmpty;
import com.carrotsearch.hppc.generator.intrinsics.NewArray;
import com.carrotsearch.hppc.generator.parser.SignatureProcessor;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.util.ExtProperties;
import org.slf4j.Logger;

/** Template processor. */
public class TemplateProcessor extends Command<ExitCode> {
  private final HashMap<String, IntrinsicMethod> intrinsics;

  {
    intrinsics = new HashMap<>();
    intrinsics.put("empty", new Empty());
    intrinsics.put("isEmpty", new IsEmpty());
    intrinsics.put("newArray", new NewArray());
    intrinsics.put("cast", new Cast());
    intrinsics.put("add", new Add());
    intrinsics.put("equals", new Equals());
  }

  @Parameter(names = {"--incremental"})
  public boolean incremental;

  @Parameter(
      names = {"--templates"},
      required = true)
  public Path templatesDir;

  @Parameter(
      names = {"--output"},
      required = true)
  public Path outputDir;

  private RuntimeInstance velocity;
  private Path templatesPath;
  private Path outputPath;
  private Logger logger;

  public static void main(String[] args) {
    ExitCode exitCode = new Launcher().runCommand(new TemplateProcessor(), args);
    System.exit(exitCode.processReturnValue());
  }

  @Override
  protected ExitCode run() {
    try {
      logger = Loggers.CONSOLE;
      return execute();
    } catch (IOException e) {
      logger.error("I/O exception occurred.", e);
      return ExitCodes.ERROR_INTERNAL;
    }
  }

  private ExitCode execute() throws IOException {
    velocity = new RuntimeInstance();
    final ExtProperties p = new ExtProperties();
    velocity.setConfiguration(p);

    this.templatesPath = templatesDir.toAbsolutePath().normalize();
    this.outputPath = outputDir.toAbsolutePath().normalize();

    logger.debug(
        String.format(
            Locale.ROOT, "Processing templates from %s => %s", templatesPath, outputPath));

    final Instant startTime = Instant.now();
    final List<TemplateFile> templates = collectTemplateFiles(templatesPath);
    final List<OutputFile> generated = processTemplates(templates);
    final List<Path> removed = removeOtherFiles(outputPath, generated);

    int updated = generated.size();
    for (OutputFile o : generated) {
      if (o.upToDate) updated--;
    }

    logger.info(
        String.format(
            Locale.ROOT,
            "Processed %d templates in %.2f sec. (%d output files: %d updated, %d deleted).",
            templates.size(),
            Duration.between(startTime, Instant.now()).toMillis() / 1000.0f,
            generated.size(),
            updated,
            removed.size()));

    return ExitCodes.SUCCESS;
  }

  private List<Path> removeOtherFiles(Path outputPath, List<OutputFile> keep) throws IOException {
    final Set<String> keepPaths = new HashSet<>();
    for (OutputFile o : keep) {
      keepPaths.add(o.path.toRealPath().toString());
    }

    final List<Path> toRemove = new ArrayList<>();
    Files.walkFileTree(
        outputPath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
              throws IOException {
            path = path.toRealPath();
            if (!keepPaths.contains(path.toString())) {
              toRemove.add(path);
            }
            return FileVisitResult.CONTINUE;
          }
        });

    for (Path p : toRemove) {
      logger.debug("Deleting: " + p.toString());
      Files.delete(p);
    }

    return toRemove;
  }

  /** Apply templates to <code>.ktype</code> files (single-argument). */
  private List<OutputFile> processTemplates(List<TemplateFile> inputs) throws IOException {
    List<OutputFile> outputs = new ArrayList<>();
    for (TemplateFile f : inputs) {
      String fileName = f.getFileName();
      if (!fileName.contains("VType") && fileName.contains("KType")) {
        for (Type ktype : Type.values()) {
          TemplateOptions options = new TemplateOptions(ktype);
          options.templateFile = f.path;
          generate(f, outputs, options);
        }
      }
      if (fileName.contains("KTypeVType")) {
        for (Type ktype : Type.values()) {
          for (Type vtype : Type.values()) {
            TemplateOptions options = new TemplateOptions(ktype, vtype);
            options.templateFile = f.path;
            generate(f, outputs, options);
          }
        }
      }
    }
    return outputs;
  }

  /** Apply templates. */
  private void generate(
      TemplateFile input, List<OutputFile> outputs, TemplateOptions templateOptions)
      throws IOException {
    final String targetFileName =
        targetFileName(templatesPath.relativize(input.path).toString(), templateOptions);
    final OutputFile output =
        new OutputFile(outputPath.resolve(targetFileName).toAbsolutePath().normalize());

    if (incremental
        && Files.exists(output.path)
        && Files.getLastModifiedTime(output.path).toMillis()
            >= Files.getLastModifiedTime(input.path).toMillis()) {
      // No need to re-render but mark as generated.
      output.upToDate = true;
      outputs.add(output);
      return;
    }

    String template = new String(Files.readAllBytes(input.path), StandardCharsets.UTF_8);

    template = filterVelocity(input, template, templateOptions);

    // Check if template requested ignoring a given type combination.
    if (templateOptions.isIgnored()) {
      return;
    }

    logger.debug("Processing: " + input.getFileName() + " => " + output.path);
    try {
      template = filterIntrinsics(template, templateOptions);
      template = filterComments(template);
      template = filterTypeClassRefs(template, templateOptions);
      template = filterStaticTokens(template, templateOptions);
    } catch (RuntimeException e) {
      logger.error(
          "Error processing: "
              + input.getFileName()
              + " => "
              + output.path
              + ":\n\t"
              + e.getMessage());
      throw e;
    }

    Files.createDirectories(output.path.getParent());
    Files.write(output.path, template.getBytes(StandardCharsets.UTF_8));

    outputs.add(output);
  }

  private String filterStaticTokens(String template, TemplateOptions templateOptions) {
    return template.replace(TemplateOptions.TEMPLATE_FILE_TOKEN, templateOptions.getTemplateFile());
  }

  private String filterIntrinsics(String input, TemplateOptions templateOptions) {
    // TODO: this should be eventually moved to AST processor.
    Pattern p =
        Pattern.compile(
            "(Intrinsics.\\s*)" + "(<(?<generic>[^>]+)>\\s*)?" + "(?<method>[a-zA-Z0-9]+)",
            Pattern.MULTILINE | Pattern.DOTALL);

    StringBuilder sb = new StringBuilder();
    while (true) {
      Matcher m = p.matcher(input);
      if (m.find()) {
        sb.append(input.substring(0, m.start()));

        String method = m.group("method");

        int bracketCount = 0;
        int last = m.end() + 1;
        ArrayList<String> params = new ArrayList<>();
        outer:
        for (int i = m.end(); i < input.length(); i++) {
          switch (input.charAt(i)) {
            case '(':
              bracketCount++;
              break;
            case ')':
              bracketCount--;
              if (bracketCount == 0) {
                if (last != i) {
                  params.add(input.substring(last, i).trim());
                }
                input = input.substring(i + 1);
                break outer;
              }
              break;
            case ',':
              if (bracketCount == 1) {
                params.add(input.substring(last, i));
                last = i + 1;
              }
              break;
          }
        }

        logger.debug(
            "Intrinsic call: "
                + m.group()
                + "; method: "
                + method
                + ", generic: "
                + m.group("generic")
                + ", args: "
                + params);

        // Allow recursion of intrinsics into arguments.
        for (int i = 0; i < params.size(); i++) {
          params.set(i, filterIntrinsics(params.get(i), templateOptions));
        }

        IntrinsicMethod im = intrinsics.get(method);
        if (im == null) {
          throw new RuntimeException(
              String.format(
                  Locale.ROOT, "Unknown intrinsic method '%s' in call: %s", method, m.group()));
        } else {
          im.invoke(m, sb, templateOptions, m.group("generic"), params);
        }
      } else {
        sb.append(input);
        break;
      }
    }

    return sb.toString();
  }

  private String filterComments(String input) {
    Pattern p = Pattern.compile("(/\\*!)|(!\\*/)", Pattern.MULTILINE | Pattern.DOTALL);
    return p.matcher(input).replaceAll("");
  }

  private String filterTypeClassRefs(String input, TemplateOptions options) {
    try {
      SignatureProcessor signatureProcessor = new SignatureProcessor(input);
      return signatureProcessor.process(options);
    } catch (Exception e) {
      logger.error("Signature processor failure: " + options.getTemplateFile(), e);
      throw new RuntimeException(e);
    }
  }

  /** Apply velocity to the input. */
  private String filterVelocity(TemplateFile f, String template, TemplateOptions options) {
    final VelocityContext ctx = new VelocityContext();
    ctx.put("TemplateOptions", options);
    ctx.put("true", true);
    ctx.put("templateOnly", false);
    ctx.put("false", false);

    StringWriter sw = new StringWriter();
    velocity.evaluate(ctx, sw, f.getFileName(), template);
    return sw.toString();
  }

  private String targetFileName(String relativePath, TemplateOptions templateOptions) {
    if (templateOptions.hasVType()) {
      relativePath =
          relativePath.replace(
              "KTypeVType",
              templateOptions.getKType().getBoxedType()
                  + templateOptions.getVType().getBoxedType());
    }

    return relativePath.replace("KType", templateOptions.getKType().getBoxedType());
  }

  private List<Path> scanFilesMatching(Path dir, String matchPattern) throws IOException {
    final List<Path> paths = new ArrayList<>();
    if (Files.isDirectory(dir)) {
      final PathMatcher matcher = dir.getFileSystem().getPathMatcher(matchPattern);
      Files.walkFileTree(
          dir,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                throws IOException {
              if (matcher.matches(path)) {
                paths.add(path);
              }
              return FileVisitResult.CONTINUE;
            }
          });
    }
    return paths;
  }

  /** Collect all template files from this and subdirectories. */
  private List<TemplateFile> collectTemplateFiles(Path dir) throws IOException {
    final List<TemplateFile> paths = new ArrayList<>();
    for (Path path : scanFilesMatching(dir, "glob:**.java")) {
      paths.add(new TemplateFile(path));
    }
    return paths;
  }
}
