package com.carrotsearch.hppc.generator;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.common.base.Preconditions;

/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions {
  public static final String TEMPLATE_FILE_TOKEN = "__TEMPLATE_SOURCE__";

  private boolean ignore;

  public Type ktype;
  public Type vtype;

  public Path templateFile;

  public TemplateOptions(Type ktype) {
    this(ktype, null);
  }

  public TemplateOptions(Type ktype, Type vtype) {
    this.ktype = ktype;
    this.vtype = vtype;
  }

  public void setIgnored(boolean ignore) {
    this.ignore = ignore;
  }

  public boolean isIgnored() {
    return ignore;
  }

  public boolean isKTypePrimitive() {
    return ktype != Type.GENERIC;
  }

  public boolean isVTypePrimitive() {
    return getVType() != Type.GENERIC;
  }

  public boolean isKTypeGeneric() {
    return ktype == Type.GENERIC;
  }

  public boolean isVTypeGeneric() {
    return getVType() == Type.GENERIC;
  }

  public boolean isAllGeneric() {
    return isKTypeGeneric() && isVTypeGeneric();
  }

  public boolean isAnyPrimitive() {
    return isKTypePrimitive() || isVTypePrimitive();
  }

  public boolean isAnyGeneric() {
    return isKTypeGeneric() || (hasVType() && isVTypeGeneric());
  }

  public boolean hasVType() {
    return vtype != null;
  }

  public boolean hasKType() {
    return true;
  }

  public Type getKType() {
    Preconditions.checkArgument(hasKType(), "Template does not specify KType.");
    return ktype;
  }

  public Type getVType() {
    Preconditions.checkArgument(hasVType(), "Template does not specify VType.");
    return vtype;
  }

  /*
   * Returns the current time in ISO format.
   */
  public String getTimeNow() {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
    return format.format(new Date());
  }

  public String getTemplateFile() {
    return templateFile.getFileName().toString();
  }

  public String getGeneratedAnnotation() {
    return String.format(Locale.ROOT, 
        "@javax.annotation.Generated(\n" + 
        "    date = \"%s\",\n" +
        "    value = \"%s\")",
        getTimeNow(),
        TEMPLATE_FILE_TOKEN);
  }
}