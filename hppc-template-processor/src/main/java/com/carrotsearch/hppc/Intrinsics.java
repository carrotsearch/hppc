package com.carrotsearch.hppc;

import java.util.Objects;

/**
 * Intrinsic methods that are fully functional for the source templates
 * (generic) and are replaced with low-level corresponding equivalents for the
 * generated primitive types.
 * 
 * Whenever there is a generic type on a static method it can be used to parameterize
 * the given method based on the actual template type. Most intrinsics can guess their
 * generic template parameter (for example if the template has only one replaceable
 * type), but sometimes it may be necessary to provide the template type directly. 
 * 
 * <b>This class should not appear in the final distribution (all methods are
 * replaced in templates.</b> Use forbidden-apis checker to make sure this is the
 * case.
 */
public final class Intrinsics {
  /**
   * Anything that implements value-equality function as replaced by the
   * {@link #equals} intrinsic.
   * 
   * Not a public interface because we want the implementing methods to be
   * effectively protected, but used for API consistency in templates.
   */
  public interface EqualityFunction {
    boolean equals(Object v1, Object v2);
  }

  /**
   * Anything that distributes keys by their hash value.
   * 
   * Not a public interface because we want the implementing methods to be
   * effectively protected, but used for API consistency in templates.   * 
   */
  public interface KeyHasher<T> {
    int hashKey(T key);
  }

  /**
   * Everything static.
   */
  private Intrinsics() {}

  /**
   * Returns <code>true</code> if the provided value is an "empty key" marker.
   * For generic types the empty key is <code>null</code>, for any other type
   * it is an equivalent of zero.
   * 
   * Testing for zeros should be compiled into fast machine code.
   */
  public static <T> boolean isEmpty(Object value) {
    return value == null;
  }

  /**
   * Returns the default "empty key" (<code>null</code> or <code>0</code> for
   * primitive types).
   */
  public static <T> T empty() {
    return (T) null;
  }

  /**
   * A template cast to the given type T. With type erasure it should work
   * internally just fine and it simplifies code. The cast will be erased for
   * primitive types.
   */
  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }

  /**
   * Creates an array for the given template type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(int arraySize) {
    return (T[]) new Object[arraySize];
  }

  /**
   * Compare two keys for equivalence.
   * 
   * Generic types are compared using the delegate function.
   * 
   * Primitive types are compared using <code>==</code>, except for
   * floating-point types where they're compared by their actual representation
   * bits as returned from {@link Double#doubleToLongBits(double)} and
   * {@link Float#floatToIntBits(float)}.
   */
  public static <T> boolean equals(EqualityFunction delegate, Object e1, Object e2) {
    return delegate.equals(e1, e2);
  }

  /**
   * Compare two keys for equivalence.
   * 
   * Generic types are compared for null-equivalence or using
   * {@link Object#equals(Object)}.
   * 
   * Primitive types are compared using <code>==</code>, except for
   * floating-point types where they're compared by their actual representation
   * bits as returned from {@link Double#doubleToLongBits(double)} and
   * {@link Float#floatToIntBits(float)}.
   */
  public static <T> boolean equals(Object e1, Object e2) {
    return Objects.equals(e1, e2);
  }

  /**
   * An intrinsic that is replaced with plain addition of arguments for
   * primitive template types. Invalid for non-number generic types.
   */
  @SuppressWarnings("unchecked")
  public static <T> T add(T op1, T op2) {
    if (op1.getClass() != op2.getClass()) {
      throw new RuntimeException("Arguments of different classes: " + op1 + " " + op2);
    }

    if (Byte.class.isInstance(op1)) {
      return (T)(Byte)(byte)(((Byte) op1).byteValue() + ((Byte) op2).byteValue());
    }
    if (Short.class.isInstance(op1)) {
      return (T)(Short)(short)(((Short) op1).shortValue() + ((Short) op2).shortValue());
    }
    if (Character.class.isInstance(op1)) {
      return (T)(Character)(char)(((Character) op1).charValue() + ((Character) op2).charValue());
    }
    if (Integer.class.isInstance(op1)) {
      return (T)(Integer)(((Integer) op1).intValue() + ((Integer) op2).intValue());
    }
    if (Float.class.isInstance(op1)) {
      return (T)(Float)(((Float) op1).floatValue() + ((Float) op2).floatValue());
    }
    if (Long.class.isInstance(op1)) {
      return (T)(Long)(((Long) op1).longValue() + ((Long) op2).longValue());
    }
    if (Double.class.isInstance(op1)) {
      return (T)(Double)(((Double) op1).doubleValue() + ((Double) op2).doubleValue());
    }

    throw new UnsupportedOperationException("Invalid for arbitrary types: " + op1 + " " + op2);
  }
}
