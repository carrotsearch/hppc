package com.carrotsearch.hppc.generator;

public enum Type
{
    GENERIC, BYTE, CHAR, SHORT, INT, FLOAT, LONG, DOUBLE;

    public String getBoxedType()
    {
        if (this == GENERIC) return "Object";

        String boxed = name().toLowerCase();
        return Character.toUpperCase(boxed.charAt(0)) + boxed.substring(1);
    }

    public String getType()
    {
        if (this == GENERIC) return "Object";

        return name().toLowerCase();
    }
}