package com.carrotsearch.hppc;

import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory methods to acquire the most common types of 
 * {@link HashOrderMixingStrategy}.
 * 
 * @see HashOrderMixingStrategy
 */
public final class HashOrderMixing {
  public static final String PROPERTY_BIT_MIXER = "hppc.bitmixer";

  private static Strategy strategy;
  
  public enum Strategy implements Callable<HashOrderMixingStrategy> {
    RANDOM {
      @Override
      public HashOrderMixingStrategy call() {
        return randomized();
      }
    },
    DETERMINISTIC {
      @Override
      public HashOrderMixingStrategy call() {
        return deterministic();
      }
    },
    NONE {
      @Override
      public HashOrderMixingStrategy call() {
        return none();
      }
    };
  }

  private HashOrderMixing() {}

  /**
   * @see #deterministic()
   */
  private static final HashOrderMixingStrategy DETERMINISTIC = new HashOrderMixingStrategy() {
    @Override
    public int newKeyMixer(int newContainerBufferSize) {
      return BitMixer.mix32(newContainerBufferSize);
    }
    
    @Override
    public HashOrderMixingStrategy clone() {
      return this;
    }    
  };

  /**
   * Returns a randomized {@link HashOrderMixingStrategy} that issues unique
   * per-container seed. This minimizes the chances of hash distribution conflicts.
   */
  public static HashOrderMixingStrategy randomized() {
    return RandomizedHashOrderMixer.INSTANCE;
  }

  /**
   * A constant {@link HashOrderMixingStrategy}. This is useful if one needs to have
   * deterministic key distribution but wishes to control it manually.
   * 
   * Do not use the same constant for more than one container.
   * 
   * Consider using {@linkplain ObjectScatterSet scatter maps or sets} instead
   * of constant hash order mixer.
   */
  public static HashOrderMixingStrategy constant(final long seed) {
    return new HashOrderMixingStrategy() {
      @Override
      public int newKeyMixer(int newContainerBufferSize) {
        return (int) BitMixer.mix64(newContainerBufferSize ^ seed);
      }

      @Override
      public HashOrderMixingStrategy clone() {
        return this;
      }
    };
  }

  /**
   * Deterministic {@link HashOrderMixingStrategy} will reorder keys depending
   * on the size of the container's buffer.
   * 
   * This is inherently unsafe with hash containers using linear conflict
   * addressing. The only use case when this can be useful is to count/ collect
   * unique keys (for which scatter tables should be used).
   * 
   * @deprecated Permanently deprecated as a warning signal.
   */
  @Deprecated
  public static HashOrderMixingStrategy deterministic() {
    return DETERMINISTIC;
  }

  /**
   * This strategy does not change the hash order of keys at all. This
   * is inherently unsafe with hash containers using linear conflict 
   * addressing. The only use case when this can be useful is to count/ collect
   * unique keys (for which scatter tables should be used).
   * 
   * @deprecated Permanently deprecated as a warning signal.
   */
  @Deprecated
  public static HashOrderMixingStrategy none() {
    return new HashOrderMixingStrategy() {
      @Override
      public int newKeyMixer(int newContainerBufferSize) {
        return 0;
      }

      @Override
      public HashOrderMixingStrategy clone() {
        return this;
      }
    };
  }

  /**
   * Returns the currently configured default {@link HashOrderMixingStrategy}.
   */
  public static HashOrderMixingStrategy defaultStrategy() {
    if (strategy == null) {
      try {
        String propValue = java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
          @Override
          public String run() {
            return System.getProperty(PROPERTY_BIT_MIXER);
          }
        });
        
        if (propValue != null) {
          for (Strategy s : Strategy.values()) {
            if (s.name().equalsIgnoreCase(propValue)) {
              strategy = s;
              break;
            }
          }
        }
      } catch (SecurityException e) {
        // If failed on security exception, don't panic.
        Logger.getLogger(Containers.class.getName())
          .log(Level.INFO, "Failed to read 'tests.seed' property for initial random seed.", e);
      }

      if (strategy == null) {
        strategy = Strategy.RANDOM;
      }
    }

    try {
      return strategy.call();
    } catch (Exception e) {
      throw new RuntimeException(e); // Effectively unreachable.
    }
  }
}
