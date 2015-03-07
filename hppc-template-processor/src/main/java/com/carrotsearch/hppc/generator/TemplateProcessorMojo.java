package com.carrotsearch.hppc.generator;

import com.google.common.base.Stopwatch;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maven mojo applying preprocessor templates.
 */
@Mojo(name = "template-processor",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    threadSafe = true,
    requiresProject = true)
public class TemplateProcessorMojo extends AbstractMojo {
  @Parameter(property = "project",
      readonly = true,
      required = true)
  private MavenProject project;

  @Parameter(defaultValue = "false")
  public boolean verbose;

  @Parameter(defaultValue = "true")
  public boolean incremental;

  @Parameter(required = true)
  public String attachSources;

  @Parameter(required = true)
  public File templatesDir;

  @Parameter(required = true)
  public File outputDir;

  private RuntimeInstance velocity;
  private Path templatesPath;
  private Path outputPath;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      execute0();
    } catch (IOException e) {
      throw new MojoExecutionException("Couldn't process templates.", e);
    }
  }

  private void execute0() throws IOException, MojoExecutionException {
    velocity = new RuntimeInstance();
    final ExtendedProperties p = new ExtendedProperties();
    p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
    p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "false");
    velocity.setConfiguration(p);

    this.templatesPath = templatesDir.toPath().toAbsolutePath().normalize();
    this.outputPath = outputDir.toPath().toAbsolutePath().normalize();

    Path basedir = project.getBasedir().toPath().toAbsolutePath().normalize();
    getLog().info(String.format(Locale.ROOT,
        "2 Processing templates from %s => %s",
        basedir.relativize(templatesPath),
        basedir.relativize(outputPath)));

    final Stopwatch sw = Stopwatch.createStarted();
    final List<TemplateFile> templates = collectTemplateFiles(templatesPath);
    final List<OutputFile> generated = processTemplates(templates);
    final List<Path> removed = removeOtherFiles(outputPath, generated);

    int updated = generated.size();
    for (OutputFile o : generated) {
      if (o.upToDate) updated--;
    }

    getLog().info(String.format(Locale.ROOT,
        "Processed %d templates in %.2f sec. (%d output files: %d updated, %d deleted).",
        templates.size(),
        sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0f,
        generated.size(),
        updated,
        removed.size()));

    switch (attachSources.toLowerCase(Locale.ROOT)) {
      case "main":
        project.addCompileSourceRoot(outputPath.toString());
        break;
      case "test":
        project.addTestCompileSourceRoot(outputPath.toString());
        break;
      default:
        throw new MojoExecutionException("Invalid source attachment option ('source' or 'test' allowed): " + attachSources);
    }
  }

  private List<Path> removeOtherFiles(Path outputPath, List<OutputFile> keep) throws IOException {
    final Set<String> keepPaths = new HashSet<>();
    for (OutputFile o : keep) {
      keepPaths.add(o.path.toRealPath().toString());
    }

    final List<Path> toRemove = new ArrayList<>();
    Files.walkFileTree(outputPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        path = path.toRealPath();
        if (!keepPaths.contains(path.toString())) {
          toRemove.add(path);
        }
        return FileVisitResult.CONTINUE;
      }
    });

    for (Path p : toRemove) {
      verboseLog("Deleting: " + p.toString());
      Files.delete(p);
    }

    return toRemove;
  }

  private void verboseLog(String message) {
    if (verbose) {
      getLog().info(message);
    } else {
      getLog().debug(message);
    }
  }

  /**
   * Apply templates to <code>.ktype</code> files (single-argument).
   */
  private List<OutputFile> processTemplates(List<TemplateFile> inputs) throws IOException {
    List<OutputFile> outputs = new ArrayList<>();
    for (TemplateFile f : inputs) {
      String fileName = f.getFileName();
      if (!fileName.contains("VType") && fileName.contains("KType")) {
        for (Type ktype : Type.values()) {
          TemplateOptions options = new TemplateOptions(ktype);
          options.sourceFile = f.path;
          generate(f, outputs, options);
        }
      }
      if (fileName.contains("KTypeVType")) {
        for (Type ktype : Type.values()) {
          for (Type vtype : Type.values()) {
            TemplateOptions options = new TemplateOptions(ktype, vtype);
            options.sourceFile = f.path;
            generate(f, outputs, options);
          }
        }
      }
    }
    return outputs;
  }

  private Stopwatch timeVelocity = Stopwatch.createUnstarted();
  private Stopwatch timeIntrinsics = Stopwatch.createUnstarted();
  private Stopwatch timeTypeClassRefs = Stopwatch.createUnstarted();
  private Stopwatch timeComments = Stopwatch.createUnstarted();

  /**
   * Apply templates.
   */
  private void generate(TemplateFile input, List<OutputFile> outputs, TemplateOptions templateOptions) throws IOException {
    final String targetFileName = targetFileName(templatesPath.relativize(input.path).toString(), templateOptions);
    final OutputFile output = new OutputFile(outputPath.resolve(targetFileName).toAbsolutePath().normalize());

    if (incremental &&
        Files.exists(output.path) &&
        Files.getLastModifiedTime(output.path).toMillis() >= Files.getLastModifiedTime(input.path).toMillis()) {
      // No need to re-render but mark as generated.
      output.upToDate = true;
      outputs.add(output);
      return;
    }

    String template = new String(Files.readAllBytes(input.path), StandardCharsets.UTF_8);

    timeVelocity.start();
    template = filterVelocity(input, template, templateOptions);
    timeVelocity.stop();

    // Check if template requested ignoring a given type combination.
    if (templateOptions.isIgnored()) {
      return;
    }

    timeIntrinsics.start();
    template = filterIntrinsics(template, templateOptions);
    timeIntrinsics.stop();

    timeTypeClassRefs.start();
    template = filterTypeClassRefs(template, templateOptions);
    timeTypeClassRefs.stop();

    timeComments.start();
    template = filterComments(template);
    timeComments.stop();

    Files.createDirectories(output.path.getParent());
    Files.write(output.path, template.getBytes(StandardCharsets.UTF_8));

    outputs.add(output);
  }

  private String filterIntrinsics(String input, TemplateOptions templateOptions) {
    Pattern p = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?([a-zA-Z]+)", Pattern.MULTILINE | Pattern.DOTALL);

    StringBuilder sb = new StringBuilder();
    while (true) {
      Matcher m = p.matcher(input);
      if (m.find()) {
        sb.append(input.substring(0, m.start()));

        String method = m.group(3);

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
                params.add(input.substring(last, i).trim());
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

        if ("defaultKTypeValue".equals(method)) {
          sb.append(templateOptions.isKTypeGeneric()
              ? "null"
              : "((" + templateOptions.getKType().getType() + ") 0)");
        } else if ("defaultVTypeValue".equals(method)) {
          sb.append(templateOptions.isVTypeGeneric()
              ? "null"
              : "((" + templateOptions.getVType().getType() + ") 0)");
        } else if ("newKTypeArray".equals(method)) {
          sb.append(
              templateOptions.isKTypeGeneric()
                  ? "Internals.<KType[]>newArray(" + params.get(0) + ")"
                  : "new " + templateOptions.getKType().getType() + " [" + params.get(0) + "]");
        } else if ("newVTypeArray".equals(method)) {
          sb.append(
              templateOptions.isVTypeGeneric()
                  ? "Internals.<VType[]>newArray(" + params.get(0) + ")"
                  : "new " + templateOptions.getVType().getType() + " [" + params.get(0) + "]");
        } else if ("equalsKType".equals(method)) {
          if (templateOptions.isKTypeGeneric()) {
            sb.append(
                String.format("((%1$s) == null ? (%2$s) == null : (%1$s).equals((%2$s)))",
                    params.toArray()));
          } else if (templateOptions.ktype == Type.DOUBLE) {
            sb.append(
                String.format("(Double.doubleToLongBits(%1$s) == Double.doubleToLongBits(%2$s))",
                    params.toArray()));
          } else if (templateOptions.ktype == Type.FLOAT) {
            sb.append(
                String.format("(Float.floatToIntBits(%1$s) == Float.floatToIntBits(%2$s))",
                    params.toArray()));
          } else {
            sb.append(
                String.format("((%1$s) == (%2$s))",
                    params.toArray()));
          }
        } else if ("equalsVType".equals(method)) {
          if (templateOptions.isVTypeGeneric()) {
            sb.append(
                String.format("((%1$s) == null ? (%2$s) == null : (%1$s).equals((%2$s)))",
                    params.toArray()));
          } else if (templateOptions.vtype == Type.DOUBLE) {
            sb.append(
                String.format("(Double.doubleToLongBits(%1$s) == Double.doubleToLongBits(%2$s))",
                    params.toArray()));
          } else if (templateOptions.vtype == Type.FLOAT) {
            sb.append(
                String.format("(Float.floatToIntBits(%1$s) == Float.floatToIntBits(%2$s))",
                    params.toArray()));
          } else {
            sb.append(
                String.format("((%1$s) == (%2$s))",
                    params.toArray()));
          }
        } else {
          throw new RuntimeException("Unrecognized Intrinsic call: " + method);
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
    input = unifyTypeWithSignature(input);
    input = rewriteSignatures(input, options);
    input = rewriteLiterals(input, options);
    return input;
  }

  private String unifyTypeWithSignature(String input) {
    // This is a hack. A better way would be a full source AST and
    // rewrite at the actual typeDecl level.
    // KTypePredicate<? super VType> => VTypePredicate<? super VType>
    return input.replaceAll("(KType)(?!VType)([A-Za-z]+)(<(?:(\\? super ))?VType>)", "VType$2$3");
  }

  private String rewriteSignatures(String input, TemplateOptions options) {
    Pattern p = Pattern.compile("<[\\?A-Z]");
    Matcher m = p.matcher(input);

    StringBuilder sb = new StringBuilder();
    int fromIndex = 0;
    while (m.find(fromIndex)) {
      int next = m.start();
      int end = next + 1;
      int bracketCount = 1;
      while (bracketCount > 0 && end < input.length()) {
        switch (input.charAt(end++)) {
          case '<':
            bracketCount++;
            break;
          case '>':
            bracketCount--;
            break;
        }
      }
      sb.append(input.substring(fromIndex, next));
      sb.append(rewriteSignature(input.substring(next, end), options));
      fromIndex = end;
    }
    sb.append(input.substring(fromIndex, input.length()));
    return sb.toString();
  }

  private String rewriteSignature(String signature, TemplateOptions options) {
    if (!signature.contains("KType") && !signature.contains("VType"))
      return signature;

    Pattern p = Pattern.compile("<[^<>]*>", Pattern.MULTILINE | Pattern.DOTALL);

    StringBuilder sb = new StringBuilder(signature);
    Matcher m = p.matcher(sb);
    while (m.find()) {
      String group = m.group();
      group = group.substring(1, group.length() - 1);
      List<String> args = new ArrayList<>(Arrays.asList(group.split(",")));
      StringBuilder b = new StringBuilder();
      for (String arg : args) {
        arg = arg.trim();

        if (options.isKTypePrimitive()) {
          if (isGenericOnly(arg, "KType"))
            arg = "";
          else
            arg = arg.replace("KType", options.getKType().getBoxedType());
        }

        if (options.hasVType() && options.isVTypePrimitive()) {
          if (isGenericOnly(arg, "VType"))
            arg = "";
          else
            arg = arg.replace("VType", options.getVType().getBoxedType());
        }

        if (arg.length() > 0) {
          if (b.length() > 0) b.append(", ");
          b.append(arg.trim());
        }
      }

      if (b.length() > 0) {
        b.insert(0, '{');
        b.append('}');
      }

      sb.replace(m.start(), m.end(), b.toString());
      m = p.matcher(sb);
    }
    return sb.toString().replace('{', '<').replace('}', '>');
  }

  private boolean isGenericOnly(String arg, String type) {
    return arg.equals(type) || arg.equals("? super " + type) || arg.equals("? extends " + type);
  }

  private String rewriteLiterals(String input, TemplateOptions options) {
    Type k = options.getKType();

    if (options.hasVType()) {
      Type v = options.getVType();

      input = input.replaceAll("(KTypeVType)([A-Z][a-zA-Z]*)(<.+?>)?",
          (k.isGeneric() ? "Object" : k.getBoxedType()) +
              (v.isGeneric() ? "Object" : v.getBoxedType()) +
              "$2" +
              (options.isAnyGeneric() ? "$3" : ""));

      input = input.replaceAll("(VType)([A-Z][a-zA-Z]*)",
          (v.isGeneric() ? "Object" : v.getBoxedType()) + "$2");

      if (!v.isGeneric())
        input = input.replaceAll("VType", v.getType());
    }

    input = input.replaceAll("(KType)([A-Z][a-zA-Z]*)(<.+?>)?",
        k.isGeneric() ? "Object" + "$2$3" : k.getBoxedType() + "$2");

    if (!k.isGeneric())
      input = input.replaceAll("KType", k.getType());

    return input;
  }

  /**
   * Apply velocity to the input.
   */
  private String filterVelocity(TemplateFile f, String template, TemplateOptions options) {
    final VelocityContext ctx = new VelocityContext();
    ctx.put("TemplateOptions", options);

    StringWriter sw = new StringWriter();
    velocity.evaluate(ctx, sw, f.getFileName(), template);
    return sw.toString();
  }

  private String targetFileName(String relativePath, TemplateOptions templateOptions) {
    if (templateOptions.hasVType()) {
      relativePath = relativePath.replace("KTypeVType",
          templateOptions.getKType().getBoxedType() + templateOptions.getVType().getBoxedType());
    }

    return relativePath.replace("KType", templateOptions.getKType().getBoxedType());
  }

  private List<Path> scanFilesMatching(Path dir, String matchPattern) throws IOException {
    final List<Path> paths = new ArrayList<>();
    if (Files.isDirectory(dir)) {
      final PathMatcher matcher = dir.getFileSystem().getPathMatcher(matchPattern);
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
          if (matcher.matches(path)) {
            paths.add(path);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
    return paths;
  }

  /**
   * Collect all template files from this and subdirectories.
   */
  private List<TemplateFile> collectTemplateFiles(Path dir) throws IOException {
    final List<TemplateFile> paths = new ArrayList<>();
    for (Path path : scanFilesMatching(dir, "glob:**.java")) {
      paths.add(new TemplateFile(path));
    }
    return paths;
  }
}
