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

import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import org.junit.jupiter.api.Test;

public class PlaModelTest extends RandomizedTest {
  @Test
  public void testFinishWithoutKeysProducesNoSegment() {
    PlaModel plam = new PlaModel(4);
    IntArrayList segments = new IntArrayList();
    PlaModel.SegmentConsumer consumer = (firstKey, slope, intercept) -> segments.add(0);
    plam.finish(consumer);
    assertEquals(0, segments.size());

    // The model is still usable afterwards.
    plam.addKey(1, 0, consumer);
    plam.finish(consumer);
    assertEquals(1, segments.size());
  }

  @Test
  public void testNaNKeyRejected() {
    PlaModel plam = new PlaModel(4);
    PlaModel.SegmentConsumer consumer = (firstKey, slope, intercept) -> {};
    plam.addKey(1, 0, consumer);
    assertThrows(IllegalArgumentException.class, () -> plam.addKey(Double.NaN, 1, consumer));
  }
}
