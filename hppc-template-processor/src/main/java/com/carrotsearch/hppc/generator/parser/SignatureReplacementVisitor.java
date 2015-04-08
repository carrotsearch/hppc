package com.carrotsearch.hppc.generator.parser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;
import com.carrotsearch.hppc.generator.parser.Java7Parser.ClassDeclarationContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.ClassOrInterfaceTypeContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.ConstructorDeclarationContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.CreatedNameContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.GenericMethodDeclarationContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.IdentifierTypeOrDiamondPairContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.IdentifierTypePairContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.InterfaceDeclarationContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.MethodDeclarationContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.PrimaryContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.QualifiedNameContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.TypeArgumentContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.TypeArgumentsContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.TypeBoundContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.TypeContext;
import com.carrotsearch.hppc.generator.parser.Java7Parser.TypeParameterContext;

class SignatureReplacementVisitor extends Java7ParserBaseVisitor<List<Replacement>> {
  private final static List<Replacement> NONE = Collections.emptyList();
  private final TemplateOptions templateOptions;
  private final SignatureProcessor processor;

  public SignatureReplacementVisitor(TemplateOptions templateOptions, SignatureProcessor processor) {
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
      checkNotNull(targetType, "Target not a template bound: " + originalBound);
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
  }

  private TypeBound typeBoundOf(TypeParameterContext c) {
    String symbol = c.Identifier().toString();
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

    TypeBound t = typeBoundOf(c.type());
    if (t.isTemplateType()) {
      return new TypeBound(t.templateBound(), getSourceText(c));
    } else {
      return new TypeBound(null, getSourceText(c));
    }
  }

  private String getSourceText(ParserRuleContext c) {
    return this.processor.tokenStream.getText(c.getSourceInterval());
  }

  private TypeBound typeBoundOf(TypeContext c) {
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
    checkArgument(c.identifierTypePair().size() == 1, "Unexpected typeBoundOf context: " + c.getText());
    
    for (IdentifierTypePairContext p : c.identifierTypePair()) {
      switch (p.Identifier().getText()) {
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

    String className = ctx.Identifier().getText();
    if (isTemplateIdentifier(className) || true) {
      List<TypeBound> typeBounds = new ArrayList<>();
      if (ctx.typeParameters() != null) {
        for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
          typeBounds.add(typeBoundOf(c));
        }
        result.add(new Replacement(ctx.typeParameters(), toString(typeBounds)));
      }

      int typeBoundIndex = 0;
      if (className.contains("KType")) {
        className = className.replace("KType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
      }
      if (className.contains("VType")) {
        className = className.replace("VType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
      }
      result.add(new Replacement(ctx.Identifier(), className));
    }

    return result;
  }

  @Override
  public List<Replacement> visitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
    List<Replacement> result = super.visitInterfaceDeclaration(ctx);

    String className = ctx.Identifier().getText();
    if (isTemplateIdentifier(className)) {
      List<TypeBound> typeBounds = new ArrayList<>();
      for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
        typeBounds.add(typeBoundOf(c));
      }
      Replacement replaceGenericTypes = new Replacement(ctx.typeParameters(), toString(typeBounds));

      int typeBoundIndex = 0;
      if (className.contains("KType")) {
        className = className.replace("KType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
      }
      if (className.contains("VType")) {
        className = className.replace("VType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
      }
      Replacement replaceIdentifier = new Replacement(ctx.Identifier(), className);

      result = new ArrayList<>(result);
      result.addAll(Arrays.asList(
          replaceIdentifier,
          replaceGenericTypes));
    }

    return result;
  }

  @Override
  public List<Replacement> visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
    return processIdentifier(ctx.Identifier(), super.visitConstructorDeclaration(ctx));
  }

  @Override
  public List<Replacement> visitPrimary(PrimaryContext ctx) {
    TerminalNode identifier = ctx.Identifier();
    if (identifier != null && 
        isTemplateIdentifier(identifier.getText())) {
      return processIdentifier(identifier, NONE);
    } else {
      return super.visitPrimary(ctx);
    }
  }

  @Override
  public List<Replacement> visitGenericMethodDeclaration(GenericMethodDeclarationContext ctx) {
    ArrayList<String> bounds = new ArrayList<>();
    for (TypeParameterContext c : ctx.typeParameters().typeParameter()) {
      switch (c.Identifier().getText()) {
        case "KType":
          checkArgument(c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
          if (templateOptions.isKTypeGeneric()) {
            bounds.add(c.getText());
          }
          break;
          
        case "VType":
          checkArgument(c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
          if (templateOptions.isVTypeGeneric()) {
            bounds.add(c.getText());
          }
          break;

        default:
          TypeBoundContext tbc = c.typeBound(); 
          if (tbc != null) {
            checkArgument(tbc.type().size() == 1, "Expected exactly one type bound: " + c.getText());
            TypeContext tctx = tbc.type().get(0);
            Interval sourceInterval = tbc.getSourceInterval();
            try {
              StringWriter sw = processor.reconstruct(
                  new StringWriter(), 
                  processor.tokenStream, 
                  sourceInterval.a, 
                  sourceInterval.b, 
                  visitType(tctx),
                  templateOptions);
              bounds.add(c.Identifier() + " extends " + sw.toString());
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
    if (ctx.type() != null) {
      replacements.addAll(visitType(ctx.type()));
    }
    replacements.addAll(processIdentifier(ctx.Identifier(), NONE));
    replacements.addAll(visitFormalParameters(ctx.formalParameters()));
    if (ctx.qualifiedNameList() != null) {
      replacements.addAll(visitQualifiedNameList(ctx.qualifiedNameList()));
    }
    replacements.addAll(visitMethodBody(ctx.methodBody()));
    return replacements;
  }

  @Override
  public List<Replacement> visitIdentifierTypeOrDiamondPair(IdentifierTypeOrDiamondPairContext ctx) {
    if (ctx.typeArgumentsOrDiamond() == null) {
      return processIdentifier(ctx.Identifier(), NONE);
    } else {
      List<Replacement> replacements = new ArrayList<>();
      String identifier = ctx.Identifier().getText();
      if (ctx.typeArgumentsOrDiamond().getText().equals("<>")) {
        if (identifier.contains("KType")
            && templateOptions.isKTypePrimitive() 
            && (!identifier.contains("VType") || templateOptions.isVTypePrimitive())) {
          replacements.add(new Replacement(ctx.typeArgumentsOrDiamond(), ""));
        }
        return processIdentifier(ctx.Identifier(), replacements);
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
        replacements.add(new Replacement(ctx.Identifier(), identifier));
      }

      return replacements;
    }
  }

  @Override
  public List<Replacement> visitCreatedName(CreatedNameContext ctx) {
    return super.visitCreatedName(ctx);
  }
  
  @Override
  public List<Replacement> visitIdentifierTypePair(IdentifierTypePairContext ctx) {
    String identifier = ctx.Identifier().getText();
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
        replacements.add(new Replacement(ctx.Identifier(), identifier));
        return replacements;
      } else {
        return processIdentifier(ctx.Identifier(), NONE);
      }
    }
    return super.visitIdentifierTypePair(ctx);
  }

  @Override
  public List<Replacement> visitQualifiedName(QualifiedNameContext ctx) {
    List<Replacement> replacements = NONE;
    for (TerminalNode identifier : ctx.Identifier()) {
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
    if (first.size()  == 0) return second;
  
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
          identifier = templateOptions.isKTypePrimitive()
                       ? templateOptions.getKType().getType()
                       : "KType";
          break;
        case "VType":
          identifier = templateOptions.isVTypePrimitive()
                       ? templateOptions.getVType().getType()
                       : "VType";
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
    List <String> parts = new ArrayList<>();
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
    return symbol.contains("KType") ||
           symbol.contains("VType");
  }
}
