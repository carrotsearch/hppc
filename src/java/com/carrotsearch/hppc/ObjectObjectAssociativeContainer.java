package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

/**
 * An associative container (alias: map, dictionary) from keys to values. 
 */
public interface ObjectObjectAssociativeContainer<KType, VType> 
    extends Iterable<ObjectObjectCursor<KType, VType>>
{
}
