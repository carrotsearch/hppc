package com.carrotsearch.hppc.misc;

import java.lang.reflect.Field;

import com.carrotsearch.hppc.IntIntOpenHashMap;

public class HppcOomPut
{
    public static void main(String [] args)
    throws Exception
    {
        IntIntOpenHashMap map = new IntIntOpenHashMap(100, 1f);
        Field f = map.getClass().getDeclaredField("keys");
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
