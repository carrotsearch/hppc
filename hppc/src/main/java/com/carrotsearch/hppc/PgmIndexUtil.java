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

/** Utility methods for {@code KTypePgmIndex}. */
final class PgmIndexUtil {

  /** Adds the first key of the current segment to the segment data bytes. */
  static <KType> void addKey(KType key, IntArrayList segmentData) {
    throw new UnsupportedOperationException("Invalid for generic type: " + key);
  }

  /** Adds the first key of the current segment to the segment data bytes. */
  static void addKey(int key, IntArrayList segmentData) {
    segmentData.add(key);
  }

  /** Adds the first key of the current segment to the segment data bytes. */
  static void addKey(float key, IntArrayList segmentData) {
    addKey(Float.floatToIntBits(key), segmentData);
  }

  /** Adds the first key of the current segment to the segment data bytes. */
  static void addKey(long key, IntArrayList segmentData) {
    segmentData.add((int) key);
    segmentData.add((int) (key >> 32));
  }

  /** Adds the first key of the current segment to the segment data bytes. */
  static void addKey(double key, IntArrayList segmentData) {
    addKey(Double.doubleToRawLongBits(key), segmentData);
  }

  /** Gets the first key of the segment at the given data index. */
  static <KType> KType getKey(int segmentDataIndex, int[] segmentData, KType keyType) {
    throw new UnsupportedOperationException("Invalid for generic type: " + keyType);
  }

  /** Gets the first key of the segment at the given data index. */
  static int getKey(int segmentDataIndex, int[] segmentData, int keyType) {
    return segmentData[segmentDataIndex];
  }

  /** Gets the first key of the segment at the given data index. */
  static float getKey(int segmentDataIndex, int[] segmentData, float keyType) {
    return Float.intBitsToFloat(getKey(segmentDataIndex, segmentData, 0));
  }

  /** Gets the first key of the segment at the given data index. */
  static long getKey(int segmentDataIndex, int[] segmentData, long keyType) {
    return (segmentData[segmentDataIndex] & 0xFFFFFFFFL)
        | (((long) segmentData[segmentDataIndex + 1]) << 32);
  }

  /** Gets the first key of the segment at the given data index. */
  static double getKey(int segmentDataIndex, int[] segmentData, double keyType) {
    return Double.longBitsToDouble(getKey(segmentDataIndex, segmentData, 0L));
  }

  /**
   * Adds the intercept of the current segment to the segment data bytes. The intercept is stored as
   * an int for a key size equal to 1, otherwise it is stored as a long.
   *
   * @param keySize The size of the key, measure in {@link Integer#BYTES}.
   */
  static void addIntercept(long intercept, IntArrayList segmentData, int keySize) {
    assert keySize >= 1 && keySize <= 2;
    if (keySize == 1) {
      addKey((int) intercept, segmentData);
    } else {
      addKey(intercept, segmentData);
    }
  }

  /**
   * Gets the intercept of the segment at the given data index.
   *
   * @param keySize The size of the key, measure in {@link Integer#BYTES}.
   */
  static long getIntercept(int segmentDataIndex, int[] segmentData, int keySize) {
    assert keySize >= 1 && keySize <= 2;
    if (keySize == 1) {
      return getKey(segmentDataIndex, segmentData, 0);
    }
    return getKey(segmentDataIndex, segmentData, 0L);
  }

  /**
   * Adds the slope of the current segment to the segment data bytes. The intercept is stored as a
   * float for a key size equal to 1, otherwise it is stored as a double.
   *
   * @param keySize The size of the key, measure in {@link Integer#BYTES}.
   */
  static void addSlope(double slope, IntArrayList segmentData, int keySize) {
    assert keySize >= 1 && keySize <= 2;
    if (keySize == 1) {
      addKey((float) slope, segmentData);
    } else {
      addKey(slope, segmentData);
    }
  }

  /**
   * Gets the slope of the segment at the given data index.
   *
   * @param keySize The size of the key, measure in {@link Integer#BYTES}.
   */
  static double getSlope(int segmentDataIndex, int[] segmentData, int keySize) {
    assert keySize >= 1 && keySize <= 2;
    if (keySize == 1) {
      return getKey(segmentDataIndex, segmentData, 0f);
    }
    return getKey(segmentDataIndex, segmentData, 0d);
  }
}
