package com.carrotsearch.hppc;

import org.junit.Test;

public class HashAvalancheTest
{
    @Test
    public void testAvalanche()
    {
      int v = 10000000;
        IntOpenHashSet target = new IntOpenHashSet(v, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize, double loadFactor) {
            super.allocateBuffers(arraySize, loadFactor);
            System.out.println("#> " + arraySize);
          }
        };

        int expandAtCount = HashContainers.expandAtCount(target.keys.length, 0.9);
        int fillUntil = expandAtCount - 100000;
        
        System.out.println(expandAtCount + " " + fillUntil);
        System.out.println(HashContainers.minBufferSize(fillUntil, 0.9));
        
        IntOpenHashSet source = new IntOpenHashSet(v, 0.9);
        
        int k = 0;
        while (source.size() < expandAtCount - 1) {
          source.add(k++);
        }
        System.out.println("First.");

        while (target.size() < fillUntil) {
          target.add(k++);
        }
        System.out.println("Second.");
        
        System.out.println(source.keys.length);
        System.out.println(target.keys.length);
        System.out.println(target.resizeAt);
        System.out.println("--");

        for (int i = 0; i < source.keys.length; i++) {
          target.add(source.keys[i]);
          if ((i % 200) == 0 && source.keys.length == target.keys.length) {
            System.out.println(i);
          }
        }
    }
}
