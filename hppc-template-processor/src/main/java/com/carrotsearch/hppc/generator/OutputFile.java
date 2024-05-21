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

class OutputFile {
  public final Path path;
  public boolean upToDate;

  public OutputFile(Path target) {
    this.path = target.toAbsolutePath().normalize();
  }
}
