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

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
public class B004_HashSet_CollisionAvalanche {
  @Param("0.75")
  public double loadFactor;

  @Param
  public Library library;

  private IntSetOps source;
  private IntSetOps target;
  private int[] keys;
  
  @Setup(Level.Trial)
  public void prepare() {
    // make sure we have nearly full load (dense source)
    int keyCount = (int) Math.ceil((1 << 19) / loadFactor) - 5000;
    int [] keys = new int [keyCount];
    for (int i = keyCount; i-- != 0;) {
      keys[i] = i;
    }

    source = library.newIntSet(0, loadFactor);
    source.bulkAdd(keys);

    this.keys = source.iterationOrderArray();
  }

  @Setup(Level.Iteration)
  public void prepareDelegate() {
    target = library.newIntSet(0, loadFactor);
  }

  @Benchmark()
  @BenchmarkMode(Mode.SingleShotTime)
  public Object run() {
    target.bulkAdd(keys);
    return target;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(B004_HashSet_CollisionAvalanche.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
