/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.Containers.*;
import static com.carrotsearch.hppc.HashContainers.*;

/** A reference-equality (identity) hash set. */
public class ObjectIdentityHashSet<KType> extends ObjectHashSet<KType> {
  /** New instance with sane defaults. */
  public ObjectIdentityHashSet() {
    this(DEFAULT_EXPECTED_ELEMENTS, DEFAULT_LOAD_FACTOR);
  }

  /** New instance with sane defaults. */
  public ObjectIdentityHashSet(int expectedElements) {
    this(expectedElements, DEFAULT_LOAD_FACTOR);
  }

  /**
   * New instance with the provided defaults.
   *
   * @param expectedElements The expected number of elements guaranteed not to cause a rehash
   *     (inclusive).
   * @param loadFactor The load factor for internal buffers. Insane load factors (zero, full
   *     capacity) are rejected by {@link #verifyLoadFactor(double)}.
   */
  public ObjectIdentityHashSet(int expectedElements, double loadFactor) {
    super(expectedElements, loadFactor);
  }

  /** New instance copying elements from another {@link ObjectContainer}. */
  public ObjectIdentityHashSet(ObjectContainer<? extends KType> container) {
    this(container.size());
    addAll(container);
  }

  @Override
  protected int hashKey(KType key) {
    assert key != null; // Handled as a special case (empty slot marker).
    return BitMixer.mixPhi(System.identityHashCode(key));
  }

  @Override
  protected boolean equals(Object v1, Object v2) {
    return v1 == v2;
  }

  /**
   * Create a set from a variable number of arguments or an array of <code>KType</code>. The
   * elements are copied from the argument to the internal buffer.
   */
  @SafeVarargs
  public static <KType> ObjectIdentityHashSet<KType> from(KType... elements) {
    final ObjectIdentityHashSet<KType> set = new ObjectIdentityHashSet<KType>(elements.length);
    set.addAll(elements);
    return set;
  }
}
