/*
 * HPPC
 *
 * Copyright (C) 2010-2026 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static org.junit.jupiter.api.Assertions.*;

import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class BitMixerTest extends RandomizedTest {

  private static final int PHI_C32 = 0x9e3779b9;
  private static final long PHI_C64 = 0x9e3779b97f4a7c15L;

  @Test
  public void testMixByte() {
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      byte b = (byte) i;
      assertEquals(b * PHI_C32, BitMixer.mix(b));
    }
  }

  @Test
  public void testMixShort() {
    for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      short s = (short) i;
      assertEquals(BitMixer.mixPhi(s), BitMixer.mix(s));
    }
  }

  @Test
  public void testMixChar() {
    for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
      char c = (char) i;
      assertEquals(BitMixer.mixPhi(c), BitMixer.mix(c));
    }
  }

  @Test
  public void testMixInt(Random rnd) {
    int[] edgeCases = {0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x55555555, 0xAAAAAAAA};
    for (int k : edgeCases) {
      assertEquals(BitMixer.mix32(k), BitMixer.mix(k));
    }
    for (int i = 0; i < 10000; i++) {
      int k = rnd.nextInt();
      assertEquals(BitMixer.mix32(k), BitMixer.mix(k));
    }
  }

  @Test
  public void testMixFloat(Random rnd) {
    float[] edgeCases = {
      0.0f,
      -0.0f,
      Float.NaN,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.MIN_VALUE,
      Float.MAX_VALUE,
      1.0f,
      -1.0f
    };
    for (float f : edgeCases) {
      assertEquals(BitMixer.mix32(Float.floatToIntBits(f)), BitMixer.mix(f));
    }
    for (int i = 0; i < 10000; i++) {
      float f = rnd.nextFloat();
      assertEquals(BitMixer.mix32(Float.floatToIntBits(f)), BitMixer.mix(f));
    }
  }

  @Test
  public void testMixDouble(Random rnd) {
    double[] edgeCases = {
      0.0,
      -0.0,
      Double.NaN,
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      Double.MIN_VALUE,
      Double.MAX_VALUE,
      1.0,
      -1.0
    };
    for (double d : edgeCases) {
      assertEquals((int) BitMixer.mix64(Double.doubleToLongBits(d)), BitMixer.mix(d));
    }
    for (int i = 0; i < 10000; i++) {
      double d = rnd.nextDouble();
      assertEquals((int) BitMixer.mix64(Double.doubleToLongBits(d)), BitMixer.mix(d));
    }
  }

  @Test
  public void testMixLong(Random rnd) {
    long[] edgeCases = {
      0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 0x5555555555555555L, 0xAAAAAAAAAAAAAAAAL
    };
    for (long l : edgeCases) {
      assertEquals((int) BitMixer.mix64(l), BitMixer.mix(l));
    }
    for (int i = 0; i < 10000; i++) {
      long l = rnd.nextLong();
      assertEquals((int) BitMixer.mix64(l), BitMixer.mix(l));
    }
  }

  @Test
  public void testMixObject(Random rnd) {
    assertEquals(0, BitMixer.mix(null));
    assertEquals(BitMixer.mix32("test".hashCode()), BitMixer.mix("test"));

    for (int i = 0; i < 1000; i++) {
      int obj = rnd.nextInt();
      assertEquals(BitMixer.mix32(Integer.hashCode(obj)), BitMixer.mix((Object) obj));
    }
  }

  @Test
  public void testMixPhiByte() {
    Set<Integer> distinct = new HashSet<>();
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      byte b = (byte) i;
      int expectedH = b * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(b));
      distinct.add(BitMixer.mixPhi(b));
    }
    assertEquals(256, distinct.size());
  }

  @Test
  public void testMixPhiChar() {
    Set<Integer> distinct = new HashSet<>();
    for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
      char c = (char) i;
      int expectedH = c * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(c));
      distinct.add(BitMixer.mixPhi(c));
    }
    // mixPhi is bijective for 16-bit domain because multiplication by odd constant PHI_C32 is
    // invertible and x ^ (x >>> 16) is invertible.
    assertEquals(65536, distinct.size());
  }

  @Test
  public void testMixPhiShort() {
    Set<Integer> distinct = new HashSet<>();
    for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      short s = (short) i;
      int expectedH = s * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(s));
      distinct.add(BitMixer.mixPhi(s));
    }
    assertEquals(65536, distinct.size());
  }

  @Test
  public void testMixPhiInt(Random rnd) {
    int[] edgeCases = {0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x55555555, 0xAAAAAAAA};
    for (int k : edgeCases) {
      int expectedH = k * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(k));
    }
    for (int i = 0; i < 10000; i++) {
      int k = rnd.nextInt();
      int expectedH = k * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(k));
    }
  }

  @Test
  public void testMixPhiFloat(Random rnd) {
    float[] edgeCases = {
      0.0f,
      -0.0f,
      Float.NaN,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.MIN_VALUE,
      Float.MAX_VALUE,
      1.0f,
      -1.0f
    };
    for (float f : edgeCases) {
      int expectedH = Float.floatToIntBits(f) * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(f));
    }
    for (int i = 0; i < 10000; i++) {
      float f = rnd.nextFloat();
      int expectedH = Float.floatToIntBits(f) * PHI_C32;
      int expected = expectedH ^ (expectedH >>> 16);
      assertEquals(expected, BitMixer.mixPhi(f));
    }
  }

  @Test
  public void testMixPhiDouble(Random rnd) {
    double[] edgeCases = {
      0.0,
      -0.0,
      Double.NaN,
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      Double.MIN_VALUE,
      Double.MAX_VALUE,
      1.0,
      -1.0
    };
    for (double d : edgeCases) {
      long expectedH = Double.doubleToLongBits(d) * PHI_C64;
      int expected = Long.hashCode(expectedH);
      assertEquals(expected, BitMixer.mixPhi(d));
    }
    for (int i = 0; i < 10000; i++) {
      double d = rnd.nextDouble();
      long expectedH = Double.doubleToLongBits(d) * PHI_C64;
      int expected = Long.hashCode(expectedH);
      assertEquals(expected, BitMixer.mixPhi(d));
    }
  }

  @Test
  public void testMixPhiLong(Random rnd) {
    long[] edgeCases = {
      0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 0x5555555555555555L, 0xAAAAAAAAAAAAAAAAL
    };
    for (long l : edgeCases) {
      long expectedH = l * PHI_C64;
      int expected = Long.hashCode(expectedH);
      assertEquals(expected, BitMixer.mixPhi(l));
    }
    for (int i = 0; i < 10000; i++) {
      long l = rnd.nextLong();
      long expectedH = l * PHI_C64;
      int expected = Long.hashCode(expectedH);
      assertEquals(expected, BitMixer.mixPhi(l));
    }
  }

  @Test
  public void testMixPhiObject(Random rnd) {
    assertEquals(0, BitMixer.mixPhi(null));

    int expectedH = "test".hashCode() * PHI_C32;
    int expected = expectedH ^ (expectedH >>> 16);
    assertEquals(expected, BitMixer.mixPhi("test"));

    for (int i = 0; i < 1000; i++) {
      int obj = rnd.nextInt();
      int h = Integer.hashCode(obj) * PHI_C32;
      int exp = h ^ (h >>> 16);
      assertEquals(exp, BitMixer.mixPhi((Object) obj));
    }
  }

  @Test
  public void testMix32AndMix64Determinism(Random rnd) {
    // Verify mix32 produces consistent, deterministic results
    for (int i = 0; i < 1000; i++) {
      int val = rnd.nextInt();
      assertEquals(BitMixer.mix32(val), BitMixer.mix32(val));
    }

    // Verify mix64 produces consistent, deterministic results
    for (int i = 0; i < 1000; i++) {
      long val = rnd.nextLong();
      assertEquals(BitMixer.mix64(val), BitMixer.mix64(val));
    }
  }
}
