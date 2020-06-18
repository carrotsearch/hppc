package com.carrotsearch.hppc;

/**
 * Provides tools for maps.
 * @author broustant
 */
public class WormMapUtil {
    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hashChar(char c) {
        return hashShort((short) c);
    }

    /**
     * Hashes a short. Improves distribution for Map or Set.
     */
    public static int hashShort(short s) {
        s ^= (s >>> 10) ^ (s >>> 6);
        return s ^ (s >>> 4) ^ (s >>> 2);
    }

    /**
     * Hashes an int. Improves distribution for Map or Set.
     */
    public static int hashInt(int i) {
        int h = i * -1640531527;
        return h ^ h >> 16;
    }

    /**
     * Hashes a long. Improves distribution for Map or Set.
     */
    public static int hashLong(long l)
    {
        return hashInt((int) ((l >>> 32) ^ l));
    }


    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(char c) {
        return hashShort((short) c);
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
        return hashInt((int) ((l >>> 32) ^ l));
    }

    /**
     * Hashes a char. Improves distribution for Map or Set.
     */
    public static int hash(Object o) {
        return hash(o.hashCode());
    }
}
