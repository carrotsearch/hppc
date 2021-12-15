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

import java.util.Locale;

public enum Type {
  GENERIC,
  BYTE,
  CHAR,
  SHORT,
  INT,
  FLOAT,
  LONG,
  DOUBLE;

  public String getBoxedType() {
    if (this == GENERIC) {
      return "Object";
    } else {
      String boxed = name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(boxed.charAt(0)) + boxed.substring(1);
    }
  }

  public String getType() {
    if (this == GENERIC) {
      return "Object";
    } else {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public boolean isGeneric() {
    return this == GENERIC;
  }
}
