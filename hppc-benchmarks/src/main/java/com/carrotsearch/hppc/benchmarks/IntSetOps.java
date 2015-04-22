package com.carrotsearch.hppc.benchmarks;

public interface IntSetOps {
  void add(int key);

  void bulkAdd(int [] keys);
  int  bulkContains(int [] keys);

  int[] iterationOrderArray();
}
