package com.carrotsearch.hppc;

import static org.junit.Assert.*;

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
            Integer.MAX_VALUE - 10, 
            Integer.MAX_VALUE - 10, 20);
    }
    
    public void testExactIntRange()
    {
        resizer = new BoundedProportionalArraySizingStrategy();
        assertEquals(Integer.MAX_VALUE, 
            resizer.grow(
                Integer.MAX_VALUE - 5, 
                Integer.MAX_VALUE - 5, 1));
    }
}
