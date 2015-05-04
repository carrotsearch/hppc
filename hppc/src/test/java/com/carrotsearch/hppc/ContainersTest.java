package com.carrotsearch.hppc;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

/* */
public class ContainersTest extends RandomizedTest {
  @Rule
  public final TestRule rules = RuleChain.outerRule(
      new SystemPropertiesRestoreRule());
  
  @After
  public void resetState() {
    Containers.test$reset();
  }

  @Test
  public void testNoTestsSeed() {
    System.clearProperty("tests.seed");
    Containers.test$reset();

    Assertions.assertThat(Containers.randomSeed64())
              .isNotEqualTo(Containers.randomSeed64());
  }
  
  @Test
  public void testWithTestsSeed() {
    System.setProperty("tests.seed", "deadbeef");
    Containers.test$reset();

    Assertions.assertThat(Containers.randomSeed64())
              .isEqualTo(Containers.randomSeed64());
  }
}
