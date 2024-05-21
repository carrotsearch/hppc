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

/** Anything that can preallocate buffers given prior knowledge of the number of stored elements. */
public interface Preallocable {
  /**
   * Ensure this container can hold at least the given number of elements without resizing its
   * buffers.
   *
   * @param expectedElements The total number of elements, inclusive.
   */
  public void ensureCapacity(int expectedElements);
}
