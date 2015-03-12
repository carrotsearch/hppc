package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class NewArray implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (genericCast == null) {
      throw new RuntimeException("Instrinsic newArray requires an explicit generic type: " + m.group());
    }

    if (params.size() != 1) {
      throw new RuntimeException("Expected exactly one argument: " + m.group());
    }

    final String cast;
    switch (genericCast) {
      case "KType":
        if (templateOptions.isKTypeGeneric()) {
          cast = String.format(Locale.ROOT, 
              "((%s[]) new Object [%s])",
              genericCast,
              params.get(0));
        } else {
          cast = String.format(Locale.ROOT, 
              "(new %s [%s])",
              templateOptions.getKType().getType(),
              params.get(0));
        }
        break;

      case "VType":
        if (templateOptions.isVTypeGeneric()) {
          cast = String.format(Locale.ROOT, 
              "((%s[]) new Object [%s])",
              genericCast,
              params.get(0));
        } else {
          cast = String.format(Locale.ROOT, 
              "(new %s [%s])",
              templateOptions.getVType().getType(),
              params.get(0));
        }
        break;

      default:
        throw new RuntimeException("Can't resolve generic cast in: " + m.group());
    }

    sb.append(cast);
  }
}
