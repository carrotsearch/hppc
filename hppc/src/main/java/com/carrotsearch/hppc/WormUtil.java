package com.carrotsearch.hppc;

/**
 * Standard Java hash for primitives.
 * <p/>It is different from MapUtil because here we want <b>standard Java</b> implementations.
 * @author broustant
 */
public class WormUtil
{
    public static int stdHash(boolean b) {
        return b ? 1 : 0;
    }

    public static int stdHash(byte b) {
        return b;
    }

    public static int stdHash(char c) {
        return c;
    }

    public static int stdHash(short s) {
        return s;
    }

    public static int stdHash(int i) {
        return i;
    }

    public static int stdHash(long l) {
        return (int) (l ^ (l >>> 32));
    }

    public static int stdHash(float f) {
        return Float.floatToIntBits(f);
    }

    public static int stdHash(double d) {
        long bits = Double.doubleToLongBits(d);
        return (int) (bits ^ (bits >>> 32));
    }

    public static int stdHash(Object o) {
        return stdHash(o.hashCode());
    }

    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(char c) {
        return hash((short) c);
    }

    /**
     * Hashes a short. Improves distribution for Map or Set.
     */
    public static int hash(short s) {
        s ^= (s >>> 10) ^ (s >>> 6);
        return s ^ (s >>> 4) ^ (s >>> 2);
    }

    /**
     * Hashes an int. Improves distribution for Map or Set.
     */
    public static int hash(int i) {
        int h = i * -1640531527;
        return h ^ h >> 16;
    }

    /**
     * Hashes a long. Improves distribution for Map or Set.
     */
    public static int hash(long l)
    {
        return hash((int) ((l >>> 32) ^ l));
    }

    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(Object o) {
        return hash(o.hashCode());
    }
}
