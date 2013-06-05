package com.carrotsearch.hppc.caliper;

import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import com.carrotsearch.hppc.IntIntOpenHashMap;

public class MahoutMap extends MapImplementation<OpenIntIntHashMap>
{
    public MahoutMap()
    {
        super(new OpenIntIntHashMap(
            IntIntOpenHashMap.DEFAULT_CAPACITY,
            AbstractIntIntMap.DEFAULT_MIN_LOAD_FACTOR,
            IntIntOpenHashMap.DEFAULT_LOAD_FACTOR));
    }

    public void remove(int k) { instance.removeKey(k); }
    public void put(int k, int v) { instance.put(k, v); }
    public int get(int k) { return instance.get(k); }

    @Override
    public int containKeys(int [] keys)
    {
        final OpenIntIntHashMap prepared = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
            count += prepared.containsKey(keys[i]) ? 1 : 0;
        return count;
    }

    @Override
    public int putAll(int [] keys, int [] values)
    {
        final OpenIntIntHashMap instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
        {
            count += instance.put(keys[i], values[i]) ? 1 : 0;
        }
        return count;
    }
}