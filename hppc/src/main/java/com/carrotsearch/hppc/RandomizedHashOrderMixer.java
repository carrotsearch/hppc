package com.carrotsearch.hppc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Randomized hash order. Does not guarantee deterministic hash ordering between
 * runs. In fact, it tries hard to avoid such guarantee.
 */
public final class RandomizedHashOrderMixer implements HashOrderMixingStrategy {
  public final static RandomizedHashOrderMixer INSTANCE = new RandomizedHashOrderMixer();

  protected final AtomicLong seedMixer;

  public RandomizedHashOrderMixer() {
    this(Containers.randomSeed64());
  }

  public RandomizedHashOrderMixer(long seed) {
    seedMixer = new AtomicLong(seed);
  }

  @Override
  public int newKeyMixer(int newContainerBufferSize) {
    return (int) BitMixer.mix64(seedMixer.incrementAndGet());
  }

  @Override
  public HashOrderMixingStrategy clone() {
    return this;
  }
}
