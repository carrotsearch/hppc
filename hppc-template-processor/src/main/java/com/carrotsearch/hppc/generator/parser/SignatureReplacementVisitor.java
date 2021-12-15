/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.generator.parser;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;
import com.carrotsearch.hppc.generator.parser.JavaParser.ClassDeclarationContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.ClassOrInterfaceTypeContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.ConstructorDeclarationContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.CreatedNameContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.GenericMethodDeclarationContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.InterfaceDeclarationContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.MethodDeclarationContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.PrimaryContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.QualifiedNameContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.TypeArgumentContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.TypeArgumentsContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.TypeBoundContext;
import com.carrotsearch.hppc.generator.parser.JavaParser.TypeParameterContext;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

class SignatureReplacementVisitor extends JavaParserBaseVisitor<List<Replacement>> {
  private static final List<Replacement> NONE = Collections.emptyList();
  private final TemplateOptions templateOptions;
  private final SignatureProcessor processor;

  public SignatureReplacementVisitor(
      TemplateOptions templateOptions, SignatureProcessor processor) {
    this.templateOptions = templateOptions;
    this.processor = processor;
  }

  private static class TypeBound {
    private final String originalBound;
    private final Type targetType;

    public TypeBound(Type targetType, String originalBound) {
      this.targetType = targetType;
      this.originalBound = originalBound;
    }

    public Type templateBound() {
      checkArgument(targetType != null, "Target not a template bound: " + originalBound);
      return targetType;
    }

    public boolean isTemplateType() {
      return targetType != null;
    }

    public String originalBound() {
      return originalBound;
    }

    public String getBoxedType() {
      return templateBound().getBoxedType();
    }

    @Override
    public String toString() {
      return String.format(Locale.ROOT, "Bound(original=%s, target=%s)", originalBound, targetType);
    }
  }

  private TypeBound typeBoundOf(TypeParameterContext c) {
    String symbol = c.IDENTIFIER().getText();
    switch (symbol) {
      case "KType":
        return new TypeBound(templateOptions.getKType(), c.getText());
      case "VType":
        return new TypeBound(templateOptions.getVType(), c.getText());
      default:
        return new TypeBound(null, c.getText());
    }
  }

  private TypeBound typeBoundOf(TypeArgumentContext c, Deque<Type> wildcards) {
    if (c.getText().equals("?")) {
      return new TypeBound(wildcards.removeFirst(), c.getText());
    }

    TypeBound t = typeBoundOf(c.typeType());
    if (t.isTemplateType()) {
      return new TypeBound(t.templateBound(), getSourceText(c));
    } else {
      return new TypeBound(null, getSourceText(c));
    }
  }

  private String getSourceText(ParserRuleContext c) {
    return this.processor.tokenStream.getText(c.getSourceInterval());
  }

  private TypeBound typeBoundOf(JavaParser.TypeTypeContext c) {
    if (c.primitiveType() != null) {
      return new TypeBound(null, c.getText());
    } else {
      TypeBound t = typeBoundOf(c.classOrInterfaceType());
      if (t.isTemplateType()) {
        return new TypeBound(t.templateBound(), c.getText());
      } else {
        return new TypeBound(null, c.getText());
      }
    }
  }

  private TypeBound typeBoundOf(ClassOrInterfaceTypeContext c) {
    checkArgument(
        c.identifierTypePair().size() == 1, "Unexpected typeBoundOf context: " + c.getText());

    for (JavaParser.IdentifierTypePairContext p : c.identifierTypePair()) {
      switch (p.IDENTIFIER().getText()) {
        case "KType":
          return new TypeBound(templateOptions.getKType(), p.getText());
        case "VType":
          return new TypeBound(templateOptions.getVType(), p.getText());
      }
    }

    return new TypeBound(null, c.getText());
  }

  public List<Replacement> visitClassDeclaration(ClassDeclarationContext ctx) {
    List<Replacement> result = new ArrayList<>(super.visitClassDeclaration(ctx));

    String className = ctx.IDENTIFIER().getText();
    if (isTemplateIdentifier(className) || true) {
      List<TypeBound> typeBounds = new ArrayList<>();
      if (ctx.typeParameters() != null) {
        for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
          typeBounds.add(typeBoundOf(c));
        }
        result.add(new Replacement(ctx.typeParameters(), toString(typeBounds)));
      }

      Iterator<TypeBound> templateBounds =
          typeBounds.stream()
              .filter(TypeBound::isTemplateType)
              .collect(Collectors.toList())
              .iterator();

      if (className.contains("KType")) {
        className =
            className.replace("KType", templateBounds.next().templateBound().getBoxedType());
      }
      if (className.contains("VType")) {
        className =
            className.replace("VType", templateBounds.next().templateBound().getBoxedType());
      }
      result.add(new Replacement(ctx.IDENTIFIER(), className));
    }

    return result;
  }

  private TypeBound lookup(List<TypeBound> typeBounds, String name) {
    for (TypeBound bound : typeBounds) {
      if (bound.isTemplateType() && bound.originalBound().equals(name)) {
        return bound;
      }
    }
    throw new RuntimeException(
        String.format(Locale.ROOT, "Type bound for %s not found among: %s", name, typeBounds));
  }

  @Override
  public List<Replacement> visitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
    List<Replacement> result = super.visitInterfaceDeclaration(ctx);

    String className = ctx.IDENTIFIER().getText();
    if (isTemplateIdentifier(className)) {
      List<TypeBound> typeBounds = new ArrayList<>();
      for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
        typeBounds.add(typeBoundOf(c));
      }
      Replacement replaceGenericTypes = new Replacement(ctx.typeParameters(), toString(typeBounds));

      Iterator<TypeBound> templateBounds =
          typeBounds.stream()
              .filter(TypeBound::isTemplateType)
              .collect(Collectors.toList())
              .iterator();

      if (className.contains("KType")) {
        className =
            className.replace("KType", templateBounds.next().templateBound().getBoxedType());
      }
      if (className.contains("VType")) {
        className =
            className.replace("VType", templateBounds.next().templateBound().getBoxedType());
      }
      Replacement replaceIdentifier = new Replacement(ctx.IDENTIFIER(), className);

      result = new ArrayList<>(result);
      result.addAll(Arrays.asList(replaceIdentifier, replaceGenericTypes));
    }

    return result;
  }

  @Override
  public List<Replacement> visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
    return processIdentifier(ctx.IDENTIFIER(), super.visitConstructorDeclaration(ctx));
  }

  @Override
  public List<Replacement> visitPrimary(PrimaryContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    if (identifier != null && isTemplateIdentifier(identifier.getText())) {
      return processIdentifier(identifier, NONE);
    } else {
      return super.visitPrimary(ctx);
    }
  }

  @Override
  public List<Replacement> visitGenericMethodDeclaration(GenericMethodDeclarationContext ctx) {
    ArrayList<String> bounds = new ArrayList<>();
    for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
      switch (c.IDENTIFIER().getText()) {
        case "KType":
          checkArgument(
              c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
          if (templateOptions.isKTypeGeneric()) {
            bounds.add(c.getText());
          }
          break;

        case "VType":
          checkArgument(
              c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
          if (templateOptions.isVTypeGeneric()) {
            bounds.add(c.getText());
          }
          break;

        default:
          TypeBoundContext tbc = c.typeBound();
          if (tbc != null) {
            checkArgument(
                tbc.typeType().size() == 1, "Expected exactly one type bound: " + c.getText());
            JavaParser.TypeTypeContext tctx = tbc.typeType().get(0);
            Interval sourceInterval = tbc.getSourceInterval();
            try {
              StringWriter sw =
                  processor.reconstruct(
                      new StringWriter(),
                      processor.tokenStream,
                      sourceInterval.a,
                      sourceInterval.b,
                      visitTypeType(tctx),
                      templateOptions);
              bounds.add(c.IDENTIFIER() + " extends " + sw.toString());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } else {
            bounds.add(c.getText());
          }
          break;
      }
    }

    List<Replacement> replacements = new ArrayList<>();
    if (bounds.isEmpty()) {
      replacements.add(new Replacement(ctx.typeParameters(), ""));
    } else {
      replacements.add(new Replacement(ctx.typeParameters(), "<" + join(", ", bounds) + ">"));
    }

    replacements.addAll(super.visitMethodDeclaration(ctx.methodDeclaration()));
    return replacements;
  }

  @Override
  public List<Replacement> visitMethodDeclaration(MethodDeclarationContext ctx) {
    List<Replacement> replacements = new ArrayList<>();
    if (ctx.typeTypeOrVoid() != null) {
      replacements.addAll(visitTypeTypeOrVoid(ctx.typeTypeOrVoid()));
    }
    replacements.addAll(processIdentifier(ctx.IDENTIFIER(), NONE));
    replacements.addAll(visitFormalParameters(ctx.formalParameters()));
    if (ctx.qualifiedNameList() != null) {
      replacements.addAll(visitQualifiedNameList(ctx.qualifiedNameList()));
    }

    JavaParser.MethodBodyContext methodBody = ctx.methodBody();
    if (methodBody != null) {
      replacements.addAll(visitMethodBody(methodBody));
    }
    return replacements;
  }

  @Override
  public List<Replacement> visitIdentifierTypeOrDiamondPair(
      JavaParser.IdentifierTypeOrDiamondPairContext ctx) {
    if (ctx.typeArgumentsOrDiamond() == null) {
      return processIdentifier(ctx.IDENTIFIER(), NONE);
    } else {
      List<Replacement> replacements = new ArrayList<>();
      String identifier = ctx.IDENTIFIER().getText();
      if (ctx.typeArgumentsOrDiamond().getText().equals("<>")) {
        if (identifier.contains("KType")
            && templateOptions.isKTypePrimitive()
            && (!identifier.contains("VType") || templateOptions.isVTypePrimitive())) {
          replacements.add(new Replacement(ctx.typeArgumentsOrDiamond(), ""));
        }
        return processIdentifier(ctx.IDENTIFIER(), replacements);
      } else {
        List<TypeBound> typeBounds = new ArrayList<>();
        TypeArgumentsContext typeArguments = ctx.typeArgumentsOrDiamond().typeArguments();
        Deque<Type> wildcards = getWildcards();
        for (TypeArgumentContext c : typeArguments.typeArgument()) {
          typeBounds.add(typeBoundOf(c, wildcards));
        }
        replacements.add(new Replacement(typeArguments, toString(typeBounds)));

        int typeBoundIndex = 0;
        if (identifier.contains("KType")) {
          TypeBound bb = typeBounds.get(typeBoundIndex++);
          if (bb.isTemplateType()) {
            identifier = identifier.replace("KType", bb.getBoxedType());
          } else {
            identifier = identifier.replace("KType", "Object");
          }
        }
        if (identifier.contains("VType")) {
          TypeBound bb = typeBounds.get(typeBoundIndex++);
          if (bb.isTemplateType()) {
            identifier = identifier.replace("VType", bb.getBoxedType());
          } else {
            identifier = identifier.replace("VType", "Object");
          }
        }
        replacements.add(new Replacement(ctx.IDENTIFIER(), identifier));
      }

      return replacements;
    }
  }

  @Override
  public List<Replacement> visitCreatedName(CreatedNameContext ctx) {
    return super.visitCreatedName(ctx);
  }

  @Override
  public List<Replacement> visitIdentifierTypePair(JavaParser.IdentifierTypePairContext ctx) {
    String identifier = ctx.IDENTIFIER().getText();
    if (isTemplateIdentifier(identifier)) {
      if (ctx.typeArguments() != null) {
        List<Replacement> replacements = new ArrayList<>();
        List<TypeBound> typeBounds = new ArrayList<>();
        Deque<Type> wildcards = getWildcards();
        for (TypeArgumentContext c : ctx.typeArguments().typeArgument()) {
          typeBounds.add(typeBoundOf(c, wildcards));
        }
        replacements.add(new Replacement(ctx.typeArguments(), toString(typeBounds)));

        int typeBoundIndex = 0;
        if (identifier.contains("KType")) {
          TypeBound bb = typeBounds.get(typeBoundIndex++);
          if (bb.isTemplateType()) {
            identifier = identifier.replace("KType", bb.getBoxedType());
          } else {
            identifier = identifier.replace("KType", "Object");
          }
        }
        if (identifier.contains("VType")) {
          TypeBound bb = typeBounds.get(typeBoundIndex++);
          if (bb.isTemplateType()) {
            identifier = identifier.replace("VType", bb.getBoxedType());
          } else {
            identifier = identifier.replace("VType", "Object");
          }
        }
        replacements.add(new Replacement(ctx.IDENTIFIER(), identifier));
        return replacements;
      } else {
        return processIdentifier(ctx.IDENTIFIER(), NONE);
      }
    }
    return super.visitIdentifierTypePair(ctx);
  }

  @Override
  public List<Replacement> visitQualifiedName(QualifiedNameContext ctx) {
    List<Replacement> replacements = NONE;
    for (TerminalNode identifier : ctx.IDENTIFIER()) {
      String symbol = identifier.getText();
      if (isTemplateIdentifier(symbol)) {
        if (symbol.contains("KType")) {
          symbol = symbol.replace("KType", templateOptions.getKType().getBoxedType());
        }
        if (symbol.contains("VType")) {
          symbol = symbol.replace("VType", templateOptions.getVType().getBoxedType());
        }

        if (replacements == NONE) {
          replacements = new ArrayList<>();
        }
        replacements.add(new Replacement(identifier, symbol));
      }
    }

    return replacements;
  }

  @Override
  protected List<Replacement> defaultResult() {
    return NONE;
  }

  @Override
  protected List<Replacement> aggregateResult(List<Replacement> first, List<Replacement> second) {
    if (second.size() == 0) return first;
    if (first.size() == 0) return second;

    // Treat partial results as immutable.
    List<Replacement> result = new ArrayList<Replacement>();
    result.addAll(first);
    result.addAll(second);
    return result;
  }

  private ArrayDeque<Type> getWildcards() {
    ArrayDeque<Type> deque = new ArrayDeque<>();
    if (templateOptions.hasKType()) {
      deque.addLast(templateOptions.getKType());
    }
    if (templateOptions.hasVType()) {
      deque.addLast(templateOptions.getVType());
    }
    return deque;
  }

  private List<Replacement> processIdentifier(TerminalNode ctx, List<Replacement> replacements) {
    String identifier = ctx.getText();
    if (isTemplateIdentifier(identifier)) {
      replacements = new ArrayList<>(replacements);
      switch (identifier) {
        case "KType":
          identifier =
              templateOptions.isKTypePrimitive() ? templateOptions.getKType().getType() : "KType";
          break;
        case "VType":
          identifier =
              templateOptions.isVTypePrimitive() ? templateOptions.getVType().getType() : "VType";
          break;
        default:
          if (identifier.contains("KType")) {
            identifier = identifier.replace("KType", templateOptions.getKType().getBoxedType());
          }
          if (identifier.contains("VType")) {
            identifier = identifier.replace("VType", templateOptions.getVType().getBoxedType());
          }
          break;
      }
      replacements.add(new Replacement(ctx, identifier));
    }
    return replacements;
  }

  private String toString(List<TypeBound> typeBounds) {
    List<String> parts = new ArrayList<>();
    for (TypeBound tb : typeBounds) {
      if (tb.isTemplateType()) {
        if (!tb.templateBound().isGeneric()) {
          continue;
        }
      }
      parts.add(tb.originalBound());
    }
    return parts.isEmpty() ? "" : "<" + join(", ", parts) + ">";
  }

  private String join(String on, Iterable<String> parts) {
    StringBuilder out = new StringBuilder();
    boolean prependOn = false;
    for (String part : parts) {
      if (prependOn) {
        out.append(on);
      } else {
        prependOn = true;
      }
      out.append(part);
    }
    return out.toString();
  }

  private boolean isTemplateIdentifier(String symbol) {
    return symbol.contains("KType") || symbol.contains("VType");
  }

  static void checkArgument(boolean condition, String msg) {
    if (!condition) {
      throw new RuntimeException(msg);
    }
  }
}
