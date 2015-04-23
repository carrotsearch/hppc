package com.carrotsearch.hppc;

/**
 * Factory methods to acquire the most common types of 
 * {@link HashOrderMixingStrategy}.
 */
public final class HashOrderMixing {
  private HashOrderMixing() {}

  /**
   * @see #deterministic()
   */
  private static final HashOrderMixingStrategy DETERMINISTIC = new HashOrderMixingStrategy() {
    @Override
    public int newKeyMixer(int newContainerBufferSize) {
      return (int) XorShiftRandom.next((long) newContainerBufferSize);
    }
    
    @Override
    public HashOrderMixingStrategy clone() {
      return this;
    }    
  };

  /**
   * Returns a randomized {@link HashOrderMixingStrategy} that issues unique
   * per-container seed. This minimizes the chances of hash distribution conflicts.
   */
  public static HashOrderMixingStrategy randomized() {
    return RandomizedHashOrderMixer.INSTANCE;
  }

  /**
   * A constant {@link HashOrderMixingStrategy}. This is useful if one needs to have
   * deterministic key distribution but wishes to control it manually.
   * 
   * Do not use the same constant for more than one container.
   */
  public static HashOrderMixingStrategy constant(final long seed) {
    return new HashOrderMixingStrategy() {
      @Override
      public int newKeyMixer(int newContainerBufferSize) {
        return (int) XorShiftRandom.next(seed ^ newContainerBufferSize);
      }

      @Override
      public HashOrderMixingStrategy clone() {
        return this;
      }
    };
  }

  /**
   * Deterministic {@link HashOrderMixingStrategy} will reorder keys depending
   * on the size of the container's buffer.
   * 
   * This is inherently unsafe with hash containers using linear conflict
   * addressing. The only use case when this can be useful is to count/ collect
   * unique keys.
   * 
   * @deprecated Permanently deprecated as a warning signal.
   */
  @Deprecated
  public static HashOrderMixingStrategy deterministic() {
    return DETERMINISTIC;
  }

  /**
   * This strategy does not change the hash order of keys at all. This
   * is inherently unsafe with hash containers using linear conflict 
   * addressing. The only use case when this can be useful is to count/ collect
   * unique keys.
   * 
   * @deprecated Permanently deprecated as a warning signal.
   */
  @Deprecated
  public static HashOrderMixingStrategy none() {
    return new HashOrderMixingStrategy() {
      @Override
      public int newKeyMixer(int newContainerBufferSize) {
        return 0;
      }

      @Override
      public HashOrderMixingStrategy clone() {
        return this;
      }
    };
  }
}
