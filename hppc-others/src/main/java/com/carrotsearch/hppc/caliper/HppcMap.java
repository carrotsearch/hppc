package com.carrotsearch.hppc.caliper;

import com.carrotsearch.hppc.IntIntOpenHashMap;

public class HppcMap extends MapImplementation<IntIntOpenHashMap>
{
    public HppcMap()
    {
        super(new IntIntOpenHashMap());
    }

    public void remove(int k) { instance.remove(k); }
    public void put(int k, int v) { instance.put(k, v); }
    public int get(int k) { return instance.get(k); }

    @Override
    public int containKeys(int [] keys)
    {
        final IntIntOpenHashMap prepared = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
            count += prepared.containsKey(keys[i]) ? 1 : 0;
        return count;
    }
    
    @Override
    public int putAll(int [] keys, int [] values)
    {
        final IntIntOpenHashMap instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
        {
            count += instance.put(keys[i], values[i]);
        }
        return count;
    }
}