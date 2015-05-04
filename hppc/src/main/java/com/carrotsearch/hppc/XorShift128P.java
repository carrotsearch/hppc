package com.carrotsearch.hppc;

import java.util.Random;

/**
 * A fast pseudo-random number generator. For simplicity, we do not implement all of {@link Random} methods.
 * 
 * @see "http://xorshift.di.unimi.it/"
 * @see "http://xorshift.di.unimi.it/xorshift128plus.c"
 */
public class XorShift128P {
  /*
   * 128 bits of state.
   */
  private long state0, state1;

  public XorShift128P(long seed) {
    state0 = notZero(BitMixer.mix64(seed));
    state1 = notZero(BitMixer.mix64(seed + 1));
  }
  
  public XorShift128P() {
    this(Containers.randomSeed64());
  }

  public long nextLong() {
    long s1 = state0;
    long s0 = state1;
    state0 = s0;
    s1 ^= s1 << 23;
    return (state1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;
  }

  public int nextInt() {
    return (int) nextLong();
  }

  private static long notZero(long value) {
    return value == 0 ? 0xdeadbeefbabeL : value;
  }

  public int nextInt(int bound) {
    if (bound <= 0) {
      throw new IllegalArgumentException();
    }

    int r = (nextInt() >>> 1);
    int m = bound - 1;
    if ((bound & m) == 0) {
      r = (int)((bound * (long) r) >> 31);
    } else {
      for (int u = r;
           u - (r = u % bound) + m < 0;
           u = nextInt() >>> 1) {
      }
    }

    return r;
  }
}
