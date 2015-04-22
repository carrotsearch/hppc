package com.carrotsearch.hppc.benchmarks;

public interface BenchmarkDelegate<T>{
  T newInstance();
}
