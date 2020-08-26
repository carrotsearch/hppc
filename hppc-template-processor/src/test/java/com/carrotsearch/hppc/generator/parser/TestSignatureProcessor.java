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

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.*;
import org.apache.velocity.Template;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RandomizedRunner.class)
public class TestSignatureProcessor {
  @Test
  public void testComplexClassInterfaceDeclaration() throws IOException {
    checkResource(
        new TemplateOptions(Type.GENERIC, Type.GENERIC),
        "testComplexClassInterfaceDeclaration.java",
        "testComplexClassInterfaceDeclaration.java");
  }

  /*

    KTypeFoo<KType>
      { KType=int }    IntFoo
      { KType=Object } ObjectFoo<KType>

    KTypeFoo<KType, T>
    KTypeFoo<KTypeBar>
    KTypeFoo<KTypeBar<KType>>
      { KType=int } IntFoo<IntBar>
      replacetypes with recursive call.
                                                             // bind(KType = X, VType = Y)
    public class KTypeVTypeClass<KType, VType> {             // visit bounds => [ KType=X, VType=Y ], visit name, bind(KType=X, VType=Y)
      public Iterator<KTypeCursor<KType>> iterator() {       // visit bounds => [ KType=X ], visit name
        return new KTypeFoo<KType, KTypeCursor<KType>>();    //
      }
    }

  enter   expression, LT(1)=new
  consume [@0,0:2='new',<31>,1:0] rule expression
  enter   creator, LT(1)=KTypeFoo
  enter   createdName, LT(1)=KTypeFoo
  enter   identifierTypeOrDiamondPair, LT(1)=KTypeFoo
  consume [@2,4:11='KTypeFoo',<111>,1:4] rule identifierTypeOrDiamondPair
  enter   typeArgumentsOrDiamond, LT(1)=<

  enter   typeArguments, LT(1)=<
  consume [@3,12:12='<',<72>,1:12] rule typeArguments
  enter     typeArgument, LT(1)=KType
  enter       typeType, LT(1)=KType
  enter         classOrInterfaceType, LT(1)=KType
  enter           identifierTypePair, LT(1)=KType
  consume         [@4,13:17='KType',<111>,1:13] rule identifierTypePair
  exit            identifierTypePair, LT(1)=,
  exit          classOrInterfaceType, LT(1)=,
  exit        typeType, LT(1)=,
  exit      typeArgument, LT(1)=,
  consume [@5,18:18=',',<68>,1:18] rule typeArguments

  enter     typeArgument, LT(1)=KTypeCursor
  enter       typeType, LT(1)=KTypeCursor
  enter         classOrInterfaceType, LT(1)=KTypeCursor
  enter           identifierTypePair, LT(1)=KTypeCursor
  consume         [@7,20:30='KTypeCursor',<111>,1:20] rule identifierTypePair
  enter             typeArguments, LT(1)=<
  consume           [@8,31:31='<',<72>,1:31] rule typeArguments

  enter               typeArgument, LT(1)=KType
  enter                 typeType, LT(1)=KType
  enter                   classOrInterfaceType, LT(1)=KType
  enter                     identifierTypePair, LT(1)=KType
  consume                   [@9,32:36='KType',<111>,1:32] rule identifierTypePair
  exit                      identifierTypePair, LT(1)=>
  exit                    classOrInterfaceType, LT(1)=>
  exit                  typeType, LT(1)=>
  exit                typeArgument, LT(1)=>
  consume           [@10,37:37='>',<71>,1:37] rule typeArguments
  exit    typeArguments, LT(1)=>
  exit    identifierTypePair, LT(1)=>
  exit    classOrInterfaceType, LT(1)=>
  exit    typeType, LT(1)=>
  exit    typeArgument, LT(1)=>
  consume [@11,38:38='>',<71>,1:38] rule typeArguments
  exit    typeArguments, LT(1)=(
  exit    typeArgumentsOrDiamond, LT(1)=(
  exit    identifierTypeOrDiamondPair, LT(1)=(
  exit    createdName, LT(1)=(

  enter   classCreatorRest, LT(1)=(
  enter   arguments, LT(1)=(
  consume [@12,39:39='(',<61>,1:39] rule arguments
  consume [@13,40:40=')',<62>,1:40] rule arguments
  exit    arguments, LT(1)=<EOF>
  exit    classCreatorRest, LT(1)=<EOF>
  exit    creator, LT(1)=<EOF>
  exit    expression, LT(1)=<EOF>
     */

  @Test
  public void testCreator() throws Exception {
    String in = "new KTypeFoo<KType, KTypeCursor<KType>>()";

    abstract class RuleContext {
      protected final ParserRuleContext ctx;

      protected RuleContext(ParserRuleContext ctx) {
        this.ctx = ctx;
      }
    }

    class ArgType extends RuleContext {
      protected ArgType(ParserRuleContext ctx) {
        super(ctx);
      }

      @Override
      public String toString() {
        return ctx.getText();
      }
    }

    class TypeArguments extends RuleContext {
      private final Deque<ArgType> args = new ArrayDeque<>();

      public TypeArguments(ParserRuleContext ctx) {
        super(ctx);
      }

      @Override
      public String toString() {
        return "<" + args.stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
      }
    }

    class ArgTypeVisitor extends JavaParserBaseVisitor<ArgType> {
      @Override
      public ArgType visitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
        super.visitClassOrInterfaceType(ctx);
        return new ArgType(ctx);
      }

      @Override
      public ArgType visitIdentifierTypePair(JavaParser.IdentifierTypePairContext ctx) {
        System.out.println("# > " + ctx.IDENTIFIER().getText());
        return super.visitIdentifierTypePair(ctx);
      }
    }

    class TypeArgsVisitor extends JavaParserBaseVisitor<TypeArguments> {
      Deque<TypeArguments> typeArguments = new ArrayDeque<>();

      @Override
      public TypeArguments visitTypeArguments(JavaParser.TypeArgumentsContext ctx) {
        typeArguments.addLast(new TypeArguments(ctx));
        super.visitTypeArguments(ctx);
        return typeArguments.removeLast();
      }

      @Override
      public TypeArguments visitTypeType(JavaParser.TypeTypeContext ctx) {
        typeArguments.peekLast().args.addLast(ctx.accept(new ArgTypeVisitor()));
        return null;
      }
    }

    Lexer lexer = new JavaLexer(CharStreams.fromString(in));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JavaParser parser = new JavaParser(tokens);
    parser.setTrace(false);
    parser.setErrorHandler(new BailErrorStrategy());
    JavaParser.ExpressionContext expression = parser.expression();

    // KTypeVTypeFoo<KType, VType>
    // KTypeVTypeFoo<VType, KType>
    // KTypeFoo<KType, KTypeBar<KType>>

    List<Replacement> replacements = new ArrayList<>();
    class ExprVisitor extends JavaParserBaseVisitor<Void> {
      @Override
      public Void visitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
        return super.visitClassOrInterfaceType(ctx);
      }

      @Override
      public Void visitIdentifierTypePair(JavaParser.IdentifierTypePairContext ctx) {
        try {
          if (ctx.typeArguments() != null) {
            StringWriter sw =
                SignatureProcessor.reconstruct(
                    new StringWriter(),
                    tokens,
                    ctx.start.getTokenIndex(),
                    ctx.stop.getTokenIndex() + 1,
                    Arrays.asList(new Replacement(ctx.typeArguments(), "??")),
                    new TemplateOptions(Type.GENERIC));
            System.out.println("## " + sw);
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }

        replacements.add(new Replacement(ctx.IDENTIFIER(), "--"));
        return super.visitIdentifierTypePair(ctx);
      }

      @Override
      public Void visitTypeArguments(JavaParser.TypeArgumentsContext ctx) {
        super.visitTypeArguments(ctx);
        return null;
      }
    }
    expression.accept(new ExprVisitor());

    TemplateOptions options = new TemplateOptions(Type.GENERIC, Type.LONG);
    String out = SignatureProcessor.applyReplacements(tokens, replacements, options);
    System.out.println(out);
  }

  @Test
  public void testComplexMixedSignature() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "public class KTypeVTypeClass<KType, VType> {\n"
                + "  public Iterator<KTypeCursor<KType>> iterator() {\n"
                + "    return new KTypeFoo<KType, KTypeCursor<KType>>();\n"
                + "  }\n"
                + "}");
    check(Type.INT, Type.LONG, sp, "");
    check(Type.GENERIC, Type.LONG, sp, "");
  }

  @Test
  public void testClassMultipleMixedBound() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeFoo<T, KType, F> {}");
    check(Type.INT, sp, "public class IntFoo<T, F> {}");
    check(Type.GENERIC, sp, "public class ObjectFoo<T, KType, F> {}");
  }

  @Test
  public void testClassKV() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeVTypeClass<KType, VType> {}");
    check(Type.INT, Type.LONG, sp, "public class IntLongClass {}");
    check(Type.INT, Type.GENERIC, sp, "public class IntObjectClass<VType> {}");
    check(Type.GENERIC, Type.LONG, sp, "public class ObjectLongClass<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<KType, VType> {}");
  }

  @Test
  public void testClassVK_SignatureReversed() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeVTypeClass<VType, KType> {}");
    check(Type.INT, Type.LONG, sp, "public class LongIntClass {}");
    check(Type.INT, Type.GENERIC, sp, "public class ObjectIntClass<VType> {}");
    check(Type.GENERIC, Type.LONG, sp, "public class LongObjectClass<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<VType, KType> {}");
  }

  @Test
  public void testClassK() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeClass<KType> {}");
    check(Type.INT, sp, "public class IntClass {}");
    check(Type.GENERIC, sp, "public class ObjectClass<KType> {}");
  }

  @Test
  public void testClassExtendsNonTemplate() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor("public class KTypeVTypeClass<KType, VType> extends SuperClass {}");
    check(Type.INT, Type.LONG, sp, "public class IntLongClass extends SuperClass {}");
    check(Type.INT, Type.GENERIC, sp, "public class IntObjectClass<VType> extends SuperClass {}");
    check(Type.GENERIC, Type.LONG, sp, "public class ObjectLongClass<KType> extends SuperClass {}");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "public class ObjectObjectClass<KType, VType> extends SuperClass {}");
  }

  @Test
  public void testClassExtendsTemplate() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "public class KTypeVTypeClass<KType, VType> extends KTypeVTypeSuperClass<KType, VType> {}");
    check(Type.INT, Type.LONG, sp, "public class IntLongClass extends IntLongSuperClass {}");
    check(
        Type.INT,
        Type.GENERIC,
        sp,
        "public class IntObjectClass<VType> extends IntObjectSuperClass<VType> {}");
    check(
        Type.GENERIC,
        Type.LONG,
        sp,
        "public class ObjectLongClass<KType> extends ObjectLongSuperClass<KType> {}");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "public class ObjectObjectClass<KType, VType> extends ObjectObjectSuperClass<KType, VType> {}");
  }

  @Test
  public void testClassImplementsTemplate() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "public class KTypeVTypeClass<KType, VType> "
                + " extends     KTypeVTypeSuperClass<KType, VType>"
                + " implements  KTypeVTypeInterface<KType, VType> {}");

    check(
        Type.INT,
        Type.LONG,
        sp,
        "public class IntLongClass extends IntLongSuperClass implements IntLongInterface {}");
    check(
        Type.INT,
        Type.GENERIC,
        sp,
        "public class IntObjectClass<VType> extends IntObjectSuperClass<VType> implements IntObjectInterface<VType> {}");
    check(
        Type.GENERIC,
        Type.LONG,
        sp,
        "public class ObjectLongClass<KType> extends ObjectLongSuperClass<KType> implements ObjectLongInterface<KType> {}");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "public class ObjectObjectClass<KType, VType> extends ObjectObjectSuperClass<KType, VType> implements ObjectObjectInterface<KType, VType> {}");
  }

  @Test
  public void testInterfaceKV() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "public interface KTypeVTypeInterface<KType, VType> "
                + "         extends KTypeVTypeSuper<KType, VType> {}");

    check(Type.INT, Type.LONG, sp, "public interface IntLongInterface extends IntLongSuper {}");
    check(
        Type.INT,
        Type.GENERIC,
        sp,
        "public interface IntObjectInterface<VType> extends IntObjectSuper<VType> {}");
    check(
        Type.GENERIC,
        Type.LONG,
        sp,
        "public interface ObjectLongInterface<KType> extends ObjectLongSuper<KType> {}");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "public interface ObjectObjectInterface<KType, VType> extends ObjectObjectSuper<KType, VType> {}");
  }

  @Test
  public void testImportDeclarations() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("import foo.KTypeVTypeClass; class Foo {}");

    check(Type.INT, Type.LONG, sp, "import foo.IntLongClass; class Foo {}");
    check(Type.INT, Type.GENERIC, sp, "import foo.IntObjectClass; class Foo {}");
    check(Type.GENERIC, Type.LONG, sp, "import foo.ObjectLongClass; class Foo {}");
    check(Type.GENERIC, Type.GENERIC, sp, "import foo.ObjectObjectClass; class Foo {}");
  }

  @Test
  public void testFieldDeclaration() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor("class KTypeFoo<KType> { KType foo; KType [] foo2; }");

    check(Type.FLOAT, sp, "class FloatFoo { float foo; float [] foo2; }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { KType foo; KType [] foo2; }");
  }

  @Test
  public void testClassConstructor() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { public KTypeVTypeFoo(KType k, VType v) {} }");

    check(
        Type.FLOAT,
        Type.DOUBLE,
        sp,
        "class FloatDoubleFoo { public FloatDoubleFoo(float k, double v) {} }");
    check(
        Type.FLOAT,
        Type.GENERIC,
        sp,
        "class FloatObjectFoo<VType> { public FloatObjectFoo(float k, VType v) {} }");
    check(
        Type.GENERIC,
        Type.FLOAT,
        sp,
        "class ObjectFloatFoo<KType> { public ObjectFloatFoo(KType k, float v) {} }");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "class ObjectObjectFoo<KType, VType> { public ObjectObjectFoo(KType k, VType v) {} }");
  }

  @Test
  public void testThisReference() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { public void foo() { KTypeVTypeFoo.this.foo(); } }");

    check(
        Type.FLOAT,
        Type.DOUBLE,
        sp,
        "class FloatDoubleFoo { public void foo() { FloatDoubleFoo.this.foo(); } }");
  }

  @Test
  public void testNewClassDiamond() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { public void foo() { new KTypeVTypeFoo<>(); } }");

    check(
        Type.FLOAT,
        Type.DOUBLE,
        sp,
        "class FloatDoubleFoo { public void foo() { new FloatDoubleFoo(); } }");
    check(
        Type.GENERIC,
        Type.DOUBLE,
        sp,
        "class ObjectDoubleFoo<KType> { public void foo() { new ObjectDoubleFoo<>(); } }");
  }

  @Test
  public void testNewClass() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { public void foo() { new KTypeVTypeFoo<KType, VType>(); } }");

    check(
        Type.FLOAT,
        Type.DOUBLE,
        sp,
        "class FloatDoubleFoo { public void foo() { new FloatDoubleFoo(); } }");
    check(
        Type.GENERIC,
        Type.DOUBLE,
        sp,
        "class ObjectDoubleFoo<KType> { public void foo() { new ObjectDoubleFoo<KType>(); } }");
  }

  @Test
  public void testStaticGenericMethod() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { static <KType, VType> KTypeVTypeFoo foo(KType[] k, VType[] v) {} }");

    check(
        Type.FLOAT,
        Type.DOUBLE,
        sp,
        "class FloatDoubleFoo { static FloatDoubleFoo foo(float[] k, double[] v) {} }");
    check(
        Type.GENERIC,
        Type.DOUBLE,
        sp,
        "class ObjectDoubleFoo<KType> { static <KType> ObjectDoubleFoo foo(KType[] k, double[] v) {} }");
  }

  @Test
  public void testWildcardBound() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor("class KTypeFoo<KType> { void bar(KTypeFoo<?> other) {} }");

    check(Type.FLOAT, sp, "class FloatFoo { void bar(FloatFoo other) {} }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { void bar(ObjectFoo<?> other) {} }");
  }

  @Test
  public void testGenericNamedTypeBound() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeFoo<KType> { public <T extends KTypeBar<? super KType>> T forEach(T v) { throw new R(); } }");

    check(
        Type.FLOAT,
        sp,
        "class FloatFoo         { public <T extends FloatBar> T forEach(T v) { throw new R(); } }");
    check(
        Type.GENERIC,
        sp,
        "class ObjectFoo<KType> { public <T extends ObjectBar<? super KType>> T forEach(T v) { throw new R(); } }");
  }

  @Test
  public void testBug_ErasesObjectConstructor() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeVTypeFoo<KType, VType> { static { HashSet<Object> values = new HashSet<Object>(); }}");

    check(
        Type.FLOAT,
        Type.INT,
        sp,
        "class FloatIntFoo { static { HashSet<Object> values = new HashSet<Object>(); }}");
    check(
        Type.GENERIC,
        Type.GENERIC,
        sp,
        "class ObjectObjectFoo<KType, VType> { static { HashSet<Object> values = new HashSet<Object>(); }}");
  }

  @Test
  public void testBug_ErasesUntemplated() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeFoo<KType> { void foo() { KTypeBar<B> x = new KTypeBar<B>(); } }");

    check(Type.FLOAT, sp, "class FloatFoo { void foo() { ObjectBar<B> x = new ObjectBar<B>(); } }");
    check(
        Type.GENERIC,
        sp,
        "class ObjectFoo<KType> { void foo() { ObjectBar<B> x = new ObjectBar<B>(); } }");
  }

  @Test
  public void testBug_EraseNestedPrimitive() throws IOException {
    SignatureProcessor sp =
        new SignatureProcessor(
            "class KTypeFoo<KType> { static class Nested<KType> extends KTypeBar<KType> {} }");

    check(Type.FLOAT, sp, "class FloatFoo { static class Nested extends FloatBar {} }");
    check(
        Type.GENERIC,
        sp,
        "class ObjectFoo<KType> { static class Nested<KType> extends ObjectBar<KType> {} }");
  }

  @Test
  public void testJavaDoc_k() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("/** KTypeFoo KTypes */");

    check(Type.FLOAT, sp, "/** FloatFoo floats */");
    check(Type.GENERIC, sp, "/** ObjectFoo Objects */");
  }

  @Test
  public void testJavaDoc_kv() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("/** KTypeFoo KTypes KTypeVTypeFoo VTypes */");

    check(Type.FLOAT, Type.DOUBLE, sp, "/** FloatFoo floats FloatDoubleFoo doubles */");
    check(Type.GENERIC, Type.GENERIC, sp, "/** ObjectFoo Objects ObjectObjectFoo Objects */");
  }

  @Test
  public void testFullClass() throws IOException {
    checkResource(
        new TemplateOptions(Type.LONG, Type.GENERIC),
        "KTypeVTypeClass.java",
        "LongObjectClass.expected");
    checkResource(
        new TemplateOptions(Type.LONG, Type.INT), "KTypeVTypeClass.java", "LongIntClass.expected");
  }

  private void checkResource(TemplateOptions options, String input, String expected)
      throws IOException {
    SignatureProcessor signatureProcessor = new SignatureProcessor(getResource(input));
    check(options, signatureProcessor, getResource(expected));
  }

  private String getResource(String name) {
    InputStream is = getClass().getResourceAsStream(name);
    if (is == null) {
      throw new AssertionError(
          "Could not find this resource: " + name + " relative to class " + getClass().getName());
    }

    try (InputStream ignored = is) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void check(Type ktype, SignatureProcessor processor, String expected) throws IOException {
    check(new TemplateOptions(ktype), processor, expected);
  }

  private void check(Type ktype, Type vtype, SignatureProcessor processor, String expected)
      throws IOException {
    check(new TemplateOptions(ktype, vtype), processor, expected);
  }

  private void check(TemplateOptions templateOptions, SignatureProcessor processor, String expected)
      throws IOException {
    Assertions.assertThat(processor.process(templateOptions)).isEqualToIgnoringWhitespace(expected);
  }
}
