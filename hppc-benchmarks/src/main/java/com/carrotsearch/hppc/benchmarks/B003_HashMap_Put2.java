package com.carrotsearch.hppc.benchmarks;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.Random;

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

import com.carrotsearch.hppc.XorShiftRandom;

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class B003_HashMap_Put2 {
  @Param("44")
  public long seed;

  @Param("0.75")
  public double loadFactor;

  public int [] keys;
  public IntOpenHashSet set;

  @Setup(Level.Trial) 
  public void prepare() {
    keys = new int [1024 * 1024 * 50];
    Random rnd = new XorShiftRandom(seed);
    for (int i = 0; i < keys.length; i++) {
      keys[i] = rnd.nextInt();
    }

    set = new IntOpenHashSet(keys.length, (float) loadFactor);
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object put() {
    for (int value : keys) {
      set.add(value);
    }
    return set;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B003_HashMap_Put2.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
