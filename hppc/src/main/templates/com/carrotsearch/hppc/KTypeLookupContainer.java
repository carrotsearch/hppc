package com.carrotsearch.hppc;

/**
 * Marker interface for containers that can check if they contain a given object
 * in at least time <code>O(log n)</code> and ideally in amortized constant time
 * <code>O(1)</code>.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeLookupContainer<KType> extends KTypeContainer<KType> {
  public boolean contains(KType e);
}
