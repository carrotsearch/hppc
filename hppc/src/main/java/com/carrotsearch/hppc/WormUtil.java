/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import java.util.Arrays;
import java.util.NoSuchElementException;

/** Utility methods for {@code Worm} Map and Set. */
class WormUtil {
  /**
   * The number of recursive move attempts per recursive call level. {@link
   * #RECURSIVE_MOVE_ATTEMPTS}[i] = max number of attempts at recursive level i. It must always end
   * with 0 attempts at the last level. Used by {@code KTypeVTypeWormMap#searchAndMoveBucket} when
   * trying to move entries recursively to free a bucket instead of enlarging the map. The more
   * attempts are allowed, the more the load factor increases, but the performance decreases. It is
   * a compromise between memory reduction and performance.
   */
  static final int[] RECURSIVE_MOVE_ATTEMPTS = {10, 1, 0};

  /**
   * Marks an entry at the end of a chain. This value is stored in the "next offset" of the entry.
   */
  static final int END_OF_CHAIN = 127;

  /**
   * Target load factor for the {@code KTypeVTypeWormMap#ensureCapacity(int)} method. The method
   * sets the map capacity according to the map size and this load factor. If the map cannot fit
   * within this capacity, it is enlarged and consequently the obtained load factor is low. So this
   * {@link #FIT_LOAD_FACTOR} must be chosen carefully to work in most cases.
   */
  static final float FIT_LOAD_FACTOR = 0.75f;

  /**
   * Adds a positive offset to the provided index, handling rotation around the circular array.
   *
   * @return The new index after addition.
   */
  static int addOffset(int index, int offset, int capacity) {
    assert checkIndex(index, capacity);
    assert Math.abs(offset) < END_OF_CHAIN : "offset=" + offset;
    return (index + offset) & (capacity - 1);
  }

  /**
   * Gets the offset between two indexes, handling rotation around the circular array.
   *
   * @return The positive offset between the two indexes.
   */
  static int getOffsetBetweenIndexes(int fromIndex, int toIndex, int capacity) {
    assert checkIndex(fromIndex, capacity);
    assert checkIndex(toIndex, capacity);
    return (toIndex - fromIndex) & (capacity - 1);
  }

  /** Maximum offset value. */
  static int maxOffset(int capacity) {
    return Math.min(capacity, END_OF_CHAIN) - 1;
  }

  /** Used for assertions. */
  static boolean checkIndex(int index, int capacity) {
    assert index >= 0 && index < capacity : "index=" + index + ", capacity=" + capacity;
    return true;
  }

  /**
   * Searches a free bucket by linear probing to the right.
   *
   * @param fromIndex The index to start searching from, inclusive.
   * @param range The maximum number of buckets to search, starting from index (included), up to
   *     index + range (excluded).
   * @param next The "next offset" array.
   * @param excludedIndex Optional index to exclude from the search; -1 if none.
   * @return The index of the next free bucket; or -1 if not found within the range.
   */
  static int searchFreeBucket(int fromIndex, int range, int excludedIndex, byte[] next) {
    assert checkIndex(fromIndex, next.length);
    assert range >= 0 && range <= maxOffset(next.length)
        : "range=" + range + ", maxOffset=" + maxOffset(next.length);
    if (range == 0) {
      return -1;
    }
    final int capacity = next.length;
    for (int index = fromIndex, toIndex = fromIndex + range; index < toIndex; index++) {
      int rolledIndex = index & (capacity - 1);
      if (next[rolledIndex] == 0 && rolledIndex != excludedIndex) {
        return rolledIndex;
      }
    }
    return -1;
  }

  /**
   * Finds the previous entry in the chain by linear probing to the left.
   *
   * <p>Note: Alternatively we could compute the hash index of the key, jump directly to the head of
   * the chain, and then follow the chain until we find the entry which next entry in the chain is
   * the provided one. But this alternative is slightly slower on average, even if the key hash code
   * is cached.
   *
   * @param entryIndex The index of the entry to start searching from. This method starts scanning
   *     at this index - 1, modulo array length since the array is circular.
   * @return The index of the previous entry in the chain.
   * @throws NoSuchElementException If the previous entry is not found (never happens if entryIndex
   *     is the index of a tail-of-chain entry, with next[entryIndex] &lt; 0).
   */
  static int findPreviousInChain(int entryIndex, byte[] next) {
    assert checkIndex(entryIndex, next.length);
    assert next[entryIndex] < 0;
    final int capacity = next.length;
    final int capacityMask = capacity - 1;
    for (int index = entryIndex - 1, toIndex = index - maxOffset(capacity);
        index > toIndex;
        index--) {
      int rolledIndex = index & capacityMask;
      int absNextOffset = Math.abs(next[rolledIndex]);
      int chainedIndex = (rolledIndex + absNextOffset) & capacityMask;
      if (chainedIndex == entryIndex) {
        // The entry at rolledIndex chains to the entry at entryIndex.
        assert absNextOffset != END_OF_CHAIN;
        return rolledIndex;
      }
    }
    throw new NoSuchElementException(
        "Previous entry not found (entryIndex="
            + entryIndex
            + ", next[entryIndex]="
            + next[entryIndex]
            + ")");
  }

  /**
   * Finds the last entry of a chain.
   *
   * @param index The index of an entry in the chain.
   * @param nextOffset next[index].
   * @param returnPrevious Whether to return the entry before the last.
   * @return The index of the last entry in the chain, or the entry before, depending on {@code
   *     returnPrevious}. Returns {@code -1} if the entry at index is the last entry of the chain
   *     and {@code returnPrevious} is true.
   */
  static int findLastOfChain(int index, int nextOffset, boolean returnPrevious, byte[] next) {
    assert checkIndex(index, next.length);
    assert nextOffset != 0 && Math.abs(nextOffset) <= END_OF_CHAIN : "nextOffset=" + nextOffset;
    assert nextOffset == next[index] : "nextOffset=" + nextOffset + ", next[index]=" + next[index];

    // Follow the entry chain for this bucket.
    final int capacity = next.length;
    if (nextOffset < 0) {
      nextOffset = -nextOffset;
    }
    int previousIndex = -1;
    while (nextOffset != END_OF_CHAIN) {
      previousIndex = index;
      index = addOffset(index, nextOffset, capacity); // Jump forward.
      nextOffset = -next[index]; // Next offsets are negative for tail-of-chain entries.
      assert nextOffset > 0 : "nextOffset=" + nextOffset;
    }
    return returnPrevious ? previousIndex : index;
  }

  enum PutPolicy {
    /** Always put. Create new key if absent, or replace existing value if present. */
    NEW_OR_REPLACE,
    /** Always put and it is guaranteed that the key is absent. */
    NEW_GUARANTEED,
    /** Put only if the key is absent. Don't replace existing value. */
    NEW_ONLY_IF_ABSENT,
  }

  /**
   * Efficient immutable set of excluded indexes (immutable int set of expected small size).
   *
   * <p>Used when searching for a free bucket and attempting to move tail-of-chain entries
   * recursively. We must not move the entry chains for which we want to find a free bucket. So
   * {@link ExcludedIndexes} is immutable and can be stacked with {@link #union(ExcludedIndexes)}
   * during recursive calls. In addition the initial {@link #NONE} is a constant and does not stack
   * as it overrides {@link #union(ExcludedIndexes)}.
   */
  abstract static class ExcludedIndexes {

    static final ExcludedIndexes NONE =
        new ExcludedIndexes() {
          @Override
          ExcludedIndexes union(ExcludedIndexes excludedIndexes) {
            return excludedIndexes;
          }

          @Override
          boolean isIndexExcluded(int index) {
            return false;
          }
        };

    static ExcludedIndexes fromChain(int index, byte[] next) {
      int nextOffset = Math.abs(next[index]);
      assert nextOffset != 0 : "nextOffset=0";
      return nextOffset == END_OF_CHAIN
          ? new SingletonExcludedIndex(index)
          : new MultipleExcludedIndexes(index, nextOffset, next);
    }

    ExcludedIndexes union(ExcludedIndexes excludedIndexes) {
      return new UnionExcludedIndexes(this, excludedIndexes);
    }

    abstract boolean isIndexExcluded(int index);
  }

  static class SingletonExcludedIndex extends ExcludedIndexes {

    final int excludedIndex;

    SingletonExcludedIndex(int excludedIndex) {
      this.excludedIndex = excludedIndex;
    }

    @Override
    boolean isIndexExcluded(int index) {
      return index == excludedIndex;
    }
  }

  static class MultipleExcludedIndexes extends ExcludedIndexes {

    final int[] excludedIndexes;
    final int size;

    MultipleExcludedIndexes(int index, int nextOffset, byte[] next) {
      assert index >= 0 && index < next.length : "index=" + index + ", next.length=" + next.length;
      assert nextOffset > 0 && nextOffset < END_OF_CHAIN : "nextOffset=" + nextOffset;
      int[] excludedIndexes = new int[8];
      int size = 0;
      boolean shouldSort = false;
      excludedIndexes[size++] = index;
      do {
        int nextIndex = addOffset(index, nextOffset, next.length);
        if (nextIndex < index) {
          // Rolling on the circular buffer. We will need to sort to keep a sorted list of indexes.
          shouldSort = true;
        }
        assert nextIndex >= 0 && nextIndex < next.length
            : "nextIndex=" + index + ", next.length=" + next.length;
        if (size == excludedIndexes.length) {
          excludedIndexes = Arrays.copyOf(excludedIndexes, size * 2);
        }
        excludedIndexes[size++] = index = nextIndex;
        nextOffset = Math.abs(next[index]);
        assert nextOffset > 0 : "nextOffset=" + nextOffset;
      } while (nextOffset != END_OF_CHAIN);
      if (shouldSort) {
        Arrays.sort(excludedIndexes, 0, size);
      }
      this.excludedIndexes = excludedIndexes;
      this.size = size;
    }

    @Override
    boolean isIndexExcluded(int index) {
      return Arrays.binarySearch(excludedIndexes, 0, size, index) >= 0;
    }
  }

  static class UnionExcludedIndexes extends ExcludedIndexes {

    final ExcludedIndexes left;
    final ExcludedIndexes right;

    UnionExcludedIndexes(ExcludedIndexes left, ExcludedIndexes right) {
      this.left = left;
      this.right = right;
    }

    @Override
    boolean isIndexExcluded(int index) {
      return left.isIndexExcluded(index) || right.isIndexExcluded(index);
    }
  }
}
