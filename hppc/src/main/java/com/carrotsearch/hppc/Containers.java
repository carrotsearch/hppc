package com.carrotsearch.hppc;

import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constants used as defaults in containers.
 * 
 * @see HashContainers
 */
public final class Containers {
  /**
   * The default number of expected elements for containers.
   */
  public final static int DEFAULT_EXPECTED_ELEMENTS = 4;
  
  /**
   * External initial seed value. We do not care about multiple assignments 
   * so not volatile.
   * 
   * @see #randomSeed64()
   */
  private static String testsSeedProperty;
  
  /**
   * Unique marker for {@link #testsSeedProperty}.
   */
  private final static String NOT_AVAILABLE = new String();

  private Containers() {}

  /**
   * Provides a (possibly) random initial seed for randomized stuff. 
   * 
   * If <code>tests.seed</code> property is available and accessible,
   * the returned value will be derived from the value of that property
   * and will be constant to ensure reproducibility in presence of the 
   * randomized testing package.
   * 
   * @see "https://github.com/carrotsearch/randomizedtesting"
   */
  @SuppressForbidden
  public static long randomSeed64() {
    if (testsSeedProperty == null) {
      try {
        testsSeedProperty = java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
          @Override
          public String run() {
            return System.getProperty("tests.seed", NOT_AVAILABLE);
          }
        });
      } catch (SecurityException e) {
        // If failed on security exception, don't panic.
        testsSeedProperty = NOT_AVAILABLE;
        Logger.getLogger(Containers.class.getName())
          .log(Level.INFO, "Failed to read 'tests.seed' property for initial random seed.", e);
      }
    }

    long initialSeed;
    if (testsSeedProperty != NOT_AVAILABLE) {
      initialSeed = testsSeedProperty.hashCode();
    } else {
      // Mix something that is changing over time (nanoTime)
      // ... with something that is thread-local and relatively unique 
      //     even for very short time-spans (new Object's address from a TLAB).
      initialSeed = System.nanoTime() ^ 
                    System.identityHashCode(new Object());
    }
    return BitMixer.mix64(initialSeed);
  }

  /**
   * Reset state for tests.
   */
  static void test$reset() {
    testsSeedProperty = null;
  }
}
