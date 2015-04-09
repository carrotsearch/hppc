package com.carrotsearch.hppc.generator.parser;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.SyntaxTree;

/** */
final class Replacement {
  public final Interval interval;
  public final String replacement;

  public Replacement(Interval interval, String replacement) {
    this.interval = interval;
    this.replacement = replacement;
  }
  
  public Replacement(SyntaxTree ctx, String replacement) {
    this(ctx.getSourceInterval(), replacement);
  }

  @Override
  public String toString() {
    return interval + " => " + replacement;
  }
}
