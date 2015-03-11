package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;

public class EqualsKType implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (templateOptions.isKTypeGeneric()) {
      sb.append(
          String.format("((%1$s) == null ? (%2$s) == null : (%1$s).equals((%2$s)))",
              params.toArray()));
    } else if (templateOptions.ktype == Type.DOUBLE) {
      sb.append(
          String.format("(Double.doubleToLongBits(%1$s) == Double.doubleToLongBits(%2$s))",
              params.toArray()));
    } else if (templateOptions.ktype == Type.FLOAT) {
      sb.append(
          String.format("(Float.floatToIntBits(%1$s) == Float.floatToIntBits(%2$s))",
              params.toArray()));
    } else {
      sb.append(
          String.format("((%1$s) == (%2$s))",
              params.toArray()));
    }
  }
}
