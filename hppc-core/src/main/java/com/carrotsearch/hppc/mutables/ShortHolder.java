package com.carrotsearch.hppc.mutables;

/**
 * <code>short</code> holder.
 */
public final class ShortHolder
{
    public short value;

    public ShortHolder()
    {
    }

    public ShortHolder(short value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        return value;
    }

    public boolean equals(Object other)
    {
        return (other instanceof ShortHolder) && value == ((ShortHolder) other).value;
    }
}
