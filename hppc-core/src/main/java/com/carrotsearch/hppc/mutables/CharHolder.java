package com.carrotsearch.hppc.mutables;

/**
 * <code>char</code> holder.
 */
public final class CharHolder
{
    public char value;
    
    public CharHolder()
    {
    }

    public CharHolder(char value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        return value;
    }

    public boolean equals(Object other)
    {
        return (other instanceof CharHolder) && value == ((CharHolder) other).value;
    }
}
