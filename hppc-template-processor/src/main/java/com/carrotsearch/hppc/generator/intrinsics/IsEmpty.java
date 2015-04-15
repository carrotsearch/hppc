package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.TemplateOptions;

public class IsEmpty extends AbstractIntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    expectArgumentCount(m, params, 1);

    switch (inferTemplateType(m, templateOptions, genericCast)) {
      case GENERIC:
        sb.append(format("((%s) == null)", params.get(0)));
        break;

      case FLOAT:
        sb.append(format("(Float.floatToIntBits(%s) == 0)", params.get(0)));
        break;

      case DOUBLE:
        sb.append(format("(Double.doubleToLongBits(%s) == 0)", params.get(0)));
        break;

      case BYTE:
      case CHAR:
      case INT:
      case LONG:
      case SHORT:
        sb.append(format("((%s) == 0)", params.get(0)));
        break;

      default:
        throw unreachable();
    }
  }
}
