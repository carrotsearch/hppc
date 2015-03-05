package com.carrotsearch.hppc.generator;

import java.nio.file.Path;

class TemplateFile {
  public final Path path;

  public TemplateFile(Path path) {
    this.path = path;
  }

  public String getFileName() {
    return path.getFileName().toString();
  }
}