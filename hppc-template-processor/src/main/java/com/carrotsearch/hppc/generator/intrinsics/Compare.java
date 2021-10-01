/*
 * HPPC
 *
 * Copyright (C) 2010-2020 Carrot Search s.c.
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

public class Compare extends AbstractIntrinsicMethod {
  @Override
  public void invoke(
      Matcher m,
      StringBuilder sb,
      TemplateOptions templateOptions,
      String genericCast,
      ArrayList<String> arguments) {

    if (arguments.size() != 2) {
      throw new RuntimeException(
          format(
              "Expected exactly 2 arguments but was %d: %s(%s)",
              arguments.size(), m.group(), arguments));
    }

    String v2 = arguments.remove(arguments.size() - 1);
    String v1 = arguments.remove(arguments.size() - 1);

    Type type = inferTemplateType(m, templateOptions, genericCast);
    switch (type) {
      case GENERIC:
        sb.append(format("((Comparable<%s>) %s).compareTo(%s)", genericCast, v1, v2));
        break;
      case FLOAT:
        sb.append(format("Float.compare(%s, %s)", v1, v2));
        break;
      case DOUBLE:
        sb.append(format("Double.compare(%s, %s)", v1, v2));
        break;
      case BYTE:
        sb.append(format("Byte.compare(%s, %s)", v1, v2));
        break;
      case SHORT:
        sb.append(format("Short.compare(%s, %s)", v1, v2));
        break;
      case CHAR:
        sb.append(format("Character.compare(%s, %s)", v1, v2));
        break;
      case INT:
        sb.append(format("Integer.compare(%s, %s)", v1, v2));
        break;
      case LONG:
        sb.append(format("Long.compare(%s, %s)", v1, v2));
        break;

      default:
        throw unreachable();
    }
  }
}
