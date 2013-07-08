package com.carrotsearch.hppc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for bounded proportional sizing strategy. 
 */
public class BoundedProportionalArraySizingStrategyTest
{
    private BoundedProportionalArraySizingStrategy resizer;

    /* */
    @Before
    public void checkAssertionsEnabled()
    {
        boolean enabled = true;
        try
        {
            assert false;
            enabled = false;
        }
        catch (AssertionError e)
        {
            // Expected, fall through.
        }

        assertTrue("Enable JVM assertions for testing.", enabled);
    }

    @Test(expected = AssertionError.class)
    public void testBeyondIntRange()
    {
        resizer = new BoundedProportionalArraySizingStrategy();
        resizer.grow(
            BoundedProportionalArraySizingStrategy.MAX_ARRAY_SIZE, 
            BoundedProportionalArraySizingStrategy.MAX_ARRAY_SIZE, 1);
    }

    @Test
    public void testExactIntRange()
    {
        resizer = new BoundedProportionalArraySizingStrategy();
        int size = BoundedProportionalArraySizingStrategy.MAX_ARRAY_SIZE - 2;
        size = resizer.grow(size, size, 1);
        assertEquals(BoundedProportionalArraySizingStrategy.MAX_ARRAY_SIZE, size);
        try {
            size = resizer.grow(size, size, 1);
            throw new RuntimeException("Unexpected.");
        } catch (AssertionError e) {
            // Expected.
        }
    }
}
