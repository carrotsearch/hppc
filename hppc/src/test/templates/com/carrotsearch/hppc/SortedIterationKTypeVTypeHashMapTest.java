/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.KTypeVTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.predicates.KTypeVTypePredicate;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/*! #if ($TemplateOptions.KTypePrimitive)
import com.carrotsearch.hppc.comparators.*;
#end !*/

/** Tests for {@link SortedIterationKTypeVTypeHashMap}. */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class SortedIterationKTypeVTypeHashMapTest<KType, VType>
    extends AbstractKTypeVTypeTest<KType, VType> {

  private final KTypeVTypeHashMap<KType, VType> delegate = new KTypeVTypeHashMap<>();
  private boolean hasEmptyKey;
  private KType[] sortedKeys;
  private VType[] sortedValues;

  @Before
  public void initDelegate() {
    hasEmptyKey = randomBoolean();
    sortedKeys =
        hasEmptyKey ? newArray(keyE, key1, key2, key3, key4) : newArray(key1, key2, key3, key4);
    sortedValues =
        hasEmptyKey
            ? newvArray(value0, value1, value2, value3, value4)
            : newvArray(value1, value2, value3, value4);
    for (int i = sortedKeys.length - 1; i >= 0; i--) {
      delegate.put(sortedKeys[i], sortedValues[i]);
    }
  }

  @Test
  public void testGetContains() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getNaturalComparator());
    assertThat(view.get(key3)).isEqualTo(value3);
    assertThat(view.get(keyE)).isEqualTo(hasEmptyKey ? value0 : Intrinsics.<VType>empty());
    assertThat(view.get(key5)).isEqualTo(Intrinsics.<VType>empty());
    assertThat(view.containsKey(key4)).isTrue();
    assertThat(view.containsKey(key5)).isFalse();
    assertThat(view.indexOf(key1)).isNotNegative();
    assertThat(view.indexExists(view.indexOf(key1))).isTrue();
    assertThat(view.indexGet(view.indexOf(key1))).isEqualTo(value1);
    assertThat(view.indexExists(view.indexOf(key5))).isFalse();
  }

  @Test
  public void testReadOnly() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getNaturalComparator());
    assertUnsupported(() -> view.put(key1, value2));
    assertUnsupported(
        () -> view.putAll((KTypeVTypeAssociativeContainer<? extends KType, ? extends VType>) null));
    assertUnsupported(
        () ->
            view.putAll(
                (Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>>) null));
    assertUnsupported(() -> view.remove(key1));
    assertUnsupported(() -> view.removeAll((KTypeContainer<? super KType>) null));
    assertUnsupported(() -> view.removeAll((KTypePredicate<? super KType>) null));
    assertUnsupported(
        () -> view.removeAll((KTypeVTypePredicate<? super KType, ? super VType>) null));
    assertUnsupported(() -> view.indexInsert(0, key1, value1));
    assertUnsupported(() -> view.indexReplace(0, value1));
    assertUnsupported(() -> view.indexRemove(0));
    assertUnsupported(view::clear);
    assertUnsupported(view::release);
  }

  @Test
  public void testIterator() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getNaturalComparator());
    Iterator<KTypeVTypeCursor<KType, VType>> iterator = view.iterator();
    for (int i = 0; i < sortedKeys.length; i++) {
      assertThat(iterator.hasNext()).isTrue();
      KTypeVTypeCursor<KType, VType> cursor = iterator.next();
      assertThat(cursor.key).isEqualTo(sortedKeys[i]);
      assertThat(cursor.value).isEqualTo(sortedValues[i]);
      assertThat(view.indexGet(cursor.index)).isEqualTo(sortedValues[i]);
    }
    assertThat(iterator.hasNext()).isFalse();
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/
  @SuppressWarnings("unchecked")
  private Comparator<KType> getNaturalComparator() {
    return (a, b) -> ((Comparable<KType>) a).compareTo(b);
  }
  /*! #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("BYTE"))
  private KTypeComparator<KType> getNaturalComparator() {
    return Byte::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("CHAR"))
  private KTypeComparator<KType> getNaturalComparator() {
    return Character::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("INT"))
  private KTypeComparator<KType> getNaturalComparator() {
    return Integer::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("LONG"))
  private KTypeComparator<KType> getNaturalComparator() {
    return Long::compare;
  }
  #end !*/

  /*! #if ($TemplateOptions.isKTypeAnyOf("SHORT"))
  private KTypeComparator<KType> getNaturalComparator() {
    return Short::compare;
  }
  #end !*/

  private static void assertUnsupported(ThrowableAssert.ThrowingCallable callable) {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(callable);
  }
}
