/*
 * HPPC
 *
 * Copyright (C) 2010-2026 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the piecewise linear approximation model. */
public class PlaModelTest {

  @Test
  public void testRejectsInvalidEpsilonAndKeyOrder() {
    assertThrows(IllegalArgumentException.class, () -> new PlaModel(-1));

    PlaModel model = new PlaModel(0);
    assertThrows(IllegalArgumentException.class, () -> model.setEpsilon(-1));

    model.addKey(1, 0, (firstKey, slope, intercept) -> {});
    assertThrows(
        IllegalArgumentException.class,
        () -> model.addKey(1, 1, (firstKey, slope, intercept) -> {}));
    assertThrows(
        IllegalArgumentException.class,
        () -> model.addKey(0, 2, (firstKey, slope, intercept) -> {}));
  }

  @Test
  public void testSinglePointSegmentAndReuseAfterFinish() {
    PlaModel model = new PlaModel(2);
    List<Segment> segments = new ArrayList<>();

    model.addKey(
        10,
        5,
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));
    model.finish(
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));

    assertEquals(1, segments.size());
    assertEquals(new Segment(10, 0, 5), segments.getFirst());

    segments.clear();
    model.addKey(
        20,
        0,
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));
    model.finish(
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));

    assertEquals(1, segments.size());
    assertEquals(new Segment(20, 0, 0), segments.getFirst());
  }

  @Test
  public void testExactLinearKeysUseOneExactSegment() {
    double[] keys = {1, 3, 5, 7};
    int[] indexes = {0, 1, 2, 3};
    List<Segment> segments = build(keys, indexes, 0);

    assertEquals(1, segments.size());
    assertEquals(1, segments.getFirst().firstKey);
    assertEquals(0.5, segments.getFirst().slope, 0d);
    assertEquals(0, segments.getFirst().intercept);
    assertApproximation(keys, indexes, 0, segments);
  }

  @Test
  public void testSplitsWhenApproximationNoLongerFits() {
    double[] keys = {0, 1, 3, 6, 10};
    int[] indexes = {0, 1, 2, 3, 4};
    List<Segment> segments = build(keys, indexes, 0);

    assertTrue(segments.size() > 1);
    assertEquals(keys[0], segments.getFirst().firstKey);
    for (int i = 1; i < segments.size(); i++) {
      assertTrue(segments.get(i).firstKey > segments.get(i - 1).firstKey);
    }
    assertApproximation(keys, indexes, 0, segments);
  }

  @Test
  public void testApproximationWithHullUpdatesAndPointListGrowth() {
    int count = 512;
    double[] keys = new double[count];
    int[] indexes = new int[count];
    for (int i = 0; i < count; i++) {
      // The small alternating offset exercises both hull boundaries while keeping all points
      // within the same epsilon range. More than the initial 256 points also exercises list growth.
      keys[i] = i * 100.0 + (i & 1);
      indexes[i] = i;
    }

    List<Segment> segments = build(keys, indexes, 8);

    assertEquals(1, segments.size());
    assertApproximation(keys, indexes, 8, segments);
  }

  @Test
  public void testSaturatesEpsilonBoundsAtIntegerLimits() {
    PlaModel model = new PlaModel(10);
    List<Segment> segments = new ArrayList<>();

    model.addKey(
        1,
        Integer.MAX_VALUE,
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));
    model.finish(
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept)));

    assertEquals(1, segments.size());
    assertEquals(Integer.MAX_VALUE - 5L, segments.getFirst().intercept);
  }

  @Test
  public void testMemoryAccountingIsConsistent() {
    PlaModel model = new PlaModel(1);

    assertEquals(model.ramBytesAllocated(), model.ramBytesUsed());
    model.addKey(0, 0, (firstKey, slope, intercept) -> {});
    model.addKey(1, 1, (firstKey, slope, intercept) -> {});
    assertTrue(model.ramBytesAllocated() > 0);
    assertEquals(model.ramBytesAllocated(), model.ramBytesUsed());
  }

  private static List<Segment> build(double[] keys, int[] indexes, int epsilon) {
    assertEquals(keys.length, indexes.length);
    PlaModel model = new PlaModel(epsilon);
    List<Segment> segments = new ArrayList<>();
    PlaModel.SegmentConsumer consumer =
        (firstKey, slope, intercept) -> segments.add(new Segment(firstKey, slope, intercept));
    for (int i = 0; i < keys.length; i++) {
      model.addKey(keys[i], indexes[i], consumer);
    }
    model.finish(consumer);
    return segments;
  }

  private static void assertApproximation(
      double[] keys, int[] indexes, int epsilon, List<Segment> segments) {
    int segmentIndex = 0;
    for (int i = 0; i < keys.length; i++) {
      while (segmentIndex + 1 < segments.size()
          && keys[i] >= segments.get(segmentIndex + 1).firstKey) {
        segmentIndex++;
      }

      Segment segment = segments.get(segmentIndex);
      double predictedIndex = (keys[i] - segment.firstKey) * segment.slope + segment.intercept;
      double key = keys[i];
      int expectedIndex = indexes[i];
      assertTrue(
          Math.abs(predictedIndex - expectedIndex) <= epsilon + 1e-10,
          () -> "key=" + key + ", expected=" + expectedIndex + ", predicted=" + predictedIndex);
    }
  }

  private static final class Segment {
    final double firstKey;
    final double slope;
    final long intercept;

    Segment(double firstKey, double slope, long intercept) {
      this.firstKey = firstKey;
      this.slope = slope;
      this.intercept = intercept;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Segment that)) {
        return false;
      }
      return Double.compare(firstKey, that.firstKey) == 0
          && Double.compare(slope, that.slope) == 0
          && intercept == that.intercept;
    }

    @Override
    public int hashCode() {
      return 31 * (31 * Double.hashCode(firstKey) + Double.hashCode(slope))
          + Long.hashCode(intercept);
    }
  }
}
