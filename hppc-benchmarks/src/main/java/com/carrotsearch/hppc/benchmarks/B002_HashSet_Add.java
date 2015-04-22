package com.carrotsearch.hppc.benchmarks;

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
import com.carrotsearch.hppc.benchmarks.implementations.FastutilIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.HppcPhiMixIntSetOps;
import com.carrotsearch.hppc.benchmarks.implementations.KolobokeIntSetOps;

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
public class B002_HashSet_Add {
  public static interface Ops {
    Object addAll(int [] keys);
  }

  @Param("0.75")
  public double loadFactor;

  @Param
  public Library library;

  @Param({"200"})
  public int mbOfKeys;

  public int [] keys;
  public Ops ops;

  @Setup(Level.Iteration)
  public void prepareDelegate() {
    switch (library) {
      case HPPC:        ops = new HppcIntSetOps(keys.length, loadFactor); break;
      case HPPC_PHIMIX: ops = new HppcPhiMixIntSetOps(keys.length, loadFactor); break;
      case FASTUTIL:    ops = new FastutilIntSetOps(keys.length, loadFactor); break;
      case KOLOBOKE:    ops = new KolobokeIntSetOps(keys.length, loadFactor); break;
      default:
        throw new RuntimeException();
    }
  }
  
  @Setup(Level.Trial)
  public void prepare() {
    int keyCount = mbOfKeys * (1024 * 1024) / 4;
    keys = new int [keyCount];

    Random rnd = new XorShiftRandom(0xdeadbeef);
    for (int i = 0; i < keys.length; i++) {
      keys[i] = rnd.nextInt();
    }
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object bulk() {
    return ops.addAll(keys);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B002_HashSet_Add.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
