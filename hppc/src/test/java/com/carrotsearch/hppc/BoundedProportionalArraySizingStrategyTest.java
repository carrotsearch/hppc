package com.carrotsearch.hppc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test case for bounded proportional sizing strategy. 
 */
public class BoundedProportionalArraySizingStrategyTest
{
    private BoundedProportionalArraySizingStrategy resizer;

    @Test(expected = BufferAllocationException.class)
    public void testBeyondIntRange()
    {
        resizer = new BoundedProportionalArraySizingStrategy();
        resizer.grow(
            BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH, 
            BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH, 1);
    }

    @Test
    public void testExactIntRange()
    {
        resizer = new BoundedProportionalArraySizingStrategy();
        int size = BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH - 2;
        size = resizer.grow(size, size, 1);
        assertEquals(BoundedProportionalArraySizingStrategy.MAX_ARRAY_LENGTH, size);
        try {
            size = resizer.grow(size, size, 1);
            throw new RuntimeException("Unexpected.");
        } catch (BufferAllocationException e) {
            // Expected.
        }
    }
}
