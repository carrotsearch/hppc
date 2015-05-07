package com.carrotsearch.hppc.examples;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.carrotsearch.hppc.HashOrderMixing;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;

/**
 * Explains the difference between scatter and hash containers.
 */
@SuppressWarnings("deprecation")
public class HppcExample_006_HashAndScatterMaps {
  @Test
  public void scatterVsHashDifference() throws Exception {
    int key;

    /*
     * To understand the difference between scatter and hash containers one must first
     * understand how open addressing with linear conflict resolution works.
     * 
     * Any associative container has a "key buffer" which is a linear array. When a new
     * (or existing) key arrives, its "slot" is computed in this linear array (the slot
     * is an index to the key buffer). The calculation of the slot for a key 
     * can be done in a number of ways, typically it is the hash code of the key modulo
     * the buffer length.
     * 
     * In HPPC we can even retrieve the actual "slot" of an existing key. Here is a snippet
     * that forces the slot (hash) value of a key to become itself, just for demonstration.
     */
    IntHashSet set = new IntHashSet() {
      @Override
      protected int hashKey(int key) {
        return key;
      }
    };

    key = 0x0002;
    set.add(key);
    println("Key 0x%04x is at slot: %d", key, set.indexOf(key));

    /*
     * Prints:
     * 
     * Key 0x0002 is at slot: 2
     * 
     * The problem arises when two keys have the same slot value, the we have to resolve
     * a conflict; this is typically done by looking for the next available slot (to the right),
     * again modulo key buffer size. For example this key will conflict with the previous one
     * because they have the same hash value (modulo buffer size), so it'll be placed at slot
     * '3', even though it should have been at '2'.
     */
    key = 0x1002;
    set.add(key);
    println("Key 0x%04x is at slot: %d", key, set.indexOf(key));

    /*
     * Prints:
     * 
     * Key 0x1002 is at slot: 3
     * 
     * This is called open addressing with linear conflict resolution: 
     * 
     * http://en.wikipedia.org/wiki/Open_addressing
     * http://en.wikipedia.org/wiki/Linear_probing
     * 
     * This method of building associative containers has some very nice properties: we can 
     * use CPU caches very effectively (since conflicts scan neighboring elements), we can implement
     * element removals efficiently (it's basically a shift of other conflicting elements).
     * 
     * But there is also a problem. What happens if we keep adding conflicting elements over
     * and over? They will create a long chain of "occuppied" slots and every new insertion 
     * (or lookup) will have an increasingly prohibitive cost until a rehash occurs.
     * 
     * With our toy example this is quite easy to demonstrate.
     */
    for (int i = 2; i < 10; i++) {
      key = (i << 12) + 2;
      set.add(key);
      println("Key 0x%04x is at slot: %d", key, set.indexOf(key));
    }
    
    /*
     * Prints:
     * 
     * Key 0x2002 is at slot: 4
     * Key 0x3002 is at slot: 5
     * Key 0x4002 is at slot: 6
     * Key 0x5002 is at slot: 7
     * Key 0x6002 is at slot: 8
     * Key 0x7002 is at slot: 9
     * Key 0x8002 is at slot: 10
     * Key 0x9002 is at slot: 11
     * 
     * We now have a hash map that isn't like a hash map at all -- all keys, instead of being
     * distributed across the buffer space, are adjacent and grouped. Here is the visualization
     * of the buffer (a dot is an empty buffer region, numbers indicate progressively
     * more occupied regions). Let's also expand the buffer a bit to make the point clearer.  
     */
    set.ensureCapacity(1000);
    println("Keys buffer: %s", set.visualizeKeyDistribution(30));

    /*
     * Prints:
     * 
     * Keys buffer: 1.............................
     * 
     * which is clearly a nonsensical hash set distribution. A typically deployed trick is to make
     * some hash function redistribute keys all over the buffer space. Then it's more difficult (but
     * not impossible) to encounter such bad collision chains. Note how the (linear) keys are 
     * distributed over the buffer space and how the occupancy of the buffer drops upon buffer
     * expansions. We use a constant hash order mixing strategy here, ignore it for the moment.
     */
    println("Adding keys...");
    set = new IntHashSet(0, 0.75d, HashOrderMixing.constant(0xdeadbeef));
    key = 0;
    for (int i = 0; i < 50; i++) {
      for (int j = 0; j < 1000; j++) {
        set.add(key++);
      }
      println("%5d keys, buffer size: %6d, occupancy: %s", set.size(), set.keys.length, set.visualizeKeyDistribution(30));
    }
    
    /*
     * You should see that, with increasing buffer size and the number of keys, they are nearly
     * uniformly distributed over the buffer space:
     * 
     *  1000 keys, buffer size:   2049, occupancy: 544355644445654565654666556565
     *  2000 keys, buffer size:   4097, occupancy: 545564435485554555744555454565
     *  3000 keys, buffer size:   4097, occupancy: 879887778698777787987788687888
     *  4000 keys, buffer size:   8193, occupancy: 555555655554565455555554555445
     *  5000 keys, buffer size:   8193, occupancy: 667766866675676566666666666666
     *  6000 keys, buffer size:   8193, occupancy: 888887987786898677888787787777
     *  7000 keys, buffer size:  16385, occupancy: 445454445444444454545444444444
     *  8000 keys, buffer size:  16385, occupancy: 555555545554455555655555555455
     * ...
     * 49000 keys, buffer size:  65537, occupancy: 888888878888888888887878888888
     * 50000 keys, buffer size: 131073, occupancy: 444444444444444444444444444444
     * 
     * The final step causes a rehash so occupancy drops by 50% 
     * (the buffer is doubled). Makes sense.
     * 
     * There is still a very subtle problem that remains. Consider the following
     * snippet of code that copies the first 10000 keys from the above container
     * to another hash container (with the same hashing function). We make the 
     * capacity of "other" identical to the origin set. 
     */
    println("Copying to 'other'...");
    IntHashSet other = new IntHashSet(set.size(), 0.75d, HashOrderMixing.constant(0xdeadbeef));
    int keysToCopy = 10000;
    for (IntCursor c : set) {
      if (--keysToCopy < 0) {
        break;
      } else {
        other.add(c.value);
      }
    }
    
    /*
     * Now lets check out the distribution of keys in "other".
     */
    println("%5d keys, buffer size: %6d, occupancy: %s", 
        other.size(), 
        other.keys.length, 
        other.visualizeKeyDistribution(30));
    
    /*
     * The above should print:
     * 
     * 10000 keys, buffer size: 131073, occupancy: 444443........................
     * 
     * Clearly something went terribly wrong -- our keys are grouped again 
     * (although a bit more sparsely).
     * 
     * The reason for this behavior is pretty obvious: the keys are assigned to slots that are
     * distributed according to the result of the hash function. But when one iterates over 
     * the elements of a hash set, the keys are traversed in the nearly-sorted order of 
     * these hash values!
     * 
     * Let it sink in a bit. If we iterate over a hash set, the keys we retrieve are in the
     * *worst* possible hash-order.
     * 
     * This can lead to some very dangerous data-related pathologies, like any lookup operation 
     * requiring a long time to find a free or matching slot. 
     * 
     * This is very easy to demonstrate (in a number of ways). The example below 
     * simply recreates a hash container with a high load factor that is on 
     * the verge of expansion.
     */

    // 5000 keys from expanding the buffer... (nearly full capacity).  
    double lf = 0.9;
    int keys = (int) Math.ceil((1 << 19) / lf) - 5000;

    set = new IntHashSet(0, lf, HashOrderMixing.none());
    for (int i = keys; i-- != 0;) {
      set.add(i);
    }

    other = new IntHashSet(0, lf, HashOrderMixing.none());
    int added = 0;
    long start = System.currentTimeMillis();
    long deadline = start + TimeUnit.SECONDS.toMillis(5);
    for (int v : set.toArray()) {
      other.add(v);

      // Print some diagnostics every 10k elements.
      if ((++added % 10000) == 0) {
        long round = -(start - (start = System.currentTimeMillis()));
        println("%6d keys, round: %5d ms, buffer: %s", added, round, other.visualizeKeyDistribution(40));
        
        if (start > deadline) {
          println("Breaking out forcibly, it'll take forever.");
          break; // Don't run for too long.
        }
      }
    }
    
    /*
     * Note how add(), that should be a very simple and cheap operation, becomes an 
     * expensive, nearly blocking call. The reason is of course the fully-occupied
     * front of the hash table; every slot lookup become more and more expensive.
     * 
     * ...
     * 340000 keys, round:     1 ms, buffer: 7777777777777777777777777777777777777771
     * 345000 keys, round:     1 ms, buffer: 7777777777777777777777777777777777777775
     * 350000 keys, round:     4 ms, buffer: 9777777777777777777777777777777777777777
     * 355000 keys, round:    29 ms, buffer: X977777777777777777777777777777777777777
     * 360000 keys, round:    57 ms, buffer: XXX7777777777777777777777777777777777777
     * 365000 keys, round:    85 ms, buffer: XXXX777777777777777777777777777777777777
     * 370000 keys, round:   112 ms, buffer: XXXXX77777777777777777777777777777777777
     * 375000 keys, round:   141 ms, buffer: XXXXXX8777777777777777777777777777777777
     * 380000 keys, round:   169 ms, buffer: XXXXXXX877777777777777777777777777777777
     * 385000 keys, round:   196 ms, buffer: XXXXXXXX97777777777777777777777777777777
     * 390000 keys, round:   223 ms, buffer: XXXXXXXXX9777777777777777777777777777777
     * 395000 keys, round:   250 ms, buffer: XXXXXXXXXXX77777777777777777777777777777
     * 400000 keys, round:   278 ms, buffer: XXXXXXXXXXXX7777777777777777777777777777
     * 405000 keys, round:   306 ms, buffer: XXXXXXXXXXXXX777777777777777777777777777
     * 410000 keys, round:   333 ms, buffer: XXXXXXXXXXXXXX77777777777777777777777777
     * 415000 keys, round:   362 ms, buffer: XXXXXXXXXXXXXXX8777777777777777777777777
     * 420000 keys, round:   390 ms, buffer: XXXXXXXXXXXXXXXX877777777777777777777777
     * 425000 keys, round:   418 ms, buffer: XXXXXXXXXXXXXXXXX97777777777777777777777
     * 430000 keys, round:   447 ms, buffer: XXXXXXXXXXXXXXXXXX9777777777777777777777
     * 435000 keys, round:   473 ms, buffer: XXXXXXXXXXXXXXXXXXXX77777777777777777777
     * 440000 keys, round:   502 ms, buffer: XXXXXXXXXXXXXXXXXXXXX7777777777777777777
     * ...
     * 
     * The examples leading to the above scenario can be multiplied and are not always
     * easy to predict. There is no perfect way to solve it either, it's an inherent 
     * feature of linear conflict resolution. There are some workarounds that could 
     * be applied to putAll and similar operations (Koloboke and Fastutil implement 
     * them), but there is no way to solve the issue systematically... unless each and 
     * every hash container has a different key distribution.
     * 
     * Which is exactly what HPPC implements. The two different "flavors" of associative
     * containers are meant to distinguish between "safe" and "fast" ones.
     * 
     * Any hash container will, by default, use a fairly unique internal
     * mixing seed that ensures no two objects have the same distribution of keys.
     * 
     * Compare the running time (and distributions) from the code below with the one
     * executed before.
     */

    other = new IntHashSet(0, 0.9d);
    added = 0;
    start = System.currentTimeMillis();
    for (int v : set.toArray()) {
      if ((++added % 50000) == 0) {
        long round = -(start - (start = System.currentTimeMillis()));
        println("%6d keys, round: %5d ms, buffer: %s", added, round, other.visualizeKeyDistribution(40));
      }
      other.add(v);
    }
    
    /*
     * Prints:
     * 
     *  50000 keys, round:     7 ms, buffer: 8888888888888878888888888888888888888888
     * 100000 keys, round:     3 ms, buffer: 8888888888888888888888888888888888888888
     * 150000 keys, round:     3 ms, buffer: 6666666666666666666666666666666666666666
     * 200000 keys, round:     2 ms, buffer: 8888888888888888888888888888888888888888
     * 250000 keys, round:     5 ms, buffer: 5555555555555555555555555555555555555555
     * 300000 keys, round:     1 ms, buffer: 6666666666666666666666666666666666666666
     * 350000 keys, round:     2 ms, buffer: 7777777777777777777777777777777777777777
     * 400000 keys, round:     2 ms, buffer: 8888888888888888888888888888888888888888
     * 450000 keys, round:     2 ms, buffer: 9999999999999999999999999999999999999999
     * 500000 keys, round:     9 ms, buffer: 5555555555555555555555555555555555555555
     * 550000 keys, round:     2 ms, buffer: 5555555555555555555555555555555555555555
     * 600000 keys, round:     2 ms, buffer: 6666666666666666666666666666666666666666
     * 650000 keys, round:     2 ms, buffer: 6666666666666666666666666666666666666666 
     * 
     * Nothing is free though. There is a small performance penalty (and it 
     * really is very small unless you're dealing with huge collections) associated with
     * an additional memory read (the mixing seed) and a XOR with the key hash.
     * 
     * To conclude this example: contrary to hash containers, the "scatter" 
     * variants of maps and sets do *not* implement key remixing; they also implement
     * a simpler hashing heuristic to speed up slot lookups. 
     * 
     * Scatter containers are still useful (they're faster!) and can be used to implement lookup tables
     * or counting tables. It is important to remember though to *never* copy the keys of a hash map
     * or a hash set to a scatter map or set. You can do it the other way around though:
     * copy keys from a scatter set to a hash set, for instance, because the hash set's mixing
     * function will make sure they are uniquely redistributed across the buffer space. 
     * 
     * That's it, simple.
     */
  }

  private void println(String msg, Object... args) {
    System.out.println(String.format(Locale.ROOT, msg, args));
  }
}
