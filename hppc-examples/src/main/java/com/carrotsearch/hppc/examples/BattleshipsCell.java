package com.carrotsearch.hppc.examples;

import com.carrotsearch.hppc.annotations.Struct;

/**
 * A simple structure-class (pseudo value type).
 */
@Struct(dimensions = 2)
public final class BattleshipsCell
{
    public boolean hasShip;
    public boolean hit;
    public int damageLevel;
    public byte owner;
}
