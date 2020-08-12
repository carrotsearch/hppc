package com.carrotsearch.hppc.examples;

import java.util.Locale;

import com.carrotsearch.hppc.SuppressForbidden;

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
