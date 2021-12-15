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

class OutputFile {
  public final Path path;
  public boolean upToDate;

  public OutputFile(Path target) {
    this.path = target.toAbsolutePath().normalize();
  }
}
