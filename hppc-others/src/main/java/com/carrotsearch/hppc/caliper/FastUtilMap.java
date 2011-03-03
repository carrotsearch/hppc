package com.carrotsearch.hppc.caliper;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class FastUtilMap extends MapImplementation<Int2IntOpenHashMap>
{
    public FastUtilMap()
    {
        super(new Int2IntOpenHashMap());
    }
    
    public void remove(int k) { instance.remove(k); }
    public void put(int k, int v) { instance.put(k, v); }
    public int get(int k) { return instance.get(k); }
    
    @Override
    public int containKeys(int [] keys)
    {
        final Int2IntOpenHashMap instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
            count += instance.containsKey(keys[i]) ? 1 : 0;
        return count;
    }
    
    @Override
    public int putAll(int [] keys, int [] values)
    {
        final Int2IntOpenHashMap instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
        {
            count += instance.put(keys[i], values[i]);
        }
        return count;
    }
}