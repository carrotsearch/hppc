package com.carrotsearch.hppc.annotations;

import java.lang.annotation.*;

/**
 * Identifies a structure-like class from which utility code can be generated using
 * <code>hppc-struct</code> project's APT processors.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Struct
{
    
}
