package com.carrotsearch.hppc;

import java.util.Arrays;
import java.util.concurrent.Callable;

import com.carrotsearch.hppc.hash.MurmurHash3;

final class HashContainerUtils
{
    /**
     * Maximum capacity for an array that is of power-of-two size and still
     * allocable in Java (not a negative int).  
     */
    final static int MAX_CAPACITY = 0x80000000 >>> 1;
    
    /**
     * Minimum capacity for a hash container.
     */
    final static int MIN_CAPACITY = 4;

    /**
     * Default capacity for a hash container.
     */
    final static int DEFAULT_CAPACITY = 16;

    /**
     * Default load factor.
     */
    final static float DEFAULT_LOAD_FACTOR = 0.75f;
    
    
    public  final static int BLANK_ARRAY_SIZE_IN_BIT_SHIFT = 10;
    
    /**
     * Batch blanking array size
     */
    public final static int BLANK_ARRAY_SIZE = 1 << BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
    
    /**
     * Batch blanking array with Object nulls
     */
    public final static Object[] BLANKING_OBJECT_ARRAY = new Object[BLANK_ARRAY_SIZE];

    /**
     * Computer static perturbations table.
     */
    final static int [] PERTURBATIONS = new Callable<int[]>() {
        public int[] call() {
            int [] result = new int [32];
            for (int i = 0; i < result.length; i++) {
                result[i] = MurmurHash3.hash(17 + i);
            }
            return result;
        }
    }.call();

    /**
     * Round the capacity to the next allowed value. 
     */
    static int roundCapacity(int requestedCapacity)
    {
        if (requestedCapacity > MAX_CAPACITY)
            return MAX_CAPACITY;

        return Math.max(MIN_CAPACITY, BitUtil.nextHighestPowerOfTwo(requestedCapacity));
    }

    /**
     * Return the next possible capacity, counting from the current buffers'
     * size.
     */
    static int nextCapacity(int current)
    {
        assert current > 0 && Long.bitCount(current) == 1 : "Capacity must be a power of two.";

        if (current < MIN_CAPACITY / 2)
        { 
            current = MIN_CAPACITY / 2;
        }

        current <<= 1;
        if (current < 0) 
        {
            throw new RuntimeException("Maximum capacity exceeded.");
        }

        return current;
    }
    
    /**
     * Method to blank Object[] array to "null"
     * Either the size is < HashContainerUtils.BLANK_ARRAY_SIZE, or 
     * it must be power of 2 sized, like in HashMaps or Sets.
     * @param objectArray
     */
    static <T> void blankPowerOf2ObjectArray(T[] objectArray) {
        
        if (objectArray.length < BLANK_ARRAY_SIZE)
        {
           Arrays.fill(objectArray, null); // Help the GC.
        } 
        else 
        {
            //if big enough, blank by batch of BLANK_ARRAY_SIZE.
            final int nbChunks = objectArray.length >> BLANK_ARRAY_SIZE_IN_BIT_SHIFT;
            
            for (int ii = 0; ii < nbChunks; ii++) {
                
                System.arraycopy(HashContainerUtils.BLANKING_OBJECT_ARRAY, 0, 
                                 objectArray, ii << BLANK_ARRAY_SIZE_IN_BIT_SHIFT, 
                                 BLANK_ARRAY_SIZE);
            } //end for
        }
   }
}
