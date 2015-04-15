package com.carrotsearch.hppc;

import java.util.Objects;

/**
 * Intrinsic methods that are fully functional for the generic ({@link Object}) versions
 * of collection classes, but are replaced with low-level corresponding structures for
 * primitive types.
 * 
 * <p><b>This class should not appear in the final distribution (all methods are replaced
 * in templates.</b></p>
 */
final class Intrinsics
{
    /**
     * Anything that implements value-equality function as replaced by the
     * {@link #equals} intrinsic.
     * 
     * Not a public interface because we want the implementing methods to be
     * effectively protected.
     */
    interface EqualityFunction {
      boolean equals(Object v1, Object v2);
    }

    private Intrinsics()
    {
        // no instances.
    }

    /**
     * Returns <code>true</code> if the provided value is an "empty slot"
     * marker. For generic types the empty slot is <code>null</code>,
     * for any other type it is an equivalent of zero.
     * 
     * For floating-point types {@link Float#floatToIntBits(float)} and 
     * {@link Double#doubleToLongBits(double)} is invoked to normalize different
     * representations of zero.  
     * 
     * Testing for zeros should be compiled into fast machine code. 
     */
    public static <T> boolean isEmpty(Object value) {
      return value == null;
    }

    /**
     * Returns the default "empty" value (<code>null</code> or <code>0</code>
     * for primitive types).
     */
    public static <T> T empty()
    {
        return (T) null;
    }

    /**
     * A template cast to the given type T. With type erasure it should work internally just
     * fine and it simplifies code. The cast will be erased for primitive types.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value) {
      return (T) value;
    }

    /**
     * Create an array for the given template type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(int arraySize) {
      return (T[]) new Object [arraySize];
    }

    /**
     * Compare two keys for equivalence.
     * 
     * Generic types are compared by the delegate. 

     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static <T> boolean equals(EqualityFunction delegate, Object e1, Object e2)
    {
        return delegate.equals(e1, e2);
    }

    /**
     * Compare two keys for equivalence.
     * 
     * Generic types are compared for null-equivalence or using {@link Object#equals(Object)}. 

     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static <T> boolean equals(Object e1, Object e2)
    {
        return Objects.equals(e1, e2);
    }

    /**
     * An intrinsic that is replaced with plain addition of arguments for primitive
     * template types. Invalid for generic types.
     */
    public static <T> T add(T op1, T op2) {
      throw new UnsupportedOperationException("Invalid for template or generic types.");
    }    
}
