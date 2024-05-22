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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Helper class that helps estimate memory usage
 *
 * <p>Mostly forked from Lucene tag releases/lucene-solr/8.5.1
 */
final class RamUsageEstimator {
  /** No instantiation. */
  private RamUsageEstimator() {}

  /** True, iff compressed references (oops) are enabled by this JVM */
  static final boolean COMPRESSED_REFS_ENABLED;

  /** Number of bytes this JVM uses to represent an object reference. */
  static final int NUM_BYTES_OBJECT_REF;

  /** Number of bytes to represent an object header (no fields, no alignments). */
  static final int NUM_BYTES_OBJECT_HEADER;

  /** Number of bytes to represent an array header (no content, but with alignments). */
  static final int NUM_BYTES_ARRAY_HEADER;

  /**
   * A constant specifying the object alignment boundary inside the JVM. Objects will always take a
   * full multiple of this constant, possibly wasting some space.
   */
  static final int NUM_BYTES_OBJECT_ALIGNMENT;

  /** Sizes of primitive classes. */
  static final Map<Class<?>, Integer> primitiveSizes;

  static {
    Map<Class<?>, Integer> primitiveSizesMap = new IdentityHashMap<>();
    primitiveSizesMap.put(boolean.class, 1);
    primitiveSizesMap.put(byte.class, 1);
    primitiveSizesMap.put(char.class, Character.BYTES);
    primitiveSizesMap.put(short.class, Short.BYTES);
    primitiveSizesMap.put(int.class, Integer.BYTES);
    primitiveSizesMap.put(float.class, Float.BYTES);
    primitiveSizesMap.put(double.class, Double.BYTES);
    primitiveSizesMap.put(long.class, Long.BYTES);

    primitiveSizes = Collections.unmodifiableMap(primitiveSizesMap);
  }

  static final boolean JRE_IS_64BIT;

  static final String MANAGEMENT_FACTORY_CLASS = "java.lang.management.ManagementFactory";
  static final String HOTSPOT_BEAN_CLASS = "com.sun.management.HotSpotDiagnosticMXBean";

  static final String OS_ARCH = System.getProperty("os.arch");

  // Initialize constants and try to collect information about the JVM internals.
  static {
    boolean is64Bit = false;
    String datamodel = null;
    try {
      datamodel = System.getProperty("sun.arch.data.model");
      if (datamodel != null) {
        is64Bit = datamodel.contains("64");
      }
    } catch (SecurityException ignored) {
    }
    if (datamodel == null) {
      is64Bit = OS_ARCH != null && OS_ARCH.contains("64");
    }
    JRE_IS_64BIT = is64Bit;
    if (JRE_IS_64BIT) {
      // Try to get compressed oops and object alignment (the default seems to be 8 on Hotspot);
      // (this only works on 64 bit, on 32 bits the alignment and reference size is fixed):
      boolean compressedOops = false;
      int objectAlignment = 8;
      try {
        final Class<?> beanClazz = Class.forName(HOTSPOT_BEAN_CLASS);
        // we use reflection for this, because the management factory is not part
        // of Java 8's compact profile:
        final Object hotSpotBean =
            Class.forName(MANAGEMENT_FACTORY_CLASS)
                .getMethod("getPlatformMXBean", Class.class)
                .invoke(null, beanClazz);
        if (hotSpotBean != null) {
          final Method getVMOptionMethod = beanClazz.getMethod("getVMOption", String.class);
          try {
            final Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "UseCompressedOops");
            compressedOops =
                Boolean.parseBoolean(
                    vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
          } catch (ReflectiveOperationException | RuntimeException ignored) {
          }
          try {
            final Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "ObjectAlignmentInBytes");
            objectAlignment =
                Integer.parseInt(
                    vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
          } catch (ReflectiveOperationException | RuntimeException ignored) {
          }
        }
      } catch (ReflectiveOperationException | RuntimeException ignored) {
      }
      COMPRESSED_REFS_ENABLED = compressedOops;
      NUM_BYTES_OBJECT_ALIGNMENT = objectAlignment;
      // reference size is 4, if we have compressed oops:
      NUM_BYTES_OBJECT_REF = COMPRESSED_REFS_ENABLED ? 4 : 8;
      // "best guess" based on reference size:
      NUM_BYTES_OBJECT_HEADER = 8 + NUM_BYTES_OBJECT_REF;
      // array header is NUM_BYTES_OBJECT_HEADER + NUM_BYTES_INT, but aligned (object alignment):
      NUM_BYTES_ARRAY_HEADER = (int) alignObjectSize(NUM_BYTES_OBJECT_HEADER + Integer.BYTES);
    } else {
      COMPRESSED_REFS_ENABLED = false;
      NUM_BYTES_OBJECT_ALIGNMENT = 8;
      NUM_BYTES_OBJECT_REF = 4;
      NUM_BYTES_OBJECT_HEADER = 8;
      // For 32 bit JVMs, no extra alignment of array header:
      NUM_BYTES_ARRAY_HEADER = NUM_BYTES_OBJECT_HEADER + Integer.BYTES;
    }
  }

  /** Aligns an object size to be the next multiple of {@link #NUM_BYTES_OBJECT_ALIGNMENT}. */
  static long alignObjectSize(long size) {
    size += (long) NUM_BYTES_OBJECT_ALIGNMENT - 1L;
    return size - (size % NUM_BYTES_OBJECT_ALIGNMENT);
  }

  /**
   * Return used part of shallow size of any <code>array</code>.
   *
   * @param usedSize Size that array is actually used
   */
  static long shallowUsedSizeOfArray(Object array, int usedSize) {
    long size = NUM_BYTES_ARRAY_HEADER;
    if (usedSize > 0) {
      Class<?> arrayElementClazz = array.getClass().getComponentType();
      if (arrayElementClazz.isPrimitive()) {
        size += (long) usedSize * primitiveSizes.get(arrayElementClazz);
      } else {
        size += (long) NUM_BYTES_OBJECT_REF * usedSize;
      }
    }
    return alignObjectSize(size);
  }

  /** Return shallow size of any <code>array</code>. */
  static long shallowSizeOfArray(Object array) {
    return shallowUsedSizeOfArray(array, Array.getLength(array));
  }
}
