package com.carrotsearch.hppc.generator;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Adds additional source directories to test or main sources.
 */
@Mojo(name = "add-source", 
      defaultPhase = LifecyclePhase.GENERATE_SOURCES,
      threadSafe = true,
      requiresProject = true)
public class AddSourceMojo extends AbstractMojo {
  @Parameter( required = true )
  public File[] sources;

  @Parameter(property = "project",
      readonly = true,
      required = true)
  private MavenProject project;

  public void execute() {
    for (File source : sources) {
      this.project.addCompileSourceRoot(source.getAbsolutePath());
    }
  }
}