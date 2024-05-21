/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.generator.intrinsics;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Numeric extends AbstractIntrinsicMethod {
  @Override
  public void invoke(
      Matcher m,
      StringBuilder sb,
      TemplateOptions templateOptions,
      String genericCast,
      ArrayList<String> params) {
    expectArgumentCount(m, params, 1);
    if (inferTemplateType(m, templateOptions, genericCast) == Type.GENERIC) {
      throw new RuntimeException("Can't get the numeric value of generic types: " + m.group());
    }
    sb.append(params.get(0));
  }
}
