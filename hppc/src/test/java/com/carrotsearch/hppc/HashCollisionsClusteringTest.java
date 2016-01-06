package com.carrotsearch.hppc;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.randomizedtesting.RandomizedTest;

public class HashCollisionsClusteringTest extends RandomizedTest
{
    private static boolean debugging = false;

    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSetClusteringOnRehash()
    {
        IntHashSet source = new IntHashSet(0, 0.9d);
        for (int i = 1250000; i-- != 0;) {
          source.add(i);
        }

        IntHashSet target = new IntHashSet(0, 0.9d);
        int i = 0;
        long start = System.currentTimeMillis();
        long deadline = start + TimeUnit.SECONDS.toMillis(3);
        for (IntCursor c : source) {
          target.add(c.value);
          if ((i++ % 5000) == 0) {
            System.out.println(String.format(Locale.ROOT,
                "Keys: %7d, %5d ms.",
                i, 
                System.currentTimeMillis() - start));

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
        IntIntHashMap a = new IntIntHashMap();
        for (int i = 10000000; i-- != 0;) {
            a.put(i, 0);
        }
        IntIntHashMap b2 = new IntIntHashMap();
        b2.putAll(a);
    }
    
    /** */
    @Test 
    public void testHashSetClusteringAtFront()
    {
        int keys = 500000;
        IntHashSet target = new IntHashSet(keys, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            System.out.println("Rehashed to: " + arraySize);
          }
        };

        int expandAtCount = HashContainers.expandAtCount(target.keys.length - 1, 0.9);
        int fillUntil = expandAtCount - 100000;

        IntHashSet source = new IntHashSet(keys, 0.9);
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
        long deadline = start + TimeUnit.SECONDS.toMillis(5);
        int i = 0;
        for (IntCursor c : source) {
          target.add(c.value);
          if ((i++ % 5000) == 0) {
            if (source.keys.length == target.keys.length) {
              System.out.println(String.format(Locale.ROOT,
                  "Keys: %7d, %5d ms.: %s",
                  i, 
                  System.currentTimeMillis() - start,
                  debugging ? target.visualizeKeyDistribution(80) : "--"));
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
        IntHashSet target = new IntHashSet(expected, 0.9) {
          @Override
          protected void allocateBuffers(int arraySize) {
            super.allocateBuffers(arraySize);
            System.out.println("[Rehashed to: " + arraySize + "]");
          }
        };

        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);
        IntHashSet source = new IntHashSet(expected, 0.9);
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
              "Keys: %7d, %5d ms. (%5d): %s",
              i, 
              e - s,
              deadline - e,
              debugging ? target.visualizeKeyDistribution(80) : "--"));

          if (System.currentTimeMillis() > deadline) {
            fail("Takes too long, something is wrong. Added " + i + " batches.");
          }
        }
    }
}
