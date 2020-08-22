package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.HashContainers.iterationIncrement;

final class KTypeKeyArrayTraversal<KType> {
  private final KType[] keys;
  private final int increment;
  private final int mask;
  private final boolean hasEmptyKey;

  private int index;
  private int slot;

  KTypeKeyArrayTraversal(KType[] keys, int seed, int mask, boolean hasEmptyKey) {
    this.keys = keys;
    this.mask = mask;
    this.increment = iterationIncrement(seed);
    this.slot = seed & mask;
    this.hasEmptyKey = hasEmptyKey;
  }

  public int nextSlot() {
    while (index <= mask) {
      index++;
      slot = (slot + increment) & mask;
      if (!Intrinsics.<KType>isEmpty(Intrinsics.<KType>cast(keys[slot]))) {
        return slot;
      }
    }

    if (index == mask + 1 && hasEmptyKey) {
      return index++;
    }

    return -1;
  }
}
