package com.carrotsearch.hppc;

/* #if ($TemplateOptions.KTypeGeneric) */
import java.util.Arrays;
/* #end */

import com.carrotsearch.hppc.cursors.KTypeCursor;

/**
 * A subclass of {@link KTypeArrayList} adding stack-related utility methods.
 * The top of the stack is at the <code>{@link #size()} - 1</code> element.
 */
/*! #if ($TemplateOptions.KTypeGeneric) @SuppressWarnings("unchecked") #end !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeStack<KType> extends KTypeArrayList<KType> {
  /**
   * New instance with sane defaults.
   */
  public KTypeStack() {
    super();
  }

  /**
   * New instance with sane defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   */
  public KTypeStack(int expectedElements) {
    super(expectedElements);
  }

  /**
   * New instance with sane defaults.
   * 
   * @param expectedElements
   *          The expected number of elements guaranteed not to cause buffer
   *          expansion (inclusive).
   * @param resizer
   *          Underlying buffer sizing strategy.
   */
  public KTypeStack(int expectedElements, ArraySizingStrategy resizer) {
    super(expectedElements, resizer);
  }

  /**
   * Create a stack by pushing all elements of another container to it.
   */
  public KTypeStack(KTypeContainer<KType> container) {
    super(container);
  }

  /**
   * Adds one KType to the stack.
   */
  public void push(KType e1) {
    ensureBufferSpace(1);
    buffer[elementsCount++] = e1;
  }

  /**
   * Adds two KTypes to the stack.
   */
  public void push(KType e1, KType e2) {
    ensureBufferSpace(2);
    buffer[elementsCount++] = e1;
    buffer[elementsCount++] = e2;
  }

  /**
   * Adds three KTypes to the stack.
   */
  public void push(KType e1, KType e2, KType e3) {
    ensureBufferSpace(3);
    buffer[elementsCount++] = e1;
    buffer[elementsCount++] = e2;
    buffer[elementsCount++] = e3;
  }

  /**
   * Adds four KTypes to the stack.
   */
  public void push(KType e1, KType e2, KType e3, KType e4) {
    ensureBufferSpace(4);
    buffer[elementsCount++] = e1;
    buffer[elementsCount++] = e2;
    buffer[elementsCount++] = e3;
    buffer[elementsCount++] = e4;
  }

  /**
   * Add a range of array elements to the stack.
   */
  public void push(KType[] elements, int start, int len) {
    assert start >= 0 && len >= 0;

    ensureBufferSpace(len);
    System.arraycopy(elements, start, buffer, elementsCount, len);
    elementsCount += len;
  }

  /**
   * Vararg-signature method for pushing elements at the top of the stack.
   * <p>
   * <b>This method is handy, but costly if used in tight loops (anonymous array
   * passing)</b>
   * </p>
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  @SafeVarargs
  /* #end */
  public final void push(KType... elements) {
    push(elements, 0, elements.length);
  }

  /**
   * Pushes all elements from another container to the top of the stack.
   */
  public int pushAll(KTypeContainer<? extends KType> container) {
    return addAll(container);
  }

  /**
   * Pushes all elements from another iterable to the top of the stack.
   */
  public int pushAll(Iterable<? extends KTypeCursor<? extends KType>> iterable) {
    return addAll(iterable);
  }

  /**
   * Discard an arbitrary number of elements from the top of the stack.
   */
  public void discard(int count) {
    assert elementsCount >= count;

    elementsCount -= count;
    /* #if ($TemplateOptions.KTypeGeneric) */
    Arrays.fill(buffer, elementsCount, elementsCount + count, null);
    /* #end */
  }

  /**
   * Discard the top element from the stack.
   */
  public void discard() {
    assert elementsCount > 0;

    elementsCount--;
    /* #if ($TemplateOptions.KTypeGeneric) */
    buffer[elementsCount] = null;
    /* #end */
  }

  /**
   * Remove the top element from the stack and return it.
   */
  public KType pop() {
    assert elementsCount > 0;

    final KType v = Intrinsics.<KType> cast(buffer[--elementsCount]);
    /* #if ($TemplateOptions.KTypeGeneric) */
    buffer[elementsCount] = null;
    /* #end */
    return v;
  }

  /**
   * Peek at the top element on the stack.
   */
  public KType peek() {
    assert elementsCount > 0;
    return Intrinsics.<KType> cast(buffer[elementsCount - 1]);
  }

  /**
   * Create a stack by pushing a variable number of arguments to it.
   */
  /* #if ($TemplateOptions.KTypeGeneric) */
  @SafeVarargs
  /* #end */
  public static <KType> KTypeStack<KType> from(KType... elements) {
    final KTypeStack<KType> stack = new KTypeStack<KType>(elements.length);
    stack.push(elements);
    return stack;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public KTypeStack<KType> clone() {
    return (KTypeStack<KType>) super.clone();
  }
}
