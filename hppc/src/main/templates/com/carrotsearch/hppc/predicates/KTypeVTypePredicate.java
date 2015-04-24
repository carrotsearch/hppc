package com.carrotsearch.hppc.predicates;

/**
 * A predicate that applies to <code>KType</code>, <code>VType</code> pairs.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeVTypePredicate<KType, VType> {
  public boolean apply(KType key, VType value);
}
