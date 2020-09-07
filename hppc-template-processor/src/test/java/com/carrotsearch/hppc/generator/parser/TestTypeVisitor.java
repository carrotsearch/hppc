/*
 * HPPC
 *
 * Copyright (C) 2010-2020 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.generator.parser;

import com.carrotsearch.hppc.generator.Type;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.ObjectStreamField;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RunWith(RandomizedRunner.class)
public class TestTypeVisitor {
  static class TypeRef {}

  static class ClassRef {
    final ParserRuleContext parseTree;
    final String className;
    final List<ComplexTypeRef> typeBounds;

    ClassRef(ParserRuleContext parseTree, String className, List<ComplexTypeRef> typeBounds) {
      this.parseTree = parseTree;
      this.className = Objects.requireNonNull(className);
      this.typeBounds = Objects.requireNonNull(typeBounds);
    }

    @Override
    public String toString() {
      return String.format(
          Locale.ROOT,
          "%s%s",
          className,
          typeBounds.isEmpty()
              ? ""
              : "<"
                  + typeBounds.stream().map(Object::toString).collect(Collectors.joining(", "))
                  + ">");
    }

    public TypeBinding getType(TypeContext context) {
      return context.types.get(className);
    }

    public boolean hasTypeBound() {
      return !this.typeBounds.isEmpty();
    }

    public TypeBinding getTemplateType(TypeContext typeContext) {
      return typeContext.types.get(className);
    }

    public String process(TypeContext context) {
      return context.sourceOf(parseTree.getSourceInterval());
    }
  }

  static class PrimitiveTypeRef extends TypeRef {
    final String primitiveType;

    PrimitiveTypeRef(String type) {
      this.primitiveType = type;
    }

    @Override
    public String toString() {
      return "[" + primitiveType + "]";
    }
  }

  static class ComplexTypeRef extends TypeRef {
    final List<ClassRef> classRefs;

    ComplexTypeRef(ClassRef classRef) {
      this(Collections.singletonList(classRef));
    }

    ComplexTypeRef(List<ClassRef> classRefs) {
      this.classRefs = Objects.requireNonNull(classRefs);
      if (classRefs.isEmpty()) {
        throw new IllegalArgumentException();
      }
    }

    public boolean hasBinding(TypeContext typeContext) {
      return getBinding(typeContext) != null;
    }

    public TypeBinding getBinding(TypeContext typeContext) {
      for (ClassRef classRef : classRefs) {
        TypeBinding type = classRef.getType(typeContext);
        if (type != null) {
          return type;
        }
      }
      return null;
    }

    public String process(TypeContext context) {
      return classRefs.stream()
          .map(classRef -> classRef.process(context))
          .collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
      return classRefs.stream().map(Object::toString).collect(Collectors.joining("."));
    }

    public boolean isErased(TypeContext typeContext) {
      if (classRef().hasTypeBound()) {
        return false;
      }

      TypeBinding templateType = classRef().getTemplateType(typeContext);
      return templateType != null && templateType.replacementType.isPrimitive();
    }

    private ClassRef classRef() {
      return classRefs.get(0);
    }

    public String rewrite(TypeContext typeContext) {
      if (classRef().hasTypeBound()) {
        // Determine type context recursively?
        return "-?-";
      } else {
        return classRef().className;
      }
    }
  }

  static class TypeParametersVisitor extends JavaParserBaseVisitor<List<ComplexTypeRef>> {
    public List<ComplexTypeRef> types = new ArrayList<>();

    @Override
    public List<ComplexTypeRef> visitTypeParameters(JavaParser.TypeParametersContext ctx) {
      super.visitTypeParameters(ctx);
      return types;
    }

    @Override
    public List<ComplexTypeRef> visitTypeParameter(JavaParser.TypeParameterContext ctx) {
      // TODO: cater for complex type bound (X & Y) somehow? Seems exotic.
      List<ComplexTypeRef> bounds = new ArrayList<>();
      if (ctx.typeBound() != null) {
        bounds.add((ComplexTypeRef) ctx.typeBound().accept(new TypeTypeVisitor()));
      }

      types.add(new ComplexTypeRef(new ClassRef(ctx, ctx.IDENTIFIER().getText(), bounds)));
      return types;
    }
  }

  static class TypeArgumentsVisitor extends JavaParserBaseVisitor<List<ComplexTypeRef>> {
    public List<ComplexTypeRef> types = new ArrayList<>();

    @Override
    public List<ComplexTypeRef> visitTypeArguments(JavaParser.TypeArgumentsContext ctx) {
      super.visitTypeArguments(ctx);
      return types;
    }

    @Override
    public List<ComplexTypeRef> visitTypeArgument(JavaParser.TypeArgumentContext ctx) {
      TypeRef type = ctx.accept(new TypeTypeVisitor());
      if (type instanceof PrimitiveTypeRef) {
        throw new IllegalArgumentException(
            "Primitive type reference is syntactically invalid here.");
      }
      types.add((ComplexTypeRef) type);
      return types;
    }
  }

  static class TypeTypeVisitor extends JavaParserBaseVisitor<TypeRef> {
    @Override
    public TypeRef visitPrimitiveType(JavaParser.PrimitiveTypeContext ctx) {
      return new PrimitiveTypeRef(ctx.getText());
    }

    @Override
    public TypeRef visitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
      List<TypeRef> typeRefChain =
          ctx.identifierTypePair().stream().map(v -> v.accept(this)).collect(Collectors.toList());

      if (typeRefChain.size() == 1) {
        return typeRefChain.iterator().next();
      } else {
        List<ClassRef> classRefs =
            typeRefChain.stream()
                .flatMap(v -> ((ComplexTypeRef) v).classRefs.stream())
                .collect(Collectors.toList());
        return new ComplexTypeRef(classRefs);
      }
    }

    @Override
    public TypeRef visitIdentifierTypePair(JavaParser.IdentifierTypePairContext ctx) {
      String className = ctx.IDENTIFIER().getText();

      final List<ComplexTypeRef> typeChain;
      if (ctx.typeArguments() != null) {
        typeChain = ctx.typeArguments().accept(new TypeArgumentsVisitor());
      } else {
        typeChain = Collections.emptyList();
      }

      return new ComplexTypeRef(new ClassRef(ctx, className, typeChain));
    }
  }

  static class ParserContext {
    final JavaParser parser;
    final String input;
    final CommonTokenStream tokens;

    ParserContext(String input, CommonTokenStream tokens, JavaParser parser) {
      this.input = input;
      this.tokens = tokens;
      this.parser = parser;
    }

    public static ParserContext parse(String in) {
      Lexer lexer = new JavaLexer(CharStreams.fromString(in));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      JavaParser parser = new JavaParser(tokens);
      parser.setTrace(false);
      parser.setErrorHandler(new BailErrorStrategy());

      return new ParserContext(in, tokens, parser);
    }
  }

  @Test
  public void testTypeTypeVisitor() {
    for (String[] pairs :
        new String[][] {
          {"int", "[int]"},
          {"Foo", "Foo"},
          {"Foo<>", "Foo"},
          {"Foo<Bar>", "Foo<Bar>"},
          {"Foo<Bar, Baz>", "Foo<Bar, Baz>"},
          {"Foo<? extends Baz>", "Foo<Baz>"},
          {"Foo<? super Baz>", "Foo<Baz>"},
          {"Foo<Baz<? super Bar>>", "Foo<Baz<Bar>>"},
          {"Bar.Foo<Baz.Baz<? super Foo.Bar>>", "Bar.Foo<Baz.Baz<Foo.Bar>>"},
        }) {
      String inputType = pairs[0];
      String parsedType = pairs[1];
      ParserContext context = ParserContext.parse(inputType);
      TypeRef typeRef = context.parser.typeType().accept(new TypeTypeVisitor());
      Assertions.assertThat(typeRef.toString())
          .as("Type: '" + inputType + "' should parse to: '" + parsedType + "'")
          .isEqualTo(parsedType);
    }
  }

  @Test
  public void testTypeParametersVisitor() {
    for (String[] pairs :
        new String[][] {
          {"<Foo>", "Foo"},
          {"<Foo, Bar>", "Foo, Bar"},
          {"<Foo extends Bar>", "Foo<Bar>"},
        }) {
      String inputType = pairs[0];
      String parsedType = pairs[1];
      ParserContext context = ParserContext.parse(inputType);
      String typesRef =
          context.parser.typeParameters().accept(new TypeParametersVisitor()).stream()
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      Assertions.assertThat(typesRef)
          .as("Type refs: '" + inputType + "' should parse to: '" + parsedType + "'")
          .isEqualTo(parsedType);
    }
  }

  static class TypeBinding {
    private final String templateName;
    private final Type replacementType;

    public TypeBinding(String templateName, Type replacementType) {
      this.templateName = templateName;
      this.replacementType = replacementType;
    }
  }

  static class TypeContext {
    final Map<String, TypeBinding> types = new LinkedHashMap<>();
    final CommonTokenStream tokens;

    TypeContext(CommonTokenStream tokens) {
      this.tokens = tokens;
    }

    public void bind(TypeBinding... types) {
      for (TypeBinding type : types) {
        this.types.put(type.templateName, type);
      }
    }

    public String sourceOf(Interval interval) {
      return tokens.getText(interval);
    }
  }

  static String processIdentifier(String name, TypeContext context) {
    for (TypeBinding binding : context.types.values()) {
      if (name.contains(binding.templateName)) {
        name = name.replace(binding.templateName, binding.replacementType.getBoxedType());
      }
    }
    return name;
  }

  static class TypeReplacer extends JavaParserBaseVisitor<List<Replacement>> {
    private final List<Replacement> replacements = new ArrayList<>();
    private final ArrayDeque<TypeContext> typeContexts = new ArrayDeque<>();

    TypeReplacer(TypeContext initial) {
      this.typeContexts.push(initial);
    }

    @Override
    public List<Replacement> visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
      JavaParser.TypeParametersContext typeParameters = ctx.typeParameters();
      if (typeParameters != null) {
        TypeContext typeContext = typeContexts.peek();
        List<ComplexTypeRef> typeRefs = typeParameters.accept(new TypeParametersVisitor());
        List<String> resolved = new ArrayList<>();
        for (ComplexTypeRef p : typeRefs) {
          if (p.isErased(typeContext)) {
            // do nothing.
          } else {
            resolved.add(p.rewrite(typeContext));
          }
        }

        String rewritten =
            resolved.isEmpty()
                ? ""
                : "<" + resolved.stream().collect(Collectors.joining(", ")) + ">";
        replacements.add(new Replacement(typeParameters, rewritten));
      }

      TerminalNode identifier = ctx.IDENTIFIER();
      replacements.add(
          new Replacement(
              identifier, processIdentifier(identifier.getText(), typeContexts.peek())));
      return super.visitClassDeclaration(ctx);
    }

    @Override
    public List<Replacement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
      super.visitCompilationUnit(ctx);
      return replacements;
    }
  }

  @Test
  public void testClassName() throws IOException {
    String input = "class KTypeClass {}";
    check(input, "class ObjectClass {}", new TypeBinding("KType", Type.GENERIC));
    check(input, "class IntClass {}", new TypeBinding("KType", Type.INT));
  }

  @Test
  public void testClassNameWithSimpleTypes() throws IOException {
    String input = "class KTypeClass<KType> {}";
    check(input, "class ObjectClass<KType> {}", new TypeBinding("KType", Type.GENERIC));
    check(input, "class IntClass {}", new TypeBinding("KType", Type.INT));
  }

  @Test
  public void testClassNameWithOtherTypes() throws IOException {
    String input = "class KTypeClass<KType, Float> {}";
    check(input, "class ObjectClass<KType, Float> {}", new TypeBinding("KType", Type.GENERIC));
    check(input, "class IntClass<Float> {}", new TypeBinding("KType", Type.INT));

    input = "class KTypeClass<KType, E, T extends E> {}";
    check(
        input,
        "class ObjectClass<KType, E, T extends E> {}",
        new TypeBinding("KType", Type.GENERIC));
    check(input, "class IntClass<E, T extends E> {}", new TypeBinding("KType", Type.INT));

    input = "class KTypeFoo<KType, T extends KTypeSupplier<KType>> {}";
    check(
        input,
        "class KTypeFoo<KType, T extends KTypeSupplier<KType>> {}",
        new TypeBinding("KType", Type.GENERIC));
  }

  private void check(String input, String expected, TypeBinding... bindings) throws IOException {
    ParserContext context = ParserContext.parse(input);

    TypeContext typeContext = new TypeContext(context.tokens);
    typeContext.bind(bindings);

    List<Replacement> replacements =
        context.parser.compilationUnit().accept(new TypeReplacer(typeContext));

    String output = SignatureProcessor.applyReplacements(context.tokens, replacements, null);
    Assertions.assertThat(output).isEqualTo(expected);
  }
}
