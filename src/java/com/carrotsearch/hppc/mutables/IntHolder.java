package com.carrotsearch.hppc.mutables;

/**
 * <code>int</code> holder.
 */
public final class IntHolder
{
    public int value;

    public IntHolder()
    {
    }

    public IntHolder(int value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        return value;
    }

    public boolean equals(Object other)
    {
        return (other instanceof IntHolder) && value == ((IntHolder) other).value;
    }
}
