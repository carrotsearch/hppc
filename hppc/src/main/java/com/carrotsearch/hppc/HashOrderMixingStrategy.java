package com.carrotsearch.hppc;

/**
 * Hash order mixing strategy implementations should provide an, ideally, 
 * random integer that is later XORed with the hash of a given key before
 * the slot lookup in associative arrays.
 * 
 * <p>
 * Why good hash mixing is important is explained in the 
 * <a href="{@docRoot}/overview-summary.html#scattervshash">differences between hash 
 * and scatter sets</a>.
 * </p>
 * 
 * @see ObjectHashSet#hashKey
 * @see ObjectObjectHashMap#hashKey
 * 
 * @see HashOrderMixing
 */
public interface HashOrderMixingStrategy extends Cloneable {
  /**
   * A new key mixer value. The value can be derived from the new buffer size of the
   * container, but preferably should be random and unique. 
   * 
   * @param newContainerBufferSize
   */
  public int newKeyMixer(int newContainerBufferSize);

  /**
   * @return Return a clone of this strategy. This should use a different mixing
   *         because cloned containers should have a different hash ordering.
   */
  public HashOrderMixingStrategy clone();
}
