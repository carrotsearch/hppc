package com.carrotsearch.hppc.generator;

import java.nio.file.Path;

class OutputFile {
  public final Path path;
  public boolean upToDate;

  public OutputFile(Path target) {
    this.path = target.toAbsolutePath().normalize();
  }
}