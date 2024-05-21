/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
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
