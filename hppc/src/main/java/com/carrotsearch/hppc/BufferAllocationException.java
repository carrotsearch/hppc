package com.carrotsearch.hppc;

import java.util.IllegalFormatException;
import java.util.Locale;

@SuppressWarnings("serial")
public class BufferAllocationException extends RuntimeException {
  public BufferAllocationException(String message) {
    super(message);
  }

  public BufferAllocationException(String message, Object... args) {
    this(message, null, args);
  }

  public BufferAllocationException(String message, Throwable t, Object... args) {
    super(formatMessage(message, t, args), t);
  }

  private static String formatMessage(String message, Throwable t, Object... args) {
    try {
      return String.format(Locale.ROOT, message, args);
    } catch (IllegalFormatException e) {
      BufferAllocationException substitute = 
          new BufferAllocationException(message + " [ILLEGAL FORMAT, ARGS SUPPRESSED]");
      if (t != null) { 
        substitute.addSuppressed(t);
      }
      substitute.addSuppressed(e);
      throw substitute;
    }
  }
}
