package com.carrotsearch.hppc;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.hppc.cursors.IntCursor;

import static org.junit.Assert.*;

public class HashCollisionsClusteringTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSetClusteringOnRehash()
    {
        IntOpenHashSet a = new IntOpenHashSet();
        for (int i = 10000000; i-- != 0;) {
            a.add(i);
        }
        IntOpenHashSet b2 = new IntOpenHashSet();
        b2.addAll(a);
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashMapClusteringOnRehash()
    {
        IntIntOpenHashMap a = new IntIntOpenHashMap();
        for (int i = 10000000; i-- != 0;) {
            a.put(i, 0);
        }
        IntIntOpenHashMap b2 = new IntIntOpenHashMap();
        b2.putAll(a);
    }
    
    /** @see "http://issues.carrot2.org/browse/HPPC-103" */
    @Test @Ignore
    public void testHashSetClusteringAtFront()
    {
        int keys = 10000000;
        IntOpenHashSet target = new IntOpenHashSet(keys, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize, double loadFactor) {
            super.allocateBuffers(arraySize, loadFactor);
            System.out.println("Rehashed to: " + arraySize);
          }
        };

        int expandAtCount = HashContainers.expandAtCount(target.keys.length, 0.9);
        int fillUntil = expandAtCount - 100000;
        
        IntOpenHashSet source = new IntOpenHashSet(keys, 0.9);
        int unique = 0;
        while (source.size() < expandAtCount - 1) {
          source.add(unique++);
        }
        System.out.println("Source filled up.");

        while (target.size() < fillUntil) {
          target.add(unique++);
        }
        System.out.println("Target filled up.");

        assertEquals(source.keys.length, target.keys.length);
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
        for (int i = 0; i < source.keys.length; i++) {
          target.add(source.keys[i]);
          if ((i % 1000) == 0) {
            if (source.keys.length == target.keys.length) {
              System.out.println("Added keys: " + i);
            }
            if (System.currentTimeMillis() >= deadline) {
              fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
            }
          }
        }
    }

    /** @see "http://issues.carrot2.org/browse/HPPC-103" */
    @Test @Ignore
    public void testHashSetClusteringAtFront2()
    {
        int keys = 100000;
        int expected = keys * 5;
        IntOpenHashSet target = new IntOpenHashSet(expected, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize, double loadFactor) {
            super.allocateBuffers(arraySize, loadFactor);
            System.out.println("#> reallocate to " + arraySize);
          }
        };

        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
        IntOpenHashSet source = new IntOpenHashSet(expected, 0.9);
        int unique = 0;
        for (int i = 0; i < 200; i++) {
          source.clear();
          while (source.size() < keys) {
            source.add(unique++);
          }

          long s = System.currentTimeMillis();
          int firstSubsetOfKeys = 5000;
          for (IntCursor c : source) {
            target.add(c.value);
            if (firstSubsetOfKeys-- == 0) break;
          }
          long e = System.currentTimeMillis();
          System.out.println("> " + i + " " + (e - s) + "ms.");
          
          if (System.currentTimeMillis() > deadline) {
            fail("Takes too long, something is wrong. Added " + i + " batches.");
          }
        }
    }    
}
