package com.carrotsearch.hppc.examples;

import com.carrotsearch.hppc.annotations.Struct;

/**
 * A simple structure-like class.
 */
@Struct
public final class Person
{
    public String firstName;
    public String lastName;
    public int age;
}
