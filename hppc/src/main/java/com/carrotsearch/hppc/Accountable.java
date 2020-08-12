/*
 * HPPC
 *
 * Copyright (C) 2010-2020 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

/**
 * Anything that could be accounted for memory usage
 *
 * <p>Partly forked from Lucene tag releases/lucene-solr/8.5.1
 */
public interface Accountable {
  /**
   * Allocated memory estimation
   *
   * @return Ram allocated in bytes
   */
  public long ramBytesAllocated();

  /**
   * Bytes that is actually been used
   *
   * @return Ram used in bytes
   */
  public long ramBytesUsed();
}
