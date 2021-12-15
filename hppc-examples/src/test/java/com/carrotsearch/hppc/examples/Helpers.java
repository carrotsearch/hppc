/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.examples;

import com.carrotsearch.hppc.SuppressForbidden;
import java.util.Locale;

class Helpers {
  @SuppressForbidden
  public static void printf(String msg, Object... args) {
    System.out.printf(Locale.ROOT, msg, args);
  }

  @SuppressForbidden
  public static void printfln(String msg, Object... args) {
    System.out.printf(Locale.ROOT, msg, args);
    System.out.println();
  }
}
