package com.carrotsearch.hppc.examples;

import java.lang.reflect.Array;
import java.util.*;

import org.apache.lucene.util.RamUsageEstimator;
import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.hppc.XorShiftRandom;

/**
 * An example of using pseudo-valuetypes and generated dense arrays.
 */
public class StructExample
{
    private Random rnd;
    private int reps;

    @Before
    public void setup()
    {
        rnd = new XorShiftRandom(0xdeadbeef);
        reps = 10000000;
    }

    /**
     * Compare in-memory size estimates for a relatively simple, two-dimensional array.
     */
    @Test
    public void compareStructureArrayAndJavaArraySize()
    {
        int x = 2000, y = 2000;
        BattleshipsCell [][] board = (BattleshipsCell [][]) buildArray(BattleshipsCell.class, x, y);
        BattleshipsCellArray2D board2 = new BattleshipsCellArray2D(x, y);

        RamUsageEstimator estimator = new RamUsageEstimator();
        System.out.println(
            String.format(Locale.ENGLISH, 
                "%10s: %,15d\n%10s: %,15d",
                "Java", estimator.estimateRamUsage(board), 
                "Struct", estimator.estimateRamUsage(board2)));
    }

    /**
     * Create a "board" for a battleships game.
     */
    @Test
    public void testStructUsingScratchObject()
    {
        int x = 200, y = 100;
        BattleshipsCellArray2D board = new BattleshipsCellArray2D(x, y);

        // Place some random values on the board.
        BattleshipsCell scratch = new BattleshipsCell();
        for (int i = 0; i < reps; i++)
        {
            int px = rnd.nextInt(x);
            int py = rnd.nextInt(y);

            scratch.damageLevel = rnd.nextInt(10);
            scratch.hasShip = rnd.nextBoolean();
            scratch.hit = rnd.nextBoolean();
            scratch.owner = (byte) rnd.nextInt();
            board.set(scratch, px, py);
        }
    }

    /**
     * Create a "board" for a battleships game.
     */
    @Test
    public void testStructDirectFieldAccessors()
    {
        int x = 200, y = 100;
        BattleshipsCellArray2D board = new BattleshipsCellArray2D(x, y);

        // Place some random values on the board.
        for (int i = 0; i < reps; i++)
        {
            int px = rnd.nextInt(x);
            int py = rnd.nextInt(y);

            board.setDamageLevel(rnd.nextInt(10), px, py);
            board.setHasShip(rnd.nextBoolean(), px, py);
            board.setHit(rnd.nextBoolean(), px, py);
            board.setOwner((byte) rnd.nextInt(), px, py);
        }
    }

    /**
     * Create a "board" for a battleships game.
     */
    @Test
    public void testJavaArraysEquivalent()
    {
        int x = 200, y = 100;
        BattleshipsCell [][] board;
        board = (BattleshipsCell [][]) buildArray(BattleshipsCell.class, x, y);

        // Place some random values on the board.
        for (int i = 0; i < reps; i++)
        {
            int px = rnd.nextInt(x);
            int py = rnd.nextInt(y);

            board[px][py].damageLevel = rnd.nextInt(10);
            board[px][py].hasShip = rnd.nextBoolean();
            board[px][py].hit = rnd.nextBoolean();
            board[px][py].owner = (byte) rnd.nextInt();
        }
    }

    /**
     * Build a dense, multidimensional array using reflection.
     */
    @SuppressWarnings("unchecked")
    private <T> Object buildArray(Class<T> componentType, int... dimensions)
    {
        try
        {
            final int dim = dimensions[0];
            if (dimensions.length == 1)
            {
                T [] array = (T []) Array.newInstance(componentType, dim);
                for (int i = 0; i < dim; i++)
                    array[i] = componentType.newInstance();
                return array;
            }
            else
            {
                Object array = Array.newInstance(componentType, dimensions);
                dimensions = Arrays.copyOfRange(dimensions, 1, dimensions.length);
                for (int i = 0; i < dim; i++)
                {
                    Array.set(array, i, buildArray(componentType, dimensions));
                }
                return array;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
