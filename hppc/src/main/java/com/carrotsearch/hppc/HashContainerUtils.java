package com.carrotsearch.hppc;

import java.util.concurrent.Callable;

import com.carrotsearch.hppc.hash.MurmurHash3;

final class HashContainerUtils
{
    /**
     * Maximum capacity for an array that is of power-of-two size and still
     * allocable in Java (not a negative int).  
     */
    final static int MAX_ARRAY_SIZE = 0x80000000 >>> 1;

    /**
     * By default a hash container will store this many elements 
     * without resizing.
     */
    final static int DEFAULT_CAPACITY = 8;

    /**
     * Default load factor.
     */
    final static float DEFAULT_LOAD_FACTOR = 0.75f;

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
}
