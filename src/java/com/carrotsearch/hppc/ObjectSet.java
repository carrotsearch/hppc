package com.carrotsearch.hppc;


/**
 * A set of <code>KType</code>s.
 */
public interface ObjectSet<KType> extends ObjectCollection<KType>
{
    /**
     * Adds <code>k</code> to the set.
     * 
     * @return Returns <code>true</code> if this element was not part of the set before.
     */
    public boolean add(KType k);
}
