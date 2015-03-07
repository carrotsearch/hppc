package com.carrotsearch.hppc;

import java.util.concurrent.Callable;

import com.carrotsearch.hppc.hash.MurmurHash3;

final class HashContainerUtils
{
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
