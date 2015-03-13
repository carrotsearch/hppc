package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;

public class Mix implements IntrinsicMethod {
  private String function;
  private boolean allowNull;

  public Mix(String function, boolean allowNull) {
    this.function = function;
    this.allowNull = allowNull;
  }

  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (genericCast == null) {
      throw new RuntimeException("Instrinsic mix requires generic type: " + m.group());
    }

    if (params.size() != 1 &&
        params.size() != 2) {
      throw new RuntimeException("Expected one or two arguments: " + m.group());
    }

    final Type type;
    switch (genericCast) {
      case "KType":
        type = templateOptions.getKType();
        break;
      case "VType":
        type = templateOptions.getVType();
        break;
      default:
        throw new RuntimeException("Only KType or VType supported.");
    }

    final String bits;
    switch (type) {
      case FLOAT:
        bits = f("Float.floatToIntBits(%s)", params.get(0));
        break;

      case DOUBLE:
        bits = f("Double.doubleToLongBits(%s)", params.get(0));
        break;

      case GENERIC:
        if (allowNull) {
          bits = f("((%s) == null ? 0 : (%s).hashCode())", params.get(0), params.get(0));
        } else {
          bits = f("%s.hashCode()", params.get(0));
        }
        break;

      default:
        bits = f("%s", params.get(0));
        break;
    }

    if (params.size() == 1) {
      sb.append(f(function, f("%s", bits)));
    } else if (params.size() == 2) {
      sb.append(f(function, f("(%s) ^ (%s)", bits, params.get(1))));
    } else {
      throw new RuntimeException();
    }
  }

  public static String f(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
}
