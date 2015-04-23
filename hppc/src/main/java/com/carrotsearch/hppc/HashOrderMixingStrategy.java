package com.carrotsearch.hppc;

/**
 *  
 */
public interface HashOrderMixingStrategy extends Cloneable {
  public int newKeyMixer(int newContainerBufferSize);

  /**
   * @return Return a clone of this strategy. This should use a different mixing
   *         because cloned containers should have a different hash ordering.
   */
  public HashOrderMixingStrategy clone();
}
