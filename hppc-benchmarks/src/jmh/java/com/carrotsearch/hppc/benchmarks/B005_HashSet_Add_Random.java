/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks;

import com.carrotsearch.hppc.XorShift128P;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

// To normalize the measure, run B009_Random to know how much time XorShift128P.nextInt() takes.

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
public class B005_HashSet_Add_Random {
  @Param("0.75")
  public double loadFactor;

  @Param public Library library;

  @Param({"3", "6", "12", "24"})
  public int mbOfKeys;

  public int keyCount;
  public IntSetOps[] ops;
  public XorShift128P rnd;
  public int randomRange;

  @Setup(Level.Trial)
  public void prepare() {
    keyCount = mbOfKeys * (1024 * 1024) / 8;
    ops = new IntSetOps[10];
    for (int i = 0; i < ops.length; i++) {
      ops[i] = library.newIntSet(keyCount, loadFactor);
    }
    rnd = new XorShift128P(0xdeadbeefL);
    randomRange = 2 * keyCount;
    if (Integer.bitCount(randomRange) == 1) {
      // Avoid power-of-2 because XorShift128P.nextInt() is twice faster in this case and that
      // perturbs the measure.
      randomRange++;
    }
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object add() {
    final XorShift128P rnd = this.rnd;
    final int randomRange = this.randomRange;
    for (final IntSetOps ops : ops) {
      for (int i = 0, numLoops = keyCount; i < numLoops; i++) {
        ops.add(rnd.nextInt(randomRange));
      }
    }
    return ops;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder().include(B005_HashSet_Add_Random.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
