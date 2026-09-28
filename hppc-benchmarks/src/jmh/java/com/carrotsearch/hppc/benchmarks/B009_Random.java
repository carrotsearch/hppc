/*
 * HPPC
 *
 * Copyright (C) 2010-2026 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.benchmarks;

import com.carrotsearch.hppc.XorShift128P;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@State(Scope.Benchmark)
public class B009_Random {

  @Param({"50000000"})
  public int numLoops;

  public XorShift128P rnd;
  public int randomRange;

  @Setup(Level.Trial)
  public void prepare() {
    rnd = new XorShift128P(0xdeadbeefL);
    randomRange = (1 << 21) + 1;
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object nextInt() {
    final XorShift128P rnd = this.rnd;
    final int randomRange = this.randomRange;
    int v = 0;
    for (int i = 0, numLoops = this.numLoops; i < numLoops; i++) {
      if (rnd.nextInt(randomRange) == 0) {
        v++;
      }
    }
    return v;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B009_Random.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
