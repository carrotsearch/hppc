package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class NewKTypeArray implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (templateOptions.isKTypeGeneric()) {
      sb.append("Internals.<KType[]>newArray(" + params.get(0) + ")");
    } else {
      sb.append("new " + templateOptions.getKType().getType() + " [" + params.get(0) + "]");
    }
  }
}
