package com.carrotsearch.hppc.generator.intrinsics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.generator.IntrinsicMethod;
import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;

abstract class AbstractIntrinsicMethod implements IntrinsicMethod {
  protected static Type inferTemplateType(Matcher m, TemplateOptions templateOptions, String templateCast) {
    templateCast = inferTemplateCastName(m, templateOptions, templateCast);

    Type type;
    switch (templateCast) {
      case "KType":
        if (!templateOptions.hasKType()) {
          throw new RuntimeException(format(
              "Template cast requires %s but the template does not have it: %s",
              templateCast,
              m.group()));
        }
        type = templateOptions.getKType(); 
        break;
      case "VType":
        if (!templateOptions.hasVType()) {
          throw new RuntimeException(format(
              "Template cast requires %s but the template does not have it: %s",
              templateCast,
              m.group()));
        }
        type = templateOptions.getVType(); 
        break;
      default:
        throw new RuntimeException(format(
            "Ukrecognized template cast to %s in: %s",
            templateCast,
            m.group()));
    }
    return type;
  }

  protected static String inferTemplateCastName(Matcher m, TemplateOptions templateOptions, String templateCast) {
    if (templateCast == null || templateCast.isEmpty()) {
      if (templateOptions.hasKType() && !templateOptions.hasVType()) {
        templateCast = "KType";
      } else if (templateOptions.hasVType() && !templateOptions.hasKType()) {
        templateCast = "VType";
      }
    }

    if (templateCast == null) {
      throw new RuntimeException(format(
              "Couldn't infer template type of: %s",
              m.group()));
    }
    return templateCast;
  }

  protected static void expectArgumentCount(Matcher m, ArrayList<String> arguments, int expectedCount) {
    if (arguments.size() != expectedCount) {
      throw new RuntimeException(format("Expected exactly %d arguments but was %d: %s(%s)",
              expectedCount,
              arguments.size(),
              m.group(),
              arguments));
    }
  }
  
  protected static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
  
  protected RuntimeException unreachable() {
    throw new RuntimeException("Unreachable block reached.");
  }  
}
