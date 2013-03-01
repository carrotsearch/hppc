package com.carrotsearch.hppc.misc;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.lang.reflect.Field;

public class FastUtilOomPut
{
    public static void main(String [] args)
    throws Exception
    {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap(100, 1f);
        Field f = map.getClass().getDeclaredField("key");
        f.setAccessible(true);

        boolean hitOOM = false;
        for (int i = 0;; i++) {
            try {
                if (hitOOM) { System.out.println("put(" + i + ")"); }
                map.put(i,  i);
            } catch (OutOfMemoryError e) {
                hitOOM = true;
                System.out.println("OOM, map: " + map.size() + " " + ((int[])f.get(map)).length);
            }
        }
    }
}
