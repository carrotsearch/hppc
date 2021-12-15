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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class B001_ModXor {
  private long v;
  private long mask = 0x238751;

  @Benchmark
  public long modOp() {
    v = v % mask++;
    return v;
  }

  @Benchmark
  public long xorOp() {
    v = v ^ mask++;
    return v;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B001_ModXor.class.getSimpleName()).build();

    new Runner(opt).run();
  }
}
