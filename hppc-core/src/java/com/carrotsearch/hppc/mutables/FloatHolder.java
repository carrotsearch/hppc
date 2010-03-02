package com.carrotsearch.hppc.mutables;

/**
 * <code>float</code> holder.
 */
public final class FloatHolder
{
    public float value;

    public FloatHolder()
    {
    }
    
    public FloatHolder(float value)
    {
        this.value = value;
    }

    public int hashCode()
    {
        return Float.floatToIntBits(value);
    }
    
    public boolean equals(Object other)
    {
        return (other instanceof FloatHolder) && 
            Float.floatToIntBits(value) == 
                Float.floatToIntBits(((FloatHolder) other).value);
    }
}
