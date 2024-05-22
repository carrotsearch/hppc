/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc;

import java.util.Arrays;

/**
 * Optimal Piecewise Linear Approximation Model for <code>KType</code> keys.
 *
 * <p>Learns a mapping that returns a position for a <code>KType</code> key which is at most epsilon
 * away from the correct one in a sorted list of keys. It is optimal and piecewise because it learns
 * the minimum number of epsilon-approximate segments.
 *
 * <p>The PLA-model consists of a sequence segments. A segment s is a triple (key,slope,intercept)
 * that indexes a range of keys through the function fs(k) = k × slope + intercept, which provides
 * an epsilon-approximation of the position of the key k.
 */
public class PlaModel implements Accountable {

  /** Initial capacity of the lower and upper point lists. */
  private static final int INITIAL_CAPACITY = 1 << 8;

  /** Epsilon precision of the PLA-model. */
  private int epsilon;

  /** First key of the current segment. */
  private double firstKey;

  /** Previous key used to check that keys are added in strictly increasing sequence. */
  private double previousKey;

  /** Number of points in the convex hull for the current segment. */
  private int numPointsInHull;

  /** Enclosing rectangle for the current segment. */
  private final Point[] rect = new Point[4];

  /**
   * Ordered list of lower points for the current segment. Inside the list, allocated points are
   * re-used.
   */
  private final PointList lower = new PointList(INITIAL_CAPACITY);

  /**
   * Ordered list of upper points for the current segment. Inside the list, allocated points are
   * re-used.
   */
  private final PointList upper = new PointList(INITIAL_CAPACITY);

  /** Index of the first lower point to compare to. */
  private int lowerStart;

  /** Index of the first upper point to compare to. */
  private int upperStart;

  // Re-used mutable points and slopes.
  private final Point point1 = new Point();
  private final Point point2 = new Point();
  private final Slope slope1 = new Slope();
  private final Slope slope2 = new Slope();
  private final Slope slopeTmp = new Slope();
  private final Slope slopeMin = new Slope();
  private final Slope slopeMax = new Slope();

  /**
   * Creates an optimal PLA-model with the provided epsilon precision.
   *
   * @param epsilon must be greater than or equal to 0.
   */
  public PlaModel(int epsilon) {
    setEpsilon(epsilon);
    for (int i = 0; i < rect.length; i++) {
      rect[i] = new Point();
    }
    reset();
  }

  /** Sets epsilon precision which must be greater than or equal to 0. */
  public void setEpsilon(int epsilon) {
    if (epsilon < 0) {
      throw new IllegalArgumentException("epsilon must be >= 0");
    }
    this.epsilon = epsilon;
  }

  private void reset() {
    previousKey = Double.NEGATIVE_INFINITY;
    numPointsInHull = 0;
    lower.clear();
    upper.clear();
  }

  /**
   * Adds a key to this PLA-model. The keys must be provided in a strictly increasing sequence. That
   * is, the key must be greater than the previous key.
   *
   * @param index The index of the key in the sorted key list.
   * @param segmentConsumer The consumer to call when a new segment is built in the PLA-model.
   */
  public void addKey(double key, int index, SegmentConsumer segmentConsumer) {
    if (key <= previousKey) {
      throw new IllegalArgumentException("Keys must be increasing");
    }
    previousKey = key;
    point1.set(key, addEpsilon(index));
    point2.set(key, subtractEpsilon(index));

    if (numPointsInHull > 1) {
      slope1.set(rect[0], rect[2]);
      slope2.set(rect[1], rect[3]);
      boolean outside_line1 = slopeTmp.set(rect[2], point1).isLessThan(slope1);
      boolean outside_line2 = slopeTmp.set(rect[3], point2).isGreaterThan(slope2);
      if (outside_line1 || outside_line2) {
        produceSegment(segmentConsumer);
        numPointsInHull = 0;
      }
    }
    if (numPointsInHull == 0) {
      firstKey = key;
      rect[0].set(point1);
      rect[1].set(point2);
      upper.clear();
      lower.clear();
      upper.add(point1);
      lower.add(point2);
      upperStart = lowerStart = 0;
      numPointsInHull++;
      return;
    }
    if (numPointsInHull == 1) {
      rect[2].set(point2);
      rect[3].set(point1);
      upper.add(point1);
      lower.add(point2);
      numPointsInHull++;
      return;
    }

    if (slopeTmp.set(rect[1], point1).isLessThan(slope2)) {
      // Find extreme slope.
      slopeMin.set(point1, lower.get(lowerStart));
      int min_i = lowerStart;
      for (int i = lowerStart + 1; i < lower.size(); i++) {
        slopeTmp.set(point1, lower.get(i));
        if (slopeTmp.isGreaterThan(slopeMin)) {
          break;
        }
        slopeMin.set(slopeTmp);
        min_i = i;
      }
      rect[1].set(lower.get(min_i));
      rect[3].set(point1);
      lowerStart = min_i;

      // Hull update.
      int end = upper.size();
      while (end >= upperStart + 2 && cross(upper.get(end - 2), upper.get(end - 1), point1) <= 0) {
        end--;
      }
      upper.clearFrom(end);
      upper.add(point1);
    }

    if (slopeTmp.set(rect[0], point2).isGreaterThan(slope1)) {
      // Find extreme slope.
      slopeMax.set(point2, upper.get(upperStart));
      int max_i = upperStart;
      for (int i = upperStart + 1; i < upper.size(); i++) {
        slopeTmp.set(point2, upper.get(i));
        if (slopeTmp.isLessThan(slopeMax)) {
          break;
        }
        slopeMax.set(slopeTmp);
        max_i = i;
      }
      rect[0].set(upper.get(max_i));
      rect[2].set(point2);
      upperStart = max_i;

      // Hull update.
      int end = lower.size();
      while (end >= lowerStart + 2 && cross(lower.get(end - 2), lower.get(end - 1), point2) >= 0) {
        end--;
      }
      lower.clearFrom(end);
      lower.add(point2);
    }

    numPointsInHull++;
  }

  private void produceSegment(SegmentConsumer segmentConsumer) {
    double slope;
    long intercept;

    if (numPointsInHull == 1) {
      slope = 0d;
      intercept = ((long) rect[0].y + rect[1].y) >>> 1;

    } else {
      Point p0 = rect[0];
      Point p1 = rect[1];
      Point p2 = rect[2];
      Point p3 = rect[3];

      // Compute the slope intersection point.
      double intersectX;
      double intersectY;
      slope1.set(p0, p2);
      slope2.set(p1, p3);
      if (slope1.isEqual(slope2)) {
        intersectX = p0.x;
        intersectY = p0.y;
      } else {
        slopeTmp.set(p0, p1);
        double a = slope1.dx * slope2.dy - slope1.dy * slope2.dx;
        double b = (slopeTmp.dx * slope2.dy - slopeTmp.dy * slope2.dx) / a;
        intersectX = p0.x + b * slope1.dx;
        intersectY = p0.y + b * slope1.dy;
      }

      // Compute the slope range.
      double minSlope = Slope.asDouble(p0, p2);
      double maxSlope = Slope.asDouble(p1, p3);

      // Compute the segment slope and intercept.
      slope = (minSlope + maxSlope) / 2d;
      intercept = (long) (intersectY - (intersectX - firstKey) * slope);
    }

    segmentConsumer.accept(firstKey, slope, intercept);
  }

  /**
   * Finishes the PLA-model construction. Declares that no additional keys will be added. Builds the
   * last segment and calls the provided {@link SegmentConsumer}.
   */
  public void finish(SegmentConsumer segmentConsumer) {
    produceSegment(segmentConsumer);
    reset();
  }

  @Override
  public long ramBytesAllocated() {
    // int: epsilon, numPointsInHull, lowerStart, upperStart
    // double: firstKey, previousKey
    // Point: rect[4], point1, point2
    // Slope: slope1, slope2, slopeTmp, slopeMin, slopeMax
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + 4 * Integer.BYTES
        + 2 * Double.BYTES
        + 6L * Point.RAM_BYTES_ALLOCATED
        + lower.ramBytesAllocated()
        + upper.ramBytesAllocated()
        + 5L * Slope.RAM_BYTES_ALLOCATED;
  }

  @Override
  public long ramBytesUsed() {
    return ramBytesAllocated();
  }

  private int addEpsilon(int index) {
    try {
      return Math.addExact(index, epsilon);
    } catch (ArithmeticException e) {
      return Integer.MAX_VALUE;
    }
  }

  private int subtractEpsilon(int index) {
    try {
      return Math.subtractExact(index, epsilon);
    } catch (ArithmeticException e) {
      return Integer.MIN_VALUE;
    }
  }

  private static double cross(Point o, Point a, Point b) {
    return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
  }

  /** Consumer notified when a new segment is built by the {@link PlaModel}. */
  public interface SegmentConsumer {

    /**
     * Consumes a new segment. The segment is defined by the epsilon-approximation function fs(k) =
     * k × slope + intercept.
     *
     * @param firstKey The first key of the segment.
     * @param slope The segment slope.
     * @param intercept The segment intercept.
     */
    void accept(double firstKey, double slope, long intercept);
  }

  /** Re-usable mutable (x,y) point. */
  private static class Point {

    static final int RAM_BYTES_ALLOCATED =
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Double.BYTES + Long.BYTES;

    double x;
    long y;

    Point set(double x, long y) {
      this.x = x;
      this.y = y;
      return this;
    }

    Point set(Point p) {
      return set(p.x, p.y);
    }
  }

  /** List of mutable {@link Point}. Re-uses allocated points instead of creating new instances. */
  private static class PointList implements Accountable {

    Point[] points;
    int size;
    int numAllocated;

    PointList(int initialCapacity) {
      points = new Point[initialCapacity];
    }

    void add(Point point) {
      if (size == points.length) {
        int newSize =
            BoundedProportionalArraySizingStrategy.DEFAULT_INSTANCE.grow(points.length, size, 1);
        points = Arrays.copyOf(points, newSize);
      }
      if (size == numAllocated) {
        points[numAllocated++] = new Point();
      }
      points[size++].set(point);
    }

    Point get(int index) {
      return points[index];
    }

    int size() {
      return size;
    }

    void clear() {
      size = 0;
    }

    void clearFrom(int end) {
      size = end;
    }

    @Override
    public long ramBytesAllocated() {
      // int: size, numAllocated
      return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
          + 2 * Integer.BYTES
          + RamUsageEstimator.shallowSizeOfArray(points)
          + (long) numAllocated * Point.RAM_BYTES_ALLOCATED;
    }

    @Override
    public long ramBytesUsed() {
      return ramBytesAllocated();
    }
  }

  /** Re-usable mutable (dx,dy) slope. */
  private static class Slope {

    static final int RAM_BYTES_ALLOCATED =
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Double.BYTES + Long.BYTES;

    double dx;
    long dy;

    void set(Slope s) {
      dx = s.dx;
      dy = s.dy;
    }

    Slope set(Point p1, Point p2) {
      dx = p2.x - p1.x;
      dy = p2.y - p1.y;
      return this;
    }

    boolean isLessThan(Slope s) {
      return dy * s.dx < dx * s.dy;
    }

    boolean isGreaterThan(Slope s) {
      return dy * s.dx > dx * s.dy;
    }

    boolean isEqual(Slope s) {
      return Double.doubleToLongBits(dy * s.dx) == Double.doubleToLongBits(dx * s.dy);
    }

    static double asDouble(Point p1, Point p2) {
      return (double) (p2.y - p1.y) / (p2.x - p1.x);
    }
  }
}
