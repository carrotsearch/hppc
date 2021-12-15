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

import java.util.ArrayList;
import java.util.regex.Matcher;

/** Intrinsic method invocation. */
public interface IntrinsicMethod {
  void invoke(
      Matcher m,
      StringBuilder sb,
      TemplateOptions templateOptions,
      String genericCast,
      ArrayList<String> params);
}
