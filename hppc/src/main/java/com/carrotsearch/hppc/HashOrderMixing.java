package com.carrotsearch.hppc;

public final class HashOrderMixing {
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

  private HashOrderMixing() {}

  public static HashOrderMixingStrategy deterministic() {
    return DETERMINISTIC;
  }

  public static HashOrderMixingStrategy randomized() {
    return RandomizedHashOrderMixer.INSTANCE;
  }

  /**
   * This strategy does not change the hash order of keys at all. This
   * is inherently unsafe with hash containers using linear conflict 
   * addressing. The only use case when this can be useful is to count/ collect
   * unique keys.
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
