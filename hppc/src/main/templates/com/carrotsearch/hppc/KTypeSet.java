package com.carrotsearch.hppc;

/**
 * A set of <code>KType</code>s.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeSet<KType> extends KTypeCollection<KType> {
  /**
   * Adds <code>k</code> to the set.
   * 
   * @return Returns <code>true</code> if this element was not part of the set
   *         before. Returns <code>false</code> if an equal element is part of
   *         the set, <b>and replaces the existing equal element</b> with the
   *         argument (if keys are object types).
   */
  public boolean add(KType k);
}
