package com.carrotsearch.hppc.caliper;

import java.util.HashMap;

public class JavaMap extends MapImplementation<HashMap<Integer, Integer>>
{
    public JavaMap()
    {
        super(new HashMap<Integer, Integer>());
    }
    

    public void remove(int k) { instance.remove(k); }
    public void put(int k, int v) { instance.put(k, v); }
    public int get(int k) { return instance.get(k); }

    @Override
    public int containKeys(int [] keys)
    {
        final HashMap<Integer, Integer> prepared = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
            count += prepared.containsKey(keys[i]) ? 1 : 0;
        return count;
    }    
    
    @Override
    public int putAll(int [] keys, int [] values)
    {
        final HashMap<Integer, Integer> instance = this.instance;
        int count = 0;
        for (int i = 0; i < keys.length; i++)
        {
            instance.put(keys[i], values[i]);
        }
        return count;
    }
}
