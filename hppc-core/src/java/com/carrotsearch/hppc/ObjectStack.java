package com.carrotsearch.hppc;


/**
 * An extension to {@link ObjectArrayList} adding stack-related utility methods.
 * 
 * A brief comparison of the API against the Java Collections framework:
 * <table class="nice" summary="Java Collections Stack and HPPC ObjectStack, related methods.">
 * <caption>Java Collections Stack and HPPC {@link ObjectStack}, related methods.</caption>
 * <thead>
 *     <tr class="odd">
 *         <th scope="col">{@linkplain java.util.Stack java.util.Stack}</th>
 *         <th scope="col">{@link ObjectStack}</th>  
 *     </tr>
 * </thead>
 * <tbody>
 * <tr            ><td>push           </td><td>push           </td></tr>
 * <tr class="odd"><td>pop            </td><td>pop, discard   </td></tr>
 * <tr            ><td>peek           </td><td>peek           </td></tr>
 * <tr class="odd"><td>removeRange, 
 *                     removeElementAt</td><td>removeRange, remove, discard</td></tr>
 * <tr            ><td>size           </td><td>size           </td></tr>
 * <tr class="odd"><td>clear          </td><td>clear, release </td></tr>
 * <tr            ><td>               </td><td>+ other methods from {@link ObjectArrayList}</td></tr>
 * </tbody>
 * </table>
 */
public class ObjectStack<KType> extends ObjectArrayList<KType>
{
    /**
     * Create with default sizing strategy and initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public ObjectStack()
    {
        super();
    }

    /**
     * Create with default sizing strategy and the given initial capacity.
     * 
     * @see BoundedProportionalArraySizingStrategy
     */
    public ObjectStack(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * Create with a custom buffer resizing strategy.
     */
    public ObjectStack(int initialCapacity, ArraySizingStrategy resizer)
    {
        super(initialCapacity, resizer);
    }

    /**
     * Create a stack by pushing all elements of another container to it.
     */
    public ObjectStack(ObjectContainer<KType> container)
    {
        super(container);
    }

    /**
     * Adds one KType to the stack.
     */
    public final void push(KType e1)
    {
        ensureBufferSpace(1);
        buffer[elementsCount++] = e1;
    }

    /**
     * Adds two KTypes to the stack.
     */
    public final void push(KType e1, KType e2)
    {
        ensureBufferSpace(2);
        buffer[elementsCount++] = e1;
        buffer[elementsCount++] = e2;
    }

    /**
     * Adds three KTypes to the stack.
     */
    public final void push(KType e1, KType e2, KType e3)
    {
        ensureBufferSpace(3);
        buffer[elementsCount++] = e1;
        buffer[elementsCount++] = e2;
        buffer[elementsCount++] = e3;
    }

    /**
     * Adds four KTypes to the stack.
     */
    public final void push(KType e1, KType e2, KType e3, KType e4)
    {
        ensureBufferSpace(4);
        buffer[elementsCount++] = e1;
        buffer[elementsCount++] = e2;
        buffer[elementsCount++] = e3;
        buffer[elementsCount++] = e4;
    }

    /**
     * Add a range of array elements to the stack.
     */
    public final void push(KType [] elements, int start, int len)
    {
        assert start >= 0 && len >= 0;

        ensureBufferSpace(len);
        System.arraycopy(elements, start, buffer, elementsCount, len);
        elementsCount += len;
    }

    /**
     * Vararg-signature method for pushing elements at the top of the stack.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     */
    public final void push(KType... elements)
    {
        push(elements, 0, elements.length);
    }

    /**
     * Pushes all elements from another container to the top of the stack.
     */
    public final int pushAll(ObjectContainer<? extends KType> container)
    {
        return addAll(container);
    }

    /**
     * Discard an arbitrary number of elements from the top of the stack.
     */
    public final void discard(int count)
    {
        assert elementsCount >= count;

        elementsCount -= count;
        /* removeIf:primitiveKType */
        java.util.Arrays.fill(buffer, elementsCount, elementsCount + count, null); 
        /* end:removeIf */
    }

    /**
     * Discard the top element from the stack.
     */
    public final void discard()
    {
        assert elementsCount > 0;

        elementsCount--;
        /* removeIf:primitiveKType */
        buffer[elementsCount] = null; 
        /* end:removeIf */
    }

    /**
     * Remove the top element from the stack and return it.
     */
    public final KType pop()
    {
        assert elementsCount > 0;

        final KType v = buffer[--elementsCount];
        /* removeIf:primitiveKType */
        buffer[elementsCount] = null; 
        /* end:removeIf */
        return v;
    }

    /**
     * Peek at the top element on the stack.
     */
    public final KType peek()
    {
        assert elementsCount > 0;

        return buffer[elementsCount - 1];
    }
    
    /**
     * Create a stack by pushing a variable number of arguments to it.
     */
    public static /* removeIf:primitive */ <KType> /* end:removeIf */ 
        ObjectStack<KType> from(KType... elements)
    {
        final ObjectStack<KType> stack = new ObjectStack<KType>(elements.length);
        stack.push(elements);
        return stack;
    }

    /**
     * Create a stack by pushing all elements of another container to it.
     */
    public static /* removeIf:primitive */ <KType> /* end:removeIf */ 
        ObjectStack<KType> from(ObjectContainer<KType> container)
    {
        return new ObjectStack<KType>(container);
    }
}
