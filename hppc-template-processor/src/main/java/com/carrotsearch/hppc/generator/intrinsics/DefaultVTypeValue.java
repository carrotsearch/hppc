package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class DefaultVTypeValue implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (templateOptions.isVTypeGeneric()) {
      sb.append("null");
    } else {
      sb.append("((" + templateOptions.getVType().getType() + ") 0)");
    }
  }
}
