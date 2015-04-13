package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;

public class Add implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (genericCast == null) {
      throw new RuntimeException("Instrinsic add requires an explicit generic type: " + m.group());
    }

    if (params.size() != 2) {
      throw new RuntimeException("Expected exactly two arguments: " + m.group());
    }

    Type type;
    switch (genericCast) {
      case "KType":
        type = templateOptions.getKType(); 
        break;
      case "VType":
        type = templateOptions.getVType(); 
        break;
      default:
        throw new RuntimeException("Unrecognized type: " + genericCast);
    }

    if (type.isGeneric()) {
      throw new RuntimeException("Can't add generic types: " + m.group());
    } else {
      sb.append(String.format("((%1$s) + (%2$s))", params.toArray()));
    }
  }
}
