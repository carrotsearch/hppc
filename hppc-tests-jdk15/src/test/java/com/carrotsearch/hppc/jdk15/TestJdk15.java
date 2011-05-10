package com.carrotsearch.hppc.jdk15;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppc.IntArrayList;

/**
 * 
 */
public class TestJdk15
{
    @Test
    public void checkSimpleClassAccess()
    {
        IntArrayList list = IntArrayList.newInstance();
        list.add(1, 2, 3);
        Assert.assertEquals(3, list.size());
    }
    
    @Test
    public void checkNewToArray()
    {
        IntArrayList list = IntArrayList.newInstance();
        list.add(1, 2, 3);
        Assert.assertArrayEquals(new int [] {1, 2, 3}, list.toArray());
    }
}