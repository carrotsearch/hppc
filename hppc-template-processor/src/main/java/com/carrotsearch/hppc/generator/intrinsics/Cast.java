package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class Cast implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (genericCast == null) {
      throw new RuntimeException("Instrinsic cast requires an explicit generic type: " + m.group());
    }
    
    if (params.size() != 1) {
      throw new RuntimeException("Expected exactly one argument: " + m.group());
    }

    final String cast;
    switch (genericCast) {
      case "KType[]":
      case "KType":
        if (templateOptions.isKTypeGeneric()) {
          cast = "(" + genericCast + ")";
        } else {
          cast = ""; // dropped
        }
        break;

      case "VType[]":
      case "VType":
        if (!templateOptions.hasVType()) {
          throw new RuntimeException("VType is not available for casting in this template?: " + m.group());
        }
        if (templateOptions.isVTypeGeneric()) {
          cast = "(VType)";
        } else {
          cast = ""; // dropped
        }
        break;

      default:
        throw new RuntimeException("Can't resolve generic cast in: " + m.group());
    }

    sb.append(cast + " " + params.get(0));
  }
}
