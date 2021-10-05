/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("GENERIC"))) !*/
package com.carrotsearch.hppc.comparators;

/**
 * Compares two <code>KType</code> values.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeComparator<KType> {
  int compare(KType a, KType b);

  /*! #if ($TemplateOptions.isKTypeAnyOf("BYTE"))
  static <KType> KTypeComparator<KType> naturalOrder() {
    return Byte::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("CHAR"))
  static <KType> KTypeComparator<KType> naturalOrder() {
    return Character::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("INT"))
  static <KType> KTypeComparator<KType> naturalOrder() {
    return Integer::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("LONG"))
  static <KType> KTypeComparator<KType> naturalOrder() {
    return Long::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("SHORT"))
  static <KType> KTypeComparator<KType> naturalOrder() {
    return Short::compare;
  }
  #end !*/
}
