/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc.sorted;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.predicates.*;

import java.util.Iterator;

import org.assertj.core.api.ThrowableAssert;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link SortedIterationKTypeVTypeHashMap}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class SortedIterationKTypeVTypeHashMapTest<KType, VType> extends AbstractKTypeVTypeTest<KType, VType> {

  private final KTypeVTypeHashMap<KType, VType> delegate = new KTypeVTypeHashMap<>();
  private boolean hasEmptyKey;
  private KType[] sortedKeys;
  private VType[] sortedValues;

  @Before
  public void initDelegate() {
    hasEmptyKey = randomBoolean();
    sortedKeys = hasEmptyKey ? newArray(keyE, key1, key2, key3, key4) : newArray(key1, key2, key3, key4);
    sortedValues = hasEmptyKey ? newvArray(value0, value1, value2, value3, value4) : newvArray(value1, value2, value3, value4);
    for (int i = sortedKeys.length - 1; i >= 0; i--) {
      delegate.put(sortedKeys[i], sortedValues[i]);
    }
  }

  @Test
  public void testGetContains() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view = new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate);
    assertThat(view.get(key3)).isEqualTo(value3);
    assertThat(view.get(keyE)).isEqualTo(hasEmptyKey ? value0 : Intrinsics.<VType> empty());
    assertThat(view.get(key5)).isEqualTo(Intrinsics.<VType> empty());
    assertThat(view.containsKey(key4)).isTrue();
    assertThat(view.containsKey(key5)).isFalse();
    assertThat(view.indexOf(key1)).isNotNegative();
    assertThat(view.indexExists(view.indexOf(key1))).isTrue();
    assertThat(view.indexGet(view.indexOf(key1))).isEqualTo(value1);
    assertThat(view.indexExists(view.indexOf(key5))).isFalse();
  }

  @Test
  public void testReadOnly() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view = new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate);
    assertUnsupported(() -> view.put(key1, value2));
    assertUnsupported(() -> view.putAll((KTypeVTypeAssociativeContainer<? extends KType, ? extends VType>) null));
    assertUnsupported(() -> view.putAll((Iterable<? extends KTypeVTypeCursor<? extends KType, ? extends VType>>) null));
    assertUnsupported(() -> view.remove(key1));
    assertUnsupported(() -> view.removeAll((KTypeContainer<? super KType>) null));
    assertUnsupported(() -> view.removeAll((KTypePredicate<? super KType>) null));
    assertUnsupported(() -> view.removeAll((KTypeVTypePredicate<? super KType, ? super VType>) null));
    assertUnsupported(() -> view.indexInsert(0, key1, value1));
    assertUnsupported(() -> view.indexReplace(0, value1));
    assertUnsupported(() -> view.indexRemove(0));
    assertUnsupported(view::clear);
    assertUnsupported(view::release);
  }

  @Test
  public void testIterator() {
    SortedIterationKTypeVTypeHashMap<KType, VType> view = new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate);
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

  private static void assertUnsupported(ThrowableAssert.ThrowingCallable callable) {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(callable);
  }
}
