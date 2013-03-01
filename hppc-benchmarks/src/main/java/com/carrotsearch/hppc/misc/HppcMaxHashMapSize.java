package com.carrotsearch.hppc.misc;

import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * Requires a lot of heap!
 */
public class HppcMaxHashMapSize
{
    public static void main(String [] args)
    throws Exception
    {
        IntOpenHashSet set = new IntOpenHashSet(0x40000000, 0.75f);

        for (int i = 0;; i++) {
            try {
                set.add(i);
            } catch (RuntimeException e) {
                System.out.println("Max capacity: " + set.size());
            } catch (OutOfMemoryError e) {
                System.out.println("OOM hit at size: " + set.size()  + " (0x" + Integer.toHexString(set.size()));
            }
        }
    }
}
