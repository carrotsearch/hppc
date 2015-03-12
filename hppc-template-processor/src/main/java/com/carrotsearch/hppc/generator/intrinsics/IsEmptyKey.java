package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class IsEmptyKey implements IntrinsicMethod {
  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (params.size() != 1) {
      throw new RuntimeException("Expected exactly one argument: " + m.group());
    }
    
    if (templateOptions.isKTypeGeneric()) {
      sb.append(String.format(Locale.ROOT, 
          "((%s) == null)",
          params.get(0)));
    } else {
      switch (templateOptions.getKType()) {
        case DOUBLE:
          sb.append(String.format(Locale.ROOT, "(Double.doubleToLongBits(%s) == 0)", params.get(0)));
          break;

        case FLOAT:
          sb.append(String.format(Locale.ROOT, "(Float.floatToIntBits(%s) == 0)", params.get(0)));
          break;

        default:
          sb.append(String.format(Locale.ROOT, "((%s) == 0)", params.get(0)));
          break;
      }
    }
  }
}
