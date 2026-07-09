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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/* */
@Execution(ExecutionMode.SAME_THREAD)
public class ContainersTest {
  private String savedSeed;

  @BeforeEach
  public void saveSeed() {
    savedSeed = System.getProperty("tests.seed");
  }

  @AfterEach
  public void resetState() {
    if (savedSeed == null) {
      System.clearProperty("tests.seed");
    } else {
      System.setProperty("tests.seed", savedSeed);
    }
    Containers.test$reset();
  }

  @Test
  public void testNoTestsSeed() {
    System.clearProperty("tests.seed");
    Containers.test$reset();

    Assertions.assertThat(Containers.randomSeed64()).isNotEqualTo(Containers.randomSeed64());
  }

  @Test
  public void testWithTestsSeed() {
    System.setProperty("tests.seed", "deadbeef");
    Containers.test$reset();

    Assertions.assertThat(Containers.randomSeed64()).isEqualTo(Containers.randomSeed64());
  }
}
