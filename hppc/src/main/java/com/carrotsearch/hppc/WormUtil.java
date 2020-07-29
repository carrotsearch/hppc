package com.carrotsearch.hppc;

import java.util.Arrays;

//import static com.carrotsearch.hppc.KTypeVTypeWormMap.*;

/**
 * Java hash for primitives.
 * <p/>stdHash() is different from hash() because we want <b>standard Java</b> implementations.
 * @author broustant
 */
public class WormUtil
{
    private static final int END_OF_CHAIN = 127;
    private static final boolean DEBUG_ENABLED = false;

    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(char c) {
        return hash((short) c);
    }

    /**
     * Hashes a short. Improves distribution for Map or Set.
     */
    public static int hash(short s) {
        s ^= (s >>> 10) ^ (s >>> 6);
        return s ^ (s >>> 4) ^ (s >>> 2);
    }

    /**
     * Hashes an int. Improves distribution for Map or Set.
     */
    public static int hash(int i) {
        int h = i * -1640531527;
        return h ^ h >> 16;
    }

    /**
     * Hashes a long. Improves distribution for Map or Set.
     */
    public static int hash(long l)
    {
        return hash((int) ((l >>> 32) ^ l));
    }

    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(Object o) {
        return hash(o.hashCode());
    }

    /**
     * Adds a positive offset to the provided index, handling rotation around the circular array.
     *
     * @return The new index after addition.
     */
    static int addOffset(int index, int offset, byte[] next) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
            assert offset > 0 && offset < END_OF_CHAIN : "offset=" + offset;
        }
        index += offset;
        while (index >= next.length) {
            index -= next.length;
        }
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
        }
        return index;
    }

    /**
     * Subtracts a positive offset to the provided index, handling rotation around the circular array.
     *
     * @return The new index after subtraction.
     */
    static int subtractOffset(int index, int offset, byte[] next) {
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
            assert offset > 0 && offset < END_OF_CHAIN : "offset=" + offset;
        }
        index -= offset;
        while (index < 0) {
            index += next.length;
        }
        if (DEBUG_ENABLED) {
            assert checkIndex(index, next);
        }
        return index;
    }

    static boolean checkIndex(int index, byte[] next) {
        assert index >= 0 && index < next.length : "index=" + index + ", array length=" + next.length;
        return true;
    }

    /**
     * Efficient immutable set of excluded indexes (immutable int set of expected small size).
     * <p/>Used when searching for a free bucket and attempting to move tail-of-chain entries
     * recursively. We must not move the entry chains for which we want to find a free bucket.
     * So {@link ExcludedIndexes} is immutable and can be stacked with {@link #union(ExcludedIndexes)}
     * during recursive calls. In addition the initial {@link #NONE} is a constant and does not stack
     * as it overrides {@link #union(ExcludedIndexes)}.
     */
    static abstract class ExcludedIndexes {

        static final ExcludedIndexes NONE = new ExcludedIndexes() {
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
            if (DEBUG_ENABLED) {
                assert nextOffset != 0 : "nextOffset=0";
            }
            return nextOffset == END_OF_CHAIN ? new SingletonExcludedIndex(index)
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
            if (DEBUG_ENABLED) {
                assert index >= 0 && index < next.length : "index=" + index + ", next.length=" + next.length;
                assert nextOffset > 0 && nextOffset < END_OF_CHAIN : "nextOffset=" + nextOffset;
            }
            int[] excludedIndexes = new int[8];
            int size = 0;
            boolean shouldSort = false;
            excludedIndexes[size++] = index;
            do {
                int nextIndex = addOffset(index, nextOffset, next);
                if (nextIndex < index) {
                    // Rolling on the circular buffer. We will need to sort to keep a sorted list of indexes.
                    shouldSort = true;
                }
                if (DEBUG_ENABLED) {
                    assert nextIndex >= 0 && nextIndex < next.length : "nextIndex=" + index + ", next.length=" + next.length;
                }
                if (size == excludedIndexes.length) {
                    excludedIndexes = Arrays.copyOf(excludedIndexes, size * 2);
                }
                excludedIndexes[size++] = index = nextIndex;
                nextOffset = Math.abs(next[index]);
                if (DEBUG_ENABLED) {
                    assert nextOffset > 0 : "nextOffset=" + nextOffset;
                }
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
