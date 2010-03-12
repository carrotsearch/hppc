package com.carrotsearch.hppc.hash;

/**
 * Default hash function for <code>double</code> values.
 */
public class DoubleHashFunction
{
    /**
     * Consistent with {@link Double#hashCode()}.
     */
    public int hash(double key)
    {
        final long bits = Double.doubleToLongBits(key);
        return (int)(bits ^ (bits >>> 32));
    }
}
