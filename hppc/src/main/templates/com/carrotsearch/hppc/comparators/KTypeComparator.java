/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("GENERIC"))) !*/
package com.carrotsearch.hppc.comparators;

/**
 * Compares two <code>KType</code> values.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeComparator<KType> {
  int compare(KType a, KType b);
}
