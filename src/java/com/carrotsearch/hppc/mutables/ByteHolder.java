package com.carrotsearch.hppc.mutables;

/**
 * <code>byte</code> holder.
 */
public final class ByteHolder
{
    public byte value;

    public ByteHolder()
    {
    }
    
    public ByteHolder(byte value)
    {
        this.value = value;
    }
    
    public int hashCode()
    {
        return value;
    }
    
    public boolean equals(Object other)
    {
        return (other instanceof ByteHolder) && value == ((ByteHolder) other).value;
    }
}
