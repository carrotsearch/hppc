package com.carrotsearch.hppc;

/**
 * Standard Java hash for primitives.
 * <p/>It is different from MapUtil because here we want <b>standard Java</b> implementations.
 * @author broustant
 */
public class WormPrimitiveHashUtil
{
    public static int hashBoolean(boolean b) {
        return b ? 1 : 0;
    }

    public static int hashByte(byte b) {
        return b;
    }

    public static int hashChar(char c) {
        return c;
    }

    public static int hashShort(short s) {
        return s;
    }

    public static int hashInt(int i) {
        return i;
    }

    public static int hashLong(long l) {
        return (int) (l ^ (l >>> 32));
    }

    public static int hashFloat(float f) {
        return Float.floatToIntBits(f);
    }

    public static int hashDouble(double d) {
        long bits = Double.doubleToLongBits(d);
        return (int) (bits ^ (bits >>> 32));
    }

    public static int hash(boolean b) {
        return b ? 1 : 0;
    }

    public static int hash(byte b) {
        return b;
    }

    public static int hash(char c) {
        return c;
    }

    public static int hash(short s) {
        return s;
    }

    public static int hash(int i) {
        return i;
    }

    public static int hash(long l) {
        return (int) (l ^ (l >>> 32));
    }

    public static int hash(float f) {
        return Float.floatToIntBits(f);
    }

    public static int hash(double d) {
        long bits = Double.doubleToLongBits(d);
        return (int) (bits ^ (bits >>> 32));
    }

    public static int hash(Object o) {
        return hash(o.hashCode());
    }
}
