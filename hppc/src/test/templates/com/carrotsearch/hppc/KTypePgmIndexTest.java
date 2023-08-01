/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("GENERIC", "BYTE", "SHORT", "CHAR"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.procedures.KTypeProcedure;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link KTypePgmIndex}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypePgmIndexTest<KType> extends AbstractKTypeTest<KType> {

  /*! #if ($TemplateOptions.KTypeGeneric) !*/ @Ignore /*! #end !*/
  @Test
  public void testSanityOneSegmentLevel() {
    KType[] keys = asArray(2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034);
    KTypeArrayList<KType> keyList = new KTypeArrayList<>();
    keyList.add(keys, 0, keys.length);
    KTypePgmIndex.KTypeBuilder<KType> builder = new KTypePgmIndex.KTypeBuilder<KType>()
      .setSortedKeys(keyList)
      .setEpsilon(4)
      .setEpsilonRecursive(2);
    KTypePgmIndex<KType> pgmIndex = builder.build();
    assertEquals(keys.length, pgmIndex.size());
    for (KType key : keys) {
      assertTrue(pgmIndex.contains(key));
    }
    assertFalse(pgmIndex.contains(cast(1)));
    assertFalse(pgmIndex.contains(cast(116)));
    assertFalse(pgmIndex.contains(cast(120)));
    assertFalse(pgmIndex.contains(cast(1190)));
    assertFalse(pgmIndex.contains(cast(1192)));
    assertFalse(pgmIndex.contains(cast(1200)));
    assertFalse(pgmIndex.contains(cast(2000)));
    assertFalse(pgmIndex.contains(cast(4031)));
    System.out.println("pgmIndex.ramBytesAllocated() = " + pgmIndex.ramBytesAllocated() + " B");
    System.out.println("pgmIndex.ramBytesUsed() = " + pgmIndex.ramBytesUsed() + " B");
    System.out.println("builder.ramBytesAllocated() = " + builder.ramBytesAllocated() + " B");
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/ @Ignore /*! #end !*/
  @Test
  public void testSanityTwoSegmentLevels() {
    KType[] keys = asArray(2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034, 4035, 4036, 4037, 4039, 4900);
    KTypePgmIndex.KTypeBuilder<KType> builder = new KTypePgmIndex.KTypeBuilder<KType>()
      .setSortedKeys(keys, keys.length)
      .setEpsilon(1)
      .setEpsilonRecursive(1);
    KTypePgmIndex<KType> pgmIndex = builder.build();
    assertEquals(keys.length, pgmIndex.size());
    for (KType key : keys) {
      assertTrue(pgmIndex.contains(key));
    }
    System.out.println("pgmIndex.ramBytesAllocated() = " + pgmIndex.ramBytesAllocated() + " B");
    System.out.println("pgmIndex.ramBytesUsed() = " + pgmIndex.ramBytesUsed() + " B");
    System.out.println("builder.ramBytesAllocated() = " + builder.ramBytesAllocated() + " B");
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/ @Ignore /*! #end !*/
  @Test
  public void testRangeIterator() {
    KType[] keys = asArray(2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034, 4035, 4036, 4037, 4039, 4900);
    KTypePgmIndex.KTypeBuilder<KType> builder = new KTypePgmIndex.KTypeBuilder<KType>()
      .setSortedKeys(keys, keys.length)
      .setEpsilon(1)
      .setEpsilonRecursive(1);
    KTypePgmIndex<KType> pgmIndex = builder.build();
    assertIterator(123, 1191, pgmIndex, 123, 1024, 1129, 1191);
    assertIterator(1100, 1300, pgmIndex, 1129, 1191, 1201);
    assertIterator(-1, 100, pgmIndex, 2, 12);
    assertIterator(Integer.MIN_VALUE, 100, pgmIndex, 2, 12);
    assertIterator(Integer.MIN_VALUE, Integer.MAX_VALUE, pgmIndex, 2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034, 4035, 4036, 4037, 4039, 4900);
    assertIterator(4036, Integer.MAX_VALUE, pgmIndex, 4036, 4037, 4039, 4900);
    assertIterator(4039, 4500, pgmIndex, 4039);
    assertIterator(4040, 4500, pgmIndex);
  }

  private void assertIterator(int minKey, int maxKey, KTypePgmIndex<KType> pgmIndex, int... expectedKeys) {
    Iterator<KTypeCursor<KType>> iterator = pgmIndex.rangeIterator(cast(minKey), cast(maxKey));
    for (int expectedKey : expectedKeys) {
      if (randomBoolean()) {
        assertTrue(iterator.hasNext());
      }
      assertTrue(Intrinsics.<KType>equals(cast(expectedKey), iterator.next().value));
    }
    assertFalse(iterator.hasNext());
    assertEquals(expectedKeys.length, pgmIndex.rangeCardinality(cast(minKey), cast(maxKey)));
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/ @Ignore /*! #end !*/
  @Test
  public void testRangeProcedure() {
    KType[] keys = asArray(2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034, 4035, 4036, 4037, 4039, 4900);
    KTypePgmIndex.KTypeBuilder<KType> builder = new KTypePgmIndex.KTypeBuilder<KType>()
      .setSortedKeys(keys, keys.length)
      .setEpsilon(1)
      .setEpsilonRecursive(1);
    KTypePgmIndex<KType> pgmIndex = builder.build();
    assertProcedure(123, 1191, pgmIndex, 123, 1024, 1129, 1191);
    assertProcedure(1100, 1300, pgmIndex, 1129, 1191, 1201);
    assertProcedure(-1, 100, pgmIndex, 2, 12);
    assertProcedure(Integer.MIN_VALUE, 100, pgmIndex, 2, 12);
    assertProcedure(Integer.MIN_VALUE, Integer.MAX_VALUE, pgmIndex, 2, 12, 115, 118, 123, 1024, 1129, 1191, 1201, 4034, 4035, 4036, 4037, 4039, 4900);
    assertProcedure(4036, Integer.MAX_VALUE, pgmIndex, 4036, 4037, 4039, 4900);
    assertProcedure(4039, 4500, pgmIndex, 4039);
    assertProcedure(4040, 4500, pgmIndex);
  }

  private void assertProcedure(int minKey, int maxKey, KTypePgmIndex<KType> pgmIndex, int... expectedKeys) {
    KTypeArrayList<KType> processedKeys = new KTypeArrayList<KType>();
    KTypeProcedure<KType> procedure = new KTypeProcedure<KType>() {
      @Override
      public void apply(KType key) {
        processedKeys.add(key);
      }
    };
    pgmIndex.forEachInRange(procedure, cast(minKey), cast(maxKey));
    assertEquals(KTypeArrayList.from(asArray(expectedKeys)), processedKeys);
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/ @Ignore /*! #end !*/
  @Test
  public void testAgainstHashSet() {
    final Random random = RandomizedTest.getRandom();
    for (int i = 0; i < 1; i++) {
      //System.out.println("Loop " + i);

      KType[] additions = Intrinsics.<KType>newArray(1_000_000);
      for (int j = 0; j < additions.length; j++) {
        additions[j] = Intrinsics.<KType>cast(
          /*! #if ($TemplateOptions.isKTypeAnyOf("INT")) !*/ random.nextInt() /*! #end !*/
          /*! #if ($TemplateOptions.isKTypeAnyOf("LONG")) random.nextLong() #end !*/
          /*! #if ($TemplateOptions.isKTypeAnyOf("FLOAT")) random.nextFloat() * random.nextInt() #end !*/
          /*! #if ($TemplateOptions.isKTypeAnyOf("DOUBLE")) random.nextDouble() * random.nextLong() #end !*/
        );
      }
      Arrays.sort(additions);
      // Make sure there is at least one sequence of duplicate keys.
      int originalKeyIndex = random.nextInt(100_000);
      for (int j = 0, numDups = random.nextInt(1_000) + 1; j < numDups; j++) {
        additions[originalKeyIndex + j + 1] = additions[originalKeyIndex];
      }

      KTypePgmIndex.KTypeBuilder<KType> builder =
        new KTypePgmIndex.KTypeBuilder<KType>()
          .setSortedKeys(additions, additions.length);
      if (random.nextBoolean()) {
        builder.setEpsilon(random.nextInt(128) + 1);
        builder.setEpsilonRecursive(random.nextInt(16) + 1);
      }
      KTypePgmIndex<KType> pgmIndex = builder.build();

      Set<Object> hashSet = new HashSet<>();
      for (KType addition : additions) {
        hashSet.add(addition);
      }

      assertEquals(hashSet.size(), pgmIndex.size());
      for (int j = 0; j < additions.length; j++) {
        assertTrue(String.valueOf(j), pgmIndex.contains(additions[j]));
        assertTrue(Intrinsics.<KType>equals(additions[j], additions[pgmIndex.indexOf(additions[j])]));
      }
      random.ints(1_000_000).forEach((key) -> {
        assertEquals(String.valueOf(key), hashSet.contains(cast(key)), pgmIndex.contains(cast(key)));
        int index = pgmIndex.indexOf(cast(key));
        if (hashSet.contains(cast(key))) {
          assertTrue(Intrinsics.<KType>equals(key, additions[index]));
        } else {
          int insertionIndex = -index - 1;
          assertTrue(insertionIndex >= 0);
          assertTrue(insertionIndex <= additions.length);
          if (insertionIndex < additions.length) {
            assertTrue(String.valueOf(key), Intrinsics.<KType>numeric(additions[insertionIndex]) > key);
          }
          if (insertionIndex > 0) {
            assertTrue(String.valueOf(key), Intrinsics.<KType>numeric(additions[insertionIndex - 1]) < key);
          }
        }
      });

      System.out.println("pgmIndex.ramBytesAllocated() = " + pgmIndex.ramBytesAllocated() + " B");
      System.out.println("pgmIndex.ramBytesUsed() = " + pgmIndex.ramBytesUsed() + " B");
      System.out.println("builder.ramBytesAllocated() = " + builder.ramBytesAllocated() + " B");
    }
  }
}
