package com.carrotsearch.hppc.generator;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.carrotsearch.hppc.generator.intrinsics.Add;
import com.carrotsearch.hppc.generator.intrinsics.Cast;
import com.carrotsearch.hppc.generator.intrinsics.Empty;
import com.carrotsearch.hppc.generator.intrinsics.Equals;
import com.carrotsearch.hppc.generator.intrinsics.IsEmpty;
import com.carrotsearch.hppc.generator.intrinsics.NewArray;
import com.carrotsearch.hppc.generator.parser.SignatureProcessor;
import com.google.common.base.Stopwatch;

/**
 * Maven mojo applying preprocessor templates.
 */
@Mojo(name = "template-processor",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    threadSafe = true,
    requiresProject = true)
public class TemplateProcessorMojo extends AbstractMojo {
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

  @Parameter(property = "project",
      readonly = true,
      required = true)
  private MavenProject project;

  @Parameter(defaultValue = "false")
  public boolean verbose;

  @Parameter(property = "template.processor.incremental", defaultValue = "true")
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
    
    // Cater for Eclipse's insanity -- we can't just specify a separate launch config that'd run
    // after a manual 'clean'.
    String eclipseLauncherBuildType = System.getenv("ECLIPSE_BUILD_TYPE");
    if ("full".equals(eclipseLauncherBuildType)) {
      if (incremental) {
        getLog().info("Disabling incremental processing (Eclipse built type: " + eclipseLauncherBuildType + ")");
        incremental = false;
      }
    }

    this.templatesPath = templatesDir.toPath().toAbsolutePath().normalize();
    this.outputPath = outputDir.toPath().toAbsolutePath().normalize();

    Path basedir = project.getBasedir().toPath().toAbsolutePath().normalize();
    getLog().info(String.format(Locale.ROOT,
        "Processing templates from %s => %s",
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

    getLog().debug("Processing: " + input.getFileName() + " => " + output.path);
    try {
      timeIntrinsics.start();
      template = filterIntrinsics(template, templateOptions);
      timeIntrinsics.stop();
  
      timeComments.start();
      template = filterComments(template);
      timeComments.stop();
  
      timeTypeClassRefs.start();
      template = filterTypeClassRefs(template, templateOptions);
      template = filterStaticTokens(template, templateOptions);
      timeTypeClassRefs.stop();
    } catch (RuntimeException e) {
      getLog().error("Error processing: " + input.getFileName() + " => " + output.path + ":\n\t" + e.getMessage());
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
    Pattern p = Pattern.compile(
                "(Intrinsics.\\s*)" + 
                "(<(?<generic>[^>]+)>\\s*)?" + 
                "(?<method>[a-zA-Z0-9]+)", Pattern.MULTILINE | Pattern.DOTALL);

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

        getLog().debug("Intrinsic call: " + m.group() + "; method: " + 
            method + ", generic: " + m.group("generic") + ", args: " + 
            params);

        // Allow recursion of intrinsics into arguments.
        for (int i = 0; i < params.size(); i++) {
          params.set(i, filterIntrinsics(params.get(i), templateOptions));
        }

        IntrinsicMethod im = intrinsics.get(method);
        if (im == null) {
          throw new RuntimeException(String.format(Locale.ROOT,
              "Unknown intrinsic method '%s' in call: %s",
              method,
              m.group()));
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
      getLog().error("Signature processor failure: " + options.getTemplateFile(), e);
      throw new RuntimeException(e);
    }
  }


  /**
   * Apply velocity to the input.
   */
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
