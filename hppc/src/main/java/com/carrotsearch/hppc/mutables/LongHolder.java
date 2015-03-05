package com.carrotsearch.hppc.mutables;

/**
 * <code>long</code> holder.
 */
public final class LongHolder
{
    public long value;

    public LongHolder()
    {
    }

    public LongHolder(long value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        return (int)(value ^ (value >>> 32));
    }

    public boolean equals(Object other)
    {
        return (other instanceof LongHolder) && value == ((LongHolder) other).value;
    }
}
