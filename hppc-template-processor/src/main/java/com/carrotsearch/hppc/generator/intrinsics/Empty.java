/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.generator.intrinsics;

import com.carrotsearch.hppc.generator.TemplateOptions;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Empty extends AbstractIntrinsicMethod {
  @Override
  public void invoke(
      Matcher m,
      StringBuilder sb,
      TemplateOptions templateOptions,
      String genericCast,
      ArrayList<String> params) {
    expectArgumentCount(m, params, 0);

    switch (inferTemplateType(m, templateOptions, genericCast)) {
      case GENERIC:
        sb.append("null");
        break;

      case FLOAT:
        sb.append("0f");
        break;

      case DOUBLE:
        sb.append("0d");
        break;

      case BYTE:
        sb.append("((byte) 0)");
        break;

      case CHAR:
        sb.append("((char) 0)");
        break;

      case SHORT:
        sb.append("((short) 0)");
        break;

      case INT:
        sb.append("0");
        break;

      case LONG:
        sb.append("0L");
        break;

      default:
        throw unreachable();
    }
  }
}
