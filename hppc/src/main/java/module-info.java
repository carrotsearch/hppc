module com.carrotsearch.hppc {
  requires java.logging;
  requires static jdk.management;

  exports com.carrotsearch.hppc;
  exports com.carrotsearch.hppc.cursors;
  exports com.carrotsearch.hppc.procedures;
  exports com.carrotsearch.hppc.comparators;
  exports com.carrotsearch.hppc.predicates;
  exports com.carrotsearch.hppc.sorting;
  exports com.carrotsearch.hppc.internals;
}
