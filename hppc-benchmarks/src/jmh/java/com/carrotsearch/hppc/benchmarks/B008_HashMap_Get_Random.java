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
public class B008_HashMap_Get_Random {
  @Param("0.75")
  public double loadFactor;

  @Param public Library library;

  @Param({"16"}) // {"16", "18", "20"}
  public int capacityPowerOf2;

  @Param({"0.45", "0.5", "0.55", "0.6", "0.65", "0.7", "0.74"})
  public float load;

  public int keyCount;
  public IntIntMapOps ops;
  public XorShift128P rnd;
  public int randomRange;

  @Setup(Level.Trial)
  public void prepare() {
    keyCount = (int) ((1 << capacityPowerOf2) * load);
    ops = library.newIntIntMap(keyCount, loadFactor);
    rnd = new XorShift128P(0xdeadbeefL);
    randomRange = 2 * keyCount;
    if (Integer.bitCount(randomRange) == 1) {
      // Avoid power-of-2 because XorShift128P.nextInt() is twice faster in this case and that
      // perturbs the measure.
      randomRange++;
    }

    for (int i = 0; i < keyCount; i++) {
      int key;
      do {
        key = rnd.nextInt(randomRange);
      } while (ops.get(key) != 0);
      ops.put(key, i + 1);
    }
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object get() {
    final IntIntMapOps ops = this.ops;
    final XorShift128P rnd = this.rnd;
    final int randomRange = this.randomRange;
    int v = 0;
    for (int i = 0, numLoops = 50_000_000; i < numLoops; i++) {
      if (ops.get(rnd.nextInt(randomRange)) != 0) {
        v++;
      }
    }
    return v;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(B008_HashMap_Get_Random.class.getSimpleName())
            //            .resultFormat(ResultFormatType.CSV)
            //            .result(args[0])
            .build();
    new Runner(opt).run();
  }
}
