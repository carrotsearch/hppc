package com.carrotsearch.hppc.caliper;

import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppc.BigramCounting;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Caliper version of {@link BigramCounting}.
 */
public class BenchmarkPopCnt extends SimpleBenchmark
{
    long [] seq;

    @Param public Distribution distribution;

    public static enum Distribution
    {
        ZEROS,
        FULL,
        RANDOM,
        ONEBIT
    }

    @Override
    protected void setUp() throws Exception
    {
        seq = new long [1000000];
        
        Random rnd = new Random(0xdeadbeef);
        switch (distribution) {
            case ZEROS:
                break;
            case FULL:
                Arrays.fill(seq, -1);
                break;
            case RANDOM:
                for (int i = 0; i < seq.length; i++) {
                    seq[i] = rnd.nextLong();
                }
                break;
            case ONEBIT:
                for (int i = 0; i < seq.length; i++) {
                    seq[i] = 1L << rnd.nextInt(64);
                }
                break;
        }
    }
    
    public int timeLongBitCount(int reps) {
        int v = 0;
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < seq.length; j++) {
                v += Long.bitCount(seq[j]);
            }
        }
        return v;
    }

    public int timeHdPopCnd(int reps) {
        int v = 0;
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < seq.length; j++) {
                v += hdBitCount(seq[j]);
            }
        }
        return v;
    }

    public int timeRank9(int reps) {
        int v = 0;
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < seq.length; j++) {
                v += rank9(seq[j]);
            }
        }
        return v;
    }

    public int _timeNaivePopCnt(int reps) {
        int v = 0;
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < seq.length; j++) {
                v += naivePopCnt(seq[j]);
            }
        }
        return v;
    }

    public static int naivePopCnt(long x) {
        int cnt = 0;
        while (x != 0) {
            if (((x >>>= 1) & 1) != 0L) {
                cnt++;
            }
        }
        return cnt;
    }

    public static int hdBitCount(long i) {
        // HD, Figure 5-14
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        i = i + (i >>> 32);
        return (int)i & 0x7f;
     }

    public static int rank9(long x) {
        // Step 0 leaves in each pair of bits the number of ones originally contained in that pair:
        x = x - ((x & 0xAAAAAAAAAAAAAAAAL) >>> 1);
        // Step 1, idem for each nibble:
        x = (x & 0x3333333333333333L) + ((x >>> 2) & 0x3333333333333333L);
        // Step 2, idem for each byte:
        x = (x + (x >>> 4)) & 0x0F0F0F0F0F0F0F0FL;
        // Multiply to sum them all into the high byte, and return the high byte:
        return (int) ((x * BroadWord.L8_L) >>> 56);
      }

    public static void main(String [] args)
    {
        Runner.main(BenchmarkPopCnt.class, args);
    }
}
