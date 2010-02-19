package com.carrotsearch.hppc.mutables;

/**
 * <code>double</code> holder.
 */
public final class DoubleHolder
{
    public double value;
    
    public DoubleHolder()
    {
    }
    
    public DoubleHolder(double value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        final long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }
    
    public boolean equals(Object other)
    {
        return (other instanceof DoubleHolder) && 
            Double.doubleToLongBits(value) == 
                Double.doubleToLongBits(((DoubleHolder) other).value);
    }
}
