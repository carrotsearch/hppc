package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;

public class Mix implements IntrinsicMethod {
  private String function;

  public Mix(String function) {
    this.function = function;
  }

  @Override
  public void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params) {
    if (genericCast != null) {
      throw new RuntimeException("Instrinsic mix doesn't use any generic type: " + m.group());
    }

    if (params.size() != 2) {
      throw new RuntimeException("Expected exactly two arguments: " + m.group());
    }

    final String mix;
    switch (templateOptions.getKType()) {
      case FLOAT:
        mix = String.format(Locale.ROOT, "Float.floatToIntBits(%s) ^ (%s)", params.get(0), params.get(1));
        break;

      case DOUBLE:
        mix = String.format(Locale.ROOT, "Double.doubleToLongBits(%s) ^ (%s)", params.get(0), params.get(1));
        break;
      
      case GENERIC:
        // Null keys are handled separately so this is safe.
        mix = String.format(Locale.ROOT, "%s.hashCode() ^ (%s)", params.get(0), params.get(1));
        break;

      default:
        mix = String.format(Locale.ROOT, "%s ^ (%s)", params.get(0), params.get(1));
        break;
    }

    sb.append(String.format(Locale.ROOT, function, mix));
  }
}
