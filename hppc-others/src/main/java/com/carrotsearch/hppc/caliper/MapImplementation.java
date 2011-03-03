package com.carrotsearch.hppc.caliper;

/**
 * Something implementing a map interface (int-int).
 */
public abstract class MapImplementation<T>
{
    protected final T instance;

    protected MapImplementation(T instance)
    {
        this.instance = instance;
    }

    /**
     * Return the number of <code>keys</code> this map has.
     */
    public abstract int containKeys(int [] keys);
    public abstract int putAll(int [] keys, int [] values);

    public abstract void put(int k, int v);
    public abstract int  get(int k);
    public abstract void remove(int k);

}