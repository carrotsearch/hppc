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
public class B003_HashSet_Contains {
  @Param("0.75")
  public double loadFactor;

  @Param
  public Library library;

  @Param({"200"})
  public int mbOfKeys;

  public int [] keys;
  public IntSetOps ops;
  
  @Setup(Level.Trial)
  public void prepare() {
    int keyCount = mbOfKeys * (1024 * 1024) / 4;
    keys = new int [keyCount];

    XorShift128P rnd = new XorShift128P(0xdeadbeefL);
    for (int i = 0; i < keys.length; i++) {
      keys[i] = rnd.nextInt(2 * keys.length);
    }

    ops = library.newIntSet(keys.length, loadFactor);

    int[] existing = new int [keyCount];
    for (int i = 0; i < keys.length; i++) {
      existing[i] = rnd.nextInt(2 * keys.length);
    }
    ops.bulkAdd(existing);
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object bulk() {
    return ops.bulkContains(keys);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B003_HashSet_Contains.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
