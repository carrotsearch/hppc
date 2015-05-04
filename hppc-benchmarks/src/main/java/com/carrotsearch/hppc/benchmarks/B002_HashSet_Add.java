package com.carrotsearch.hppc.benchmarks;

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

import com.carrotsearch.hppc.XorShift128P;

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
  public IntSetOps ops;

  @Setup(Level.Iteration)
  public void prepareDelegate() {
    ops = library.newIntSet(keys.length, loadFactor);
  }
  
  @Setup(Level.Trial)
  public void prepare() {
    int keyCount = mbOfKeys * (1024 * 1024) / 4;
    keys = new int [keyCount];

    XorShift128P rnd = new XorShift128P(0xdeadbeefL);
    for (int i = 0; i < keys.length; i++) {
      keys[i] = rnd.nextInt();
    }
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object bulk() {
    ops.bulkAdd(keys);
    return ops;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
      .param("library", "HPPC_SCATTER")
      .include(B002_HashSet_Add.class.getSimpleName())
      .build();
    new Runner(opt).run();
  }
}
