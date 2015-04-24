package com.carrotsearch.hppc.procedures;

/**
 * A procedure that applies to <code>KType</code>, <code>VType</code> pairs.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeVTypeProcedure<KType, VType> {
  public void apply(KType key, VType value);
}
