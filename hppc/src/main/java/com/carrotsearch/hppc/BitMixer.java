package com.carrotsearch.hppc;

/**
 * Key hash bit mixing.
 */
final class BitMixer {
  // Don't bother mixing very small key domains much.
  static int mix (byte key)   { return key * 0x85ebca6b; }
  static int mix0(byte key)   { return key * 0x85ebca6b; }

  static int mix (short key)  { int k = key * 0x85ebca6b; return k ^= k >>> 13; }
  static int mix0(short key)  { int k = key * 0x85ebca6b; return k ^= k >>> 13; }
  static int mix (char  key)  { int k = key * 0x85ebca6b; return k ^= k >>> 13; }
  static int mix0(char  key)  { int k = key * 0x85ebca6b; return k ^= k >>> 13; }

  static int mix (int key)    { return murmurHash3(key); }
  static int mix0(int key)    { return murmurHash3(key); }

  static int mix (float key)  { return murmurHash3(Float.floatToIntBits(key)); }
  static int mix0(float key)  { return murmurHash3(Float.floatToIntBits(key)); }

  static int mix (double key) { long v = Double.doubleToLongBits(key); return murmurHash3((int)((v >>> 32) ^ v)); }
  static int mix0(double key) { long v = Double.doubleToLongBits(key); return murmurHash3((int)((v >>> 32) ^ v)); }

  static int mix (Object key) { return murmurHash3(key.hashCode()); }
  static int mix0(Object key) { return key == null ? 0 : murmurHash3(key.hashCode()); }

  static int mix (byte key, int seed)  { return mix(key ^ seed); }
  static int mix0(byte key, int seed)  { return mix0(key ^ seed); }

  static int mix (short key, int seed) { return mix(key ^ seed); }
  static int mix0(short key, int seed) { return mix0(key ^ seed); }
  static int mix (char key, int seed)  { return mix(key ^ seed); }
  static int mix0(char key, int seed)  { return mix0(key ^ seed); }

  static int mix (int key, int seed)   { return mix0(key ^ seed); }
  static int mix0(int key, int seed)   { return mix0(key ^ seed); }

  static int mix (float key, int seed)  { return murmurHash3(Float.floatToIntBits(key) ^ seed); }
  static int mix0(float key, int seed)  { return murmurHash3(Float.floatToIntBits(key) ^ seed); }

  static int mix (double key, int seed)  { long v = Double.doubleToLongBits(key); return murmurHash3((int)((v >>> 32) ^ v) ^ seed); }
  static int mix0(double key, int seed)  { long v = Double.doubleToLongBits(key); return murmurHash3((int)((v >>> 32) ^ v) ^ seed); }

  static int mix (Object key, int seed)  { return murmurHash3(key.hashCode() ^ seed); }
  static int mix0(Object key, int seed)  { return key == null ? 0 : murmurHash3(key.hashCode() ^ seed); }

  /** */
  private static int murmurHash3(int k) {
    k ^= k >>> 16;
    k *= 0x85ebca6b;
    k ^= k >>> 13;
    k *= 0xc2b2ae35;
    k ^= k >>> 16;
    return k;
  }
}
