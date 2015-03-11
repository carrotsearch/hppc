package com.carrotsearch.hppc.generator;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Intrinsic method invocation.
 */
public interface IntrinsicMethod {
  void invoke(Matcher m, StringBuilder sb, TemplateOptions templateOptions, String genericCast, ArrayList<String> params);
}
