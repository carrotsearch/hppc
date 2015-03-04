package com.carrotsearch.hppc;

import java.util.Locale;

import org.junit.Test;

import com.carrotsearch.hppc.hash.MurmurHash3;

public class HashCollisionsCornerCaseTest
{
    /** @see "http://issues.carrot2.org/browse/HPPC-80" */
    @Test
    public void testHashSets()
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
    public void testHashMaps()
    {
        IntIntOpenHashMap a = new IntIntOpenHashMap();
        for (int i = 10000000; i-- != 0;) {
            a.put(MurmurHash3.hash(i), 0);
        }
        
        System.out.println(String.format(Locale.ROOT, 
            "%4s | %35s | %35s",
            "",
            "",
            binary(a.allocated.length - 1)));
 
        for (int i = 0; i < a.keys.length; i++) {
          if (!a.allocated[i]) continue;
          if (i > 1024) break;
          System.out.println(String.format(Locale.ROOT, 
              "%4d | %s | %s",
              i,
              binary(a.keys[i]),
              binary(a.initialBucket(a.keys[i]))
              ));
        }
        
        IntIntOpenHashMap b2 = new IntIntOpenHashMap();
        //IntIntOpenHashMap b2 = a.clone();
        int v = 10000000;
        // for (int x = b2.allocated.length, v = 10000000; b2.allocated.length == x;) {
        while (b2.allocated.length < a.allocated.length) {
          b2.put(v++, 0);
        }

        System.out.println("Start...");
        b2.debug = true;
        b2.putAll(a);
        System.out.println("End: " + b2.size());
    }
    
    public static String binary(int v) {
      StringBuilder b = new StringBuilder();
      for (int p = 0; p < 4; p++) {
        if (p > 0) b.append(' ');
        for (int i = 0; i < 8; i++) {
          b.append((v & 0x80000000) != 0 ? '1' : '0'); 
          v <<= 1;
        }
      }
      return b.toString();
    }

}
