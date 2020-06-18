package com.carrotsearch.hppc;

/**
 * Bit manipulation utilities.
 * @author broustant
 */
public class WormBitUtil {

    private static final int[] LOG_TABLE_256 = initializeLogTable256();

    private WormBitUtil() {
    }

    /**
     * Gets the next power of two of an integer.
     * <br>To get the previous power of two, use {@link Integer#highestOneBit(int)}</br>
     */
    public static int nextIntPowerOfTwo(int n) {
        // See http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2.

        n--;
        // Divide by 2^k for consecutive doublings of k up to 32, and then or the results.
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n++;
        // The result is a number of 1 bits equal to the number of bits in the original number, plus 1.
        // That's the next power of 2.
        return n;

//        Here's a more concrete example. Let's take the number 221, which is 11011101 in binary:
//
//        n--;           // 1101 1101 --> 1101 1100
//        n |= n >> 1;   // 1101 1101 | 0110 1110 = 1111 1111
//        n |= n >> 2;   // ...
//        n |= n >> 4;
//        n |= n >> 8;
//        n |= n >> 16;  // 1111 1111 | 1111 1111 = 1111 1111
//        n++;           // 1111 1111 --> 1 0000 0000
//        There's one bit in the ninth position, which represents 2^8, or 256, which is indeed the next largest power of 2.
//        Each of the shifts overlaps all of the existing 1 bits in the number with some of the previously untouched zeroes,
//        eventually producing a number of 1 bits equal to the number of bits in the original number. Adding one to that
//        value produces a new power of 2.
//
//        Another example; we'll use 131, which is 10000011 in binary:
//
//        n--;           // 1000 0011 --> 1000 0010
//        n |= n >> 1;   // 1000 0010 | 0100 0010 = 1100 0011
//        n |= n >> 2;   // 1100 0011 | 0011 0000 = 1111 0011
//        n |= n >> 4;   // 1111 0011 | 0000 1111 = 1111 1111
//        n |= n >> 8;   // ... (At this point all bits are 1, so further bitwise-or
//        n |= n >> 16;  //      operations produce no effect.)
//        n++;           // 1111 1111 --> 1 0000 0000
//        And indeed, 256 is the next highest power of 2 from 131.
//
//        If the number of bits used to represent the integer is itself a power of 2, you can continue to extend this
//        technique efficiently and indefinitely (for example, add a n >> 32 line for 64-bit integers).
    }

    /**
     * Gets the next power of two of a long.
     */
    public static long nextLongPowerOfTwo(long n) {
        // See nextIntPowerOfTwo().

        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n |= n >> 32;
        n++;
        return n;
    }

    /**
     * Gets the position of the highest bit set.
     */
    public static int log2(int n) {
        // See http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogLookup.

        int t;
        int tt;
        return ((tt = n >> 16) != 0) ?
                (((t = tt >> 8) != 0) ? 24 + LOG_TABLE_256[t] : 16 + LOG_TABLE_256[tt]) :
                (((t = n >> 8) != 0) ? 8 + LOG_TABLE_256[t] : LOG_TABLE_256[n]);
    }

    /**
     * Gets the position of the highest bit set.
     */
    public static int log2(long n) {
        // See log2(int).

        int ttt;
        if ((ttt = (int) (n >> 32)) != 0) {
            int tt;
            int t;
            return ((tt = ttt >> 16) != 0) ?
                    (((t = tt >> 8) != 0) ? 56 + LOG_TABLE_256[t] : 48 + LOG_TABLE_256[tt]) :
                    (((t = ttt >> 8) != 0) ? 40 + LOG_TABLE_256[t] : 32 + LOG_TABLE_256[ttt]);
        } else if (n > Integer.MAX_VALUE) {
            return 31;
        } else {
            int n32 = (int) n;
            int tt;
            int t;
            return ((tt = n32 >> 16) != 0) ?
                    (((t = tt >> 8) != 0) ? 24 + LOG_TABLE_256[t] : 16 + LOG_TABLE_256[tt]) :
                    (((t = n32 >> 8) != 0) ? 8 + LOG_TABLE_256[t] : LOG_TABLE_256[n32]);
        }
    }

    private static int[] initializeLogTable256() {
        int[] logTable256 = new int[256];
        logTable256[0] = logTable256[1] = 0;
        for (int i = 2; i < 256; i++) {
            logTable256[i] = 1 + logTable256[i / 2];
        }
        logTable256[0] = -1; // We want log(0) to return -1.
        return logTable256;
    }
}
