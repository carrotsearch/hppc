package com.carrotsearch.hppc;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.carrotsearch.hppc.cursors.IntCursor;

public class HashCollisionsClusteringTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSetClusteringOnRehash()
    {
        IntOpenHashSet source = new IntOpenHashSet(0, 0.9d);
        for (int i = 1250000; i-- != 0;) {
          source.add(i);
        }

        IntOpenHashSet target = new IntOpenHashSet(0, 0.9d);
        int i = 0;
        long start = System.currentTimeMillis();
        long deadline = start + TimeUnit.SECONDS.toMillis(3);
        for (IntCursor c : source) {
          target.add(c.value);
          if ((i++ % 1000) == 0) {
            System.out.println("Added keys: " + i + " in " + (System.currentTimeMillis() - start) + " ms.");
            if (System.currentTimeMillis() >= deadline) {
              fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
            }
          }
        }
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
    
    /** */
    @Test 
    public void testHashSetClusteringAtFront()
    {
        int keys = 500000;
        IntOpenHashSet target = new IntOpenHashSet(keys, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            System.out.println("Rehashed to: " + arraySize);
          }
        };

        int expandAtCount = HashContainers.expandAtCount(target.keys.length - 1, 0.9);
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
        long start = System.currentTimeMillis();
        long deadline = start + TimeUnit.SECONDS.toMillis(3);
        int i = 0;
        for (IntCursor c : source) {
          target.add(c.value);
          if ((i++ % 1000) == 0) {
            if (source.keys.length == target.keys.length) {
              System.out.println("Added keys: " + i + " in " + (System.currentTimeMillis() - start) + " ms.");
            }
            if (System.currentTimeMillis() >= deadline) {
              fail("Takes too long, something is wrong. Added " + i + " keys out of " + source.size());
            }
          }
        }
    }

    /** */
    @Test 
    public void testHashSetClusteringAtFront2()
    {
        int keys = 100000;
        int expected = keys * 5;
        IntOpenHashSet target = new IntOpenHashSet(expected, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            System.out.println("[Rehashed to: " + arraySize + "]");
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
          System.out.println(String.format(Locale.ROOT,
              "Keys: %7d, %5d ms.: %s",
              i, 
              e - s,
              visualizeDistribution(target, 80)));

          if (System.currentTimeMillis() > deadline) {
            fail("Takes too long, something is wrong. Added " + i + " batches.");
          }
        }
    }

    protected String visualizeDistribution(IntOpenHashSet target, int lineLength) {
      int bucketSize = target.keys.length / lineLength;
      int [] counts = new int [lineLength];
      for (int x = 0; x < target.keys.length; x++) {
        if (target.keys[x] != 0) {
          counts[Math.min(counts.length - 1, x / bucketSize)]++;
        }
      }
      
      int max = counts[0];
      for (int x = 0; x < counts.length; x++) {
        max = Math.max(max, counts[x]);
      }

      StringBuilder b = new StringBuilder();
      final char [] chars = ".0123456789".toCharArray();
      for (int x = 0; x < counts.length; x++) {
        b.append(chars[(counts[x] * 10 / max)]);
      }
      return b.toString();
    }    
}
