package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.HashContainers.*;
import static com.carrotsearch.hppc.Containers.*;

/**
 * A reference-equality (identity) hash set.
 */
public class ObjectIdentityHashSet<KType> extends ObjectHashSet<KType> {
  /**
   * New instance with sane defaults.
   */
  public ObjectIdentityHashSet() {
    this(DEFAULT_EXPECTED_ELEMENTS, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   */
  public ObjectIdentityHashSet(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with sane defaults.
   */
  public ObjectIdentityHashSet(int expectedElements, double loadFactor) {
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
  public ObjectIdentityHashSet(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

  /**
   * New instance copying elements from another {@link ObjectContainer}.
   */
  public ObjectIdentityHashSet(ObjectContainer<? extends KType> container) {
    this(container.size());
    addAll(container);
  }
  
  @Override
  protected int hashKey(KType key) {
    assert key != null; // Handled as a special case (empty slot marker).
    return BitMixer.mix(key, this.keyMixer);
  }
  
  @Override
  protected boolean equals(Object v1, Object v2) {
    return v1 == v2;
  }
  
  /**
   * Create a set from a variable number of arguments or an array of
   * <code>KType</code>. The elements are copied from the argument to the
   * internal buffer.
   */
  @SafeVarargs
  public static <KType> ObjectIdentityHashSet<KType> from(KType... elements) {
    final ObjectIdentityHashSet<KType> set = new ObjectIdentityHashSet<KType>(elements.length);
    set.addAll(elements);
    return set;
  }
}