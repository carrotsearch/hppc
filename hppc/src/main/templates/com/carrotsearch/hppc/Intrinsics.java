package com.carrotsearch.hppc;

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
    private Intrinsics()
    {
        // no instances.
    }

    /**
     * Returns <code>true</code> if the provided key is an "empty slot"
     * marker. For generic types the empty slot is <code>null</code>,
     * for any other type it is an equivalent of zero.
     * 
     * For floating-point types {@link Float#floatToIntBits(float)} and 
     * {@link Double#doubleToLongBits(double)} is invoked to normalize different
     * representations of zero.  
     * 
     * Testing for zeros should be compiled into fast machine code. 
     */
    public static boolean isEmptyKey(Object key) {
      return key == null;
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
     * Create an array for the given type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(int arraySize) {
      return (T[]) new Object [arraySize];
    }

    /**
     * Returns the default value for keys (<code>null</code> or <code>0</code>
     * for primitive types).
     * 
     * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954"  
     */
    public static <T> T defaultKTypeValue()
    {
        return (T) null;
    }

    /**
     * Returns the default value for values (<code>null</code> or <code>0</code>
     * for primitive types).
     *  
     * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954"  
     */
    public static <T> T defaultVTypeValue()
    {
        return (T) null;
    }

    /**
     * Compare two keys for equivalence. Null references return <code>true</code>.
     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static boolean equalsKType(Object e1, Object e2)
    {
        return e1 == null ? e2 == null : e1.equals(e2);
    }
    
    /**
     * Compare two keys for equivalence. Null references return <code>true</code>.
     * Primitive types are compared using <code>==</code>.
     */
    public static boolean equalsVType(Object e1, Object e2)
    {
        return e1 == null ? e2 == null : e1.equals(e2);
    }

    /**
     * Compares two keys for equivalence.
     */
    public static <T> boolean same(Object k1, Object k2) {
      return k1 == null ? k2 == null : k1.equals(k2);
    }

    /**
     * An intrinsic that is replaced with plain addition of arguments for primitive
     * template types. Invalid for generic types.
     */
    public static <T> T add(T op1, T op2) {
      throw new UnsupportedOperationException("Invalid for template or generic types.");
    }    
}
