package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
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

    sb.append(String.format("((%1$s) + (%2$s))", params.toArray()));
  }
}
