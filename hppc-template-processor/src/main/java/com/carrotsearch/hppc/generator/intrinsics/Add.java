package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;

public class Add extends AbstractIntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    expectArgumentCount(m, params, 2);

    Type type = inferTemplateType(m, templateOptions, genericCast);
    if (type.isGeneric()) {
      throw new RuntimeException("Can't add generic types: " + m.group());
    }

    ArrayList<String> newArgs = new ArrayList<>(params);
    newArgs.add(type.getType());
    sb.append(String.format(Locale.ROOT, "((%3$s) ((%1$s) + (%2$s)))", newArgs.toArray()));
  }
}
