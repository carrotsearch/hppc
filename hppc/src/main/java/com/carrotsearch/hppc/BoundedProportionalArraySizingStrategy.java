package com.carrotsearch.hppc;

import java.util.ArrayList;

/**
 * Array resizing proportional to the current buffer size, optionally kept within the
 * given minimum and maximum growth limits. Java's {@link ArrayList} uses:
 * <pre>
 * minGrow = 1
 * maxGrow = Integer.MAX_VALUE (unbounded)
 * growRatio = 1.5f
 * </pre>
 */
public final class BoundedProportionalArraySizingStrategy 
    implements ArraySizingStrategy
{   
    /** 
     * Used by {@link ArrayList} internally to account for reference sizes. 
     */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE; 

    /** Minimum grow count. */
    public final static int DEFAULT_MIN_GROW_COUNT = 10;

    /** Maximum grow count (unbounded). */
    public final static int DEFAULT_MAX_GROW_COUNT = MAX_ARRAY_SIZE;

    /** Default resize is by half the current buffer's size. */
    public final static float DEFAULT_GROW_RATIO = 1.5f;

    /** Minimum number of elements to grow, if capacity exceeded. */
    public final int minGrowCount;

    /** Maximum number of elements to grow, if capacity exceeded. */
    public final int maxGrowCount;

    /** 
     * The current buffer length is multiplied by this ratio to get the
     * first estimate for the new size. To double the size of the current
     * buffer, for example, set to <code>2</code>. 
     */
    public final float growRatio;
    
    /**
     * Create the default sizing strategy.
     */
    public BoundedProportionalArraySizingStrategy()
    {
        this(DEFAULT_MIN_GROW_COUNT, DEFAULT_MAX_GROW_COUNT, DEFAULT_GROW_RATIO);
    }

    /**
     * Create the sizing strategy with custom policies.
     */
    public BoundedProportionalArraySizingStrategy(int minGrow, int maxGrow, float ratio)
    {
        assert minGrow >= 1 : "Min grow must be >= 1.";
        assert maxGrow >= minGrow : "Max grow must be >= min grow.";
        assert ratio >= 1f : "Growth ratio must be >= 1 (was " + ratio + ").";

        this.minGrowCount = minGrow;
        this.maxGrowCount = maxGrow;
        this.growRatio = ratio - 1.0f;
    }

    /**
     * Grow according to {@link #growRatio}, {@link #minGrowCount} and {@link #maxGrowCount}.
     */
    public int grow(int currentBufferLength, int elementsCount, int expectedAdditions)
    {
        long growBy = (long) ((long) currentBufferLength * growRatio);
        growBy = Math.max(growBy, minGrowCount);
        growBy = Math.min(growBy, maxGrowCount);
        long growTo = Math.min(MAX_ARRAY_SIZE, growBy + currentBufferLength);
        
        long newSize = Math.max((long) elementsCount + expectedAdditions, growTo); 

        if (newSize > MAX_ARRAY_SIZE) {
            throw new AssertionError(
                "Cannot resize beyond " + MAX_ARRAY_SIZE + 
                " (" + (elementsCount + expectedAdditions) + ")");
        }

        return (int) newSize;
    }

    /**
     * No specific requirements in case of this strategy - the argument is returned.
     */
    public int round(int capacity)
    {
        assert capacity >= 0 : "Capacity must be a positive number.";
        return capacity;
    }
}
