package com.carrotsearch.hppc.generator.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.parser.Java7Parser.CompilationUnitContext;

/** */
public class SignatureProcessor {
  final Java7Parser parser;
  final CommonTokenStream tokenStream;
  final CompilationUnitContext unitContext;

  public SignatureProcessor(String input) {
    Lexer lexer = new Java7Lexer(new ANTLRInputStream(input));
    tokenStream = new CommonTokenStream(lexer);
    parser = new Java7Parser(tokenStream);
    parser.setErrorHandler(new BailErrorStrategy());
    unitContext = parser.compilationUnit();
  }

  /*
   * 
   */
  public String process(TemplateOptions templateOptions) throws IOException {
    return applyReplacements(findReplacements(templateOptions), templateOptions);
  }

  /*
   * 
   */
  private List<Replacement> findReplacements(TemplateOptions templateOptions) {
    List<Replacement> replacements = unitContext.accept(new SignatureReplacementVisitor(templateOptions, this));
    return replacements;
  }

  /*
   * 
   */
  private String applyReplacements(List<Replacement> replacements, TemplateOptions options) throws IOException {
    StringWriter sw = new StringWriter();
    reconstruct(sw, tokenStream, 0, tokenStream.size() - 1, replacements, options);
    return sw.toString();
  }

  /**
   * Process references inside comment blocks, javadocs, etc.
   */
  private String processComment(String text, TemplateOptions options) {
    if (options.hasKType()) {
      text = text.replaceAll("(KType)(?=\\p{Lu})", options.getKType().getBoxedType());
      text = text.replace("KType", options.getKType().getType());
    }
    if (options.hasVType()) {
      text = text.replaceAll("(VType)(?=\\p{Lu})", options.getVType().getBoxedType());
      text = text.replace("VType", options.getVType().getType());
    }
    return text;
  }

  /*
   * 
   */
  <T extends Writer> T reconstruct(
      T sw, 
      BufferedTokenStream tokenStream, 
      int from, int to, 
      Collection<Replacement> replacements,
      TemplateOptions templateOptions) throws IOException {
    
    ArrayList<Replacement> sorted = new ArrayList<>(replacements);
    Collections.sort(sorted, new Comparator<Replacement>() {
      @Override
      public int compare(Replacement a, Replacement b) {
        return Integer.compare(a.interval.a, b.interval.b);
      }
    });

    for (int i = 1; i < sorted.size(); i++) {
      Replacement previous = sorted.get(i - 1);
      Replacement current = sorted.get(i);
      if (!previous.interval.startsBeforeDisjoint(current.interval)) {
        throw new RuntimeException("Overlapping intervals: " + previous + " " + current);
      }
    }

    int left = from;
    for (Replacement r : sorted) {
      int right = r.interval.a;
      for (int i = left; i < right; i++) {
        sw.append(tokenText(templateOptions, tokenStream.get(i)));
      }
      sw.append(r.replacement);
      left = r.interval.b + 1;
    }

    for (int i = left; i < to; i++) {
      sw.append(tokenText(templateOptions, tokenStream.get(i)));
    }
    return sw;
  }

  protected String tokenText(TemplateOptions templateOptions, Token token) {
    String text = token.getText();
    if (token.getChannel() == Java7Lexer.CHANNEL_COMMENT) {
      text = processComment(text, templateOptions);
    }
    return text;
  }
}
