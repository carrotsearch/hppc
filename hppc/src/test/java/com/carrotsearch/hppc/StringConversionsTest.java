package com.carrotsearch.hppc;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringConversionsTest
{
    @Test
    public void testByteList()
    {
        ByteArrayList list = new ByteArrayList();
        list.add(new byte [] {1, 2, 3});
        assertEquals("[1, 2, 3]", list.toString());
    }

    @Test
    public void testCharList()
    {
        CharArrayList list = new CharArrayList();
        list.add(new char [] {'a', 'b', 'c'});
        assertEquals("[a, b, c]", list.toString());
    }
    
    @Test
    public void testObjectList()
    {
        ObjectArrayList<String> list = new ObjectArrayList<String>();
        list.add("ab", "ac", "ad");
        assertEquals("[ab, ac, ad]", list.toString());
    }

    @Test
    public void testObjectObjectMap()
    {
        ObjectObjectHashMap<String, String> map = 
            ObjectObjectHashMap.from(
                new String [] {"a"}, 
                new String [] {"b"});

        assertEquals("[a=>b]", map.toString());
    }
}
