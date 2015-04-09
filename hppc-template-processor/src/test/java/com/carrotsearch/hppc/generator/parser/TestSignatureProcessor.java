package com.carrotsearch.hppc.generator.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.hppc.generator.TemplateOptions;
import com.carrotsearch.hppc.generator.Type;
import com.carrotsearch.hppc.generator.parser.SignatureProcessor;
import com.carrotsearch.randomizedtesting.RandomizedRunner;

@RunWith(RandomizedRunner.class)
public class TestSignatureProcessor {
  @Test
  public void testClassKV() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeVTypeClass<KType, VType> {}");
    check(Type.INT,     Type.LONG,    sp, "public class IntLongClass {}");
    check(Type.INT,     Type.GENERIC, sp, "public class IntObjectClass<VType> {}");
    check(Type.GENERIC, Type.LONG,    sp, "public class ObjectLongClass<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<KType, VType> {}");
  }

  @Test
  public void testClassVK_SignatureReversed() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeVTypeClass<VType, KType> {}");
    check(Type.INT,     Type.LONG,    sp, "public class LongIntClass {}");
    check(Type.INT,     Type.GENERIC, sp, "public class ObjectIntClass<VType> {}");
    check(Type.GENERIC, Type.LONG,    sp, "public class LongObjectClass<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<VType, KType> {}");
  }

  @Test
  public void testClassK() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeClass<KType> {}");
    check(Type.INT,                   sp, "public class IntClass {}");
    check(Type.GENERIC,               sp, "public class ObjectClass<KType> {}");
  }

  @Test
  public void testClassExtendsNonTemplate() throws IOException {
    SignatureProcessor sp = new SignatureProcessor("public class KTypeVTypeClass<KType, VType> extends SuperClass {}");
    check(Type.INT,     Type.LONG,    sp, "public class IntLongClass extends SuperClass {}");
    check(Type.INT,     Type.GENERIC, sp, "public class IntObjectClass<VType> extends SuperClass {}");
    check(Type.GENERIC, Type.LONG,    sp, "public class ObjectLongClass<KType> extends SuperClass {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<KType, VType> extends SuperClass {}");
  }

  @Test
  public void testClassExtendsTemplate() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "public class KTypeVTypeClass<KType, VType> extends KTypeVTypeSuperClass<KType, VType> {}");
    check(Type.INT,     Type.LONG,    sp, "public class IntLongClass extends IntLongSuperClass {}");
    check(Type.INT,     Type.GENERIC, sp, "public class IntObjectClass<VType> extends IntObjectSuperClass<VType> {}");
    check(Type.GENERIC, Type.LONG,    sp, "public class ObjectLongClass<KType> extends ObjectLongSuperClass<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<KType, VType> extends ObjectObjectSuperClass<KType, VType> {}");
  }

  @Test
  public void testClassImplementsTemplate() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
          "public class KTypeVTypeClass<KType, VType> " +
          " extends     KTypeVTypeSuperClass<KType, VType>" +
          " implements  KTypeVTypeInterface<KType, VType> {}");

    check(Type.INT,     Type.LONG,    sp, "public class IntLongClass extends IntLongSuperClass implements IntLongInterface {}");
    check(Type.INT,     Type.GENERIC, sp, "public class IntObjectClass<VType> extends IntObjectSuperClass<VType> implements IntObjectInterface<VType> {}");
    check(Type.GENERIC, Type.LONG,    sp, "public class ObjectLongClass<KType> extends ObjectLongSuperClass<KType> implements ObjectLongInterface<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public class ObjectObjectClass<KType, VType> extends ObjectObjectSuperClass<KType, VType> implements ObjectObjectInterface<KType, VType> {}");
  }

  @Test
  public void testInterfaceKV() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "public interface KTypeVTypeInterface<KType, VType> " +
        "         extends KTypeVTypeSuper<KType, VType> {}");

    check(Type.INT,     Type.LONG,    sp, "public interface IntLongInterface extends IntLongSuper {}");
    check(Type.INT,     Type.GENERIC, sp, "public interface IntObjectInterface<VType> extends IntObjectSuper<VType> {}");
    check(Type.GENERIC, Type.LONG,    sp, "public interface ObjectLongInterface<KType> extends ObjectLongSuper<KType> {}");
    check(Type.GENERIC, Type.GENERIC, sp, "public interface ObjectObjectInterface<KType, VType> extends ObjectObjectSuper<KType, VType> {}");
  }

  @Test
  public void testImportDeclarations() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "import foo.KTypeVTypeClass; class Foo {}");

    check(Type.INT,     Type.LONG,    sp, "import foo.IntLongClass; class Foo {}");
    check(Type.INT,     Type.GENERIC, sp, "import foo.IntObjectClass; class Foo {}");
    check(Type.GENERIC, Type.LONG,    sp, "import foo.ObjectLongClass; class Foo {}");
    check(Type.GENERIC, Type.GENERIC, sp, "import foo.ObjectObjectClass; class Foo {}");
  }

  @Test
  public void testFieldDeclaration() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeFoo<KType> { KType foo; KType [] foo2; }");

    check(Type.FLOAT,   sp, "class FloatFoo { float foo; float [] foo2; }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { KType foo; KType [] foo2; }");
  }

  @Test
  public void testClassConstructor() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { public KTypeVTypeFoo(KType k, VType v) {} }");

    check(Type.FLOAT,   Type.DOUBLE,  sp, "class FloatDoubleFoo { public FloatDoubleFoo(float k, double v) {} }");
    check(Type.FLOAT,   Type.GENERIC, sp, "class FloatObjectFoo<VType> { public FloatObjectFoo(float k, VType v) {} }");
    check(Type.GENERIC, Type.FLOAT,   sp, "class ObjectFloatFoo<KType> { public ObjectFloatFoo(KType k, float v) {} }");
    check(Type.GENERIC, Type.GENERIC, sp, "class ObjectObjectFoo<KType, VType> { public ObjectObjectFoo(KType k, VType v) {} }");
  }

  @Test
  public void testThisReference() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { public void foo() { KTypeVTypeFoo.this.foo(); } }");

    check(Type.FLOAT, Type.DOUBLE, sp, "class FloatDoubleFoo { public void foo() { FloatDoubleFoo.this.foo(); } }");
  }

  @Test
  public void testNewClassDiamond() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { public void foo() { new KTypeVTypeFoo<>(); } }");

    check(Type.FLOAT,   Type.DOUBLE, sp, "class FloatDoubleFoo { public void foo() { new FloatDoubleFoo(); } }");
    check(Type.GENERIC, Type.DOUBLE, sp, "class ObjectDoubleFoo<KType> { public void foo() { new ObjectDoubleFoo<>(); } }");
  }

  @Test
  public void testNewClass() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { public void foo() { new KTypeVTypeFoo<KType, VType>(); } }");

    check(Type.FLOAT,   Type.DOUBLE, sp, "class FloatDoubleFoo { public void foo() { new FloatDoubleFoo(); } }");
    check(Type.GENERIC, Type.DOUBLE, sp, "class ObjectDoubleFoo<KType> { public void foo() { new ObjectDoubleFoo<KType>(); } }");
  }

  @Test
  public void testStaticGenericMethod() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { static <KType, VType> KTypeVTypeFoo foo(KType[] k, VType[] v) {} }");

    check(Type.FLOAT,   Type.DOUBLE, sp, "class FloatDoubleFoo { static FloatDoubleFoo foo(float[] k, double[] v) {} }");
    check(Type.GENERIC, Type.DOUBLE, sp, "class ObjectDoubleFoo<KType> { static <KType> ObjectDoubleFoo foo(KType[] k, double[] v) {} }");
  }

  @Test
  public void testWildcardBound() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeFoo<KType> { void bar(KTypeFoo<?> other) {} }");

    check(Type.FLOAT,   sp, "class FloatFoo { void bar(FloatFoo other) {} }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { void bar(ObjectFoo<?> other) {} }");
  }

  @Test
  public void testGenericNamedTypeBound() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeFoo<KType> { public <T extends KTypeBar<? super KType>> T forEach(T v) { throw new R(); } }");

    check(Type.FLOAT,   sp, "class FloatFoo         { public <T extends FloatBar> T forEach(T v) { throw new R(); } }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { public <T extends ObjectBar<? super KType>> T forEach(T v) { throw new R(); } }");
  }

  @Test
  public void testBug_ErasesObjectConstructor() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeVTypeFoo<KType, VType> { static { HashSet<Object> values = new HashSet<Object>(); }}");

    check(Type.FLOAT,   Type.INT,     sp, "class FloatIntFoo { static { HashSet<Object> values = new HashSet<Object>(); }}");
    check(Type.GENERIC, Type.GENERIC, sp, "class ObjectObjectFoo<KType, VType> { static { HashSet<Object> values = new HashSet<Object>(); }}");
  }

  @Test
  public void testBug_ErasesUntemplated() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeFoo<KType> { void foo() { KTypeBar<B> x = new KTypeBar<B>(); } }");

    check(Type.FLOAT,   sp, "class FloatFoo { void foo() { ObjectBar<B> x = new ObjectBar<B>(); } }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { void foo() { ObjectBar<B> x = new ObjectBar<B>(); } }");
  }

  @Test
  public void testBug_EraseNestedPrimitive() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "class KTypeFoo<KType> { static class Nested<KType> extends KTypeBar<KType> {} }");

    check(Type.FLOAT,   sp, "class FloatFoo { static class Nested extends FloatBar {} }");
    check(Type.GENERIC, sp, "class ObjectFoo<KType> { static class Nested<KType> extends ObjectBar<KType> {} }");
  }

  @Test
  public void testJavaDoc_k() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "/** KTypeFoo KTypes */");

    check(Type.FLOAT,   sp, "/** FloatFoo floats */");
    check(Type.GENERIC, sp, "/** ObjectFoo Objects */");
  }

  @Test
  public void testJavaDoc_kv() throws IOException {
    SignatureProcessor sp = new SignatureProcessor(
        "/** KTypeFoo KTypes KTypeVTypeFoo VTypes */");

    check(Type.FLOAT,   Type.DOUBLE,  sp, "/** FloatFoo floats FloatDoubleFoo doubles */");
    check(Type.GENERIC, Type.GENERIC, sp, "/** ObjectFoo Objects ObjectObjectFoo Objects */");
  }

  @Test
  public void testFullClass() throws IOException {
    Path path = Paths.get("src/test/java/com/carrotsearch/hppc/generator/parser/KTypeVTypeClass.java");
    // Path path = Paths.get("c:\\carrot2\\gh.carrotsearch\\hppc\\hppc\\src\\main\\templates\\com\\carrotsearch\\hppc\\KTypeArrayDeque.java");

    Assume.assumeTrue(Files.isRegularFile(path));
    SignatureProcessor sp = new SignatureProcessor(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    System.out.println(sp.process(new TemplateOptions(Type.LONG, Type.GENERIC)));
  }

  private void check(Type ktype, SignatureProcessor processor, String expected) throws IOException {
    check(new TemplateOptions(ktype), processor, expected);
  }

  private void check(Type ktype, Type vtype, SignatureProcessor processor, String expected) throws IOException {
    check(new TemplateOptions(ktype, vtype), processor, expected);
  }
  
  private void check(TemplateOptions templateOptions, SignatureProcessor processor, String expected) throws IOException {
    String output = processor.process(templateOptions);

    expected = expected.replaceAll("\\s+", " ");
    output   = output.replaceAll("\\s+", " ");

    if (!output.equals(expected)) {
      System.out.println(String.format(Locale.ROOT,
          "Output  : %s\n" +
          "Expected: %s\n", output, expected));
    }
    Assertions.assertThat(output).isEqualTo(expected);
  }
}
