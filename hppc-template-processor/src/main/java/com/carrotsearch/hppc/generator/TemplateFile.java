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
