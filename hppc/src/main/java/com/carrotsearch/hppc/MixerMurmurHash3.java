package com.carrotsearch.hppc;

/**
 * Key hash mixing based on the final step of 
 * <a href="http://en.wikipedia.org/wiki/MurmurHash">MurmurHash3</a>.
 */
final class MixerMurmurHash3 {
  /**
   * Redistributes bits of an integer.
   */
  static int mix(int k) {
    k ^= k >>> 16;
    k *= 0x85ebca6b;
    k ^= k >>> 13;
    k *= 0xc2b2ae35;
    k ^= k >>> 16;
    return k;
  }

  /**
   * Redistributes bits of a long. We need an int offset into an array so we
   * just reuse the int mix. Java arrays can't be large enough to require that
   * large space anyway (we'd need to truncate).
   */
  static int mix(long v) {
    return mix((int) ((v >>> 32) ^ v));
  }
}
