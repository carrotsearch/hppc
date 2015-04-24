/*! #set($TemplateOptions.ignored = ($TemplateOptions.KTypePrimitive)) !*/
package com.carrotsearch.hppc;

/* #if ($TemplateOptions.VTypeGeneric) */
import java.util.Iterator;
import com.carrotsearch.hppc.cursors.*;
/* #end */

import static com.carrotsearch.hppc.Containers.*;
import static com.carrotsearch.hppc.HashContainers.*;

/**
 * An identity hash map of <code>KType</code> to <code>VType</code>.
 */
/*! #if ($TemplateOptions.anyGeneric) @SuppressWarnings("all") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeIdentityHashMap<KType, VType> 
  extends KTypeVTypeHashMap<KType, VType>
{
  /**
   * New instance with sane defaults.
   */
  public KTypeVTypeIdentityHashMap() {
    this(DEFAULT_EXPECTED_ELEMENTS);
  }

  /**
   * New instance with sane defaults.
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   */
  public KTypeVTypeIdentityHashMap(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   */
  public KTypeVTypeIdentityHashMap(int expectedElements, double loadFactor) {
    this(expectedElements, loadFactor, HashOrderMixing.randomized());
  }

  /**
   * New instance with the provided defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause a rehash (inclusive).
   * @param loadFactor
   *          The load factor for internal buffers. Insane load factors (zero, full capacity)
   *          are rejected by {@link #verifyLoadFactor(double)}.
   * @param orderMixer
   *          Hash key order mixing strategy. See {@link HashOrderMixing} for predefined
   *          implementations. Use constant mixers only if you understand the potential
   *          consequences.
   */
  public KTypeVTypeIdentityHashMap(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

  /**
   * Create a hash map from all key-value pairs of another container.
   */
  public KTypeVTypeIdentityHashMap(KTypeVTypeAssociativeContainer<? extends KType, ? extends VType> container) {
    this(container.size());
    putAll(container);
  }

  @Override
  public int hashKey(KType key) {
    assert !Intrinsics.<KType> isEmpty(key); // Handled as a special case (empty slot marker).
    return BitMixer.mix(System.identityHashCode(key), this.keyMixer);
  }

  @Override
  public boolean equals(Object v1, Object v2) {
    return v1 == v2;
  }

  /* #if ($TemplateOptions.VTypeGeneric) */
  @Override
  protected boolean equalElements(KTypeVTypeHashMap<?, ?> other) {
    if (other.size() != size()) {
      return false;
    }

    Iterator<? extends KTypeVTypeCursor<?, ?>> i = other.iterator();
    while (i.hasNext()) {
      KTypeVTypeCursor<?, ?> c = i.next();
      KType key = Intrinsics.<KType> cast(c.key);
      if (!containsKey(key) ||
          !equals(get(key), c.value)) {   // Compare values using the same function as keys.
        return false;
      }
    }

    return true;
  }
  /* #end */

  /**
   * Creates a hash map from two index-aligned arrays of key-value pairs.
   */
  public static <KType, VType> KTypeVTypeIdentityHashMap<KType, VType> from(KType[] keys, VType[] values) {
    if (keys.length != values.length) {
      throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
    }

    KTypeVTypeIdentityHashMap<KType, VType> map = new KTypeVTypeIdentityHashMap<>(keys.length);
    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }

    return map;
  }  
}
