package com.carrotsearch.hppc.comparators;

/**
 * Compares two <code>KType</code>, <code>VType</code> pairs.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeVTypeComparator<KType, VType> {
  int compare(KType k1, VType v1, KType k2, VType v2);
}
