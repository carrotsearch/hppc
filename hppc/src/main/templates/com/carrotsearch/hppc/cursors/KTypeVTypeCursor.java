package com.carrotsearch.hppc.cursors;

/**
 * A cursor over entries of an associative container (KType keys and VType
 * values).
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeVTypeCursor<KType, VType> {
  /**
   * The current key and value's index in the container this cursor belongs to.
   * The meaning of this index is defined by the container (usually it will be
   * an index in the underlying storage buffer).
   */
  public int index;

  /**
   * The current key.
   */
  public KType key;

  /**
   * The current value.
   */
  public VType value;

  @Override
  public String toString() {
    return "[cursor, index: " + index + ", key: " + key + ", value: " + value + "]";
  }
}
