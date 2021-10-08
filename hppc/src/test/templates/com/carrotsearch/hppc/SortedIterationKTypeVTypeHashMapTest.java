/*! #set($TemplateOptions.ignored = ($TemplateOptions.isKTypeAnyOf("DOUBLE", "FLOAT", "BYTE"))) !*/
package com.carrotsearch.hppc;

import com.carrotsearch.hppc.comparators.KTypeVTypeComparator;
import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.cursors.KTypeVTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.predicates.KTypeVTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeVTypeProcedure;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static com.carrotsearch.hppc.TestUtils.*;

/*! #if ($TemplateOptions.AnyPrimitive)
import com.carrotsearch.hppc.comparators.*;
import com.carrotsearch.hppc.cursors.*;
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
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getComparator(false));
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
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getComparator(false));
    assertUnsupported(() -> view.put(key1, value2));
    assertUnsupported(
        () -> view.putAll(null));
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
  @Repeat(iterations = 10)
  public void testIterator() {
    boolean reversedOrder = randomBoolean();
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
        new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getComparator(reversedOrder));
    Iterator<KTypeVTypeCursor<KType, VType>> iterator = view.iterator();
    for (int i = 0; i < sortedKeys.length; i++) {
      int index = reversedOrder ? sortedKeys.length - 1 - i : i;
      assertThat(iterator.hasNext()).isTrue();
      KTypeVTypeCursor<KType, VType> cursor = iterator.next();
      assertThat(cursor.key).isEqualTo(sortedKeys[index]);
      assertThat(cursor.value).isEqualTo(sortedValues[index]);
      assertThat(view.indexGet(cursor.index)).isEqualTo(sortedValues[index]);
    }
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  @Repeat(iterations = 10)
  public void testForEachProcedure() {
    boolean reversedOrder = randomBoolean();
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
      new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getValuesComparator(reversedOrder));
    KTypeArrayList<KType> iteratedKeys = new KTypeArrayList<KType>();
    KTypeArrayList<VType> iteratedValues = new KTypeArrayList<VType>();
    KTypeVTypeProcedure<KType, VType> procedure = (k, v) -> {
      iteratedKeys.add(k);
      iteratedValues.add(v);
    };
    view.forEach(procedure);
    assertThat(iteratedKeys.toArray()).isEqualTo(copyKeys(sortedKeys, reversedOrder));
    assertThat(iteratedValues.toArray()).isEqualTo(copyValues(sortedValues, reversedOrder));
  }

  @Test
  @Repeat(iterations = 10)
  public void testForEachPredicate() {
    boolean reversedOrder = randomBoolean();
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
      new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getValuesComparator(reversedOrder));
    KTypeArrayList<KType> iteratedKeys = new KTypeArrayList<KType>();
    KTypeArrayList<VType> iteratedValues = new KTypeArrayList<VType>();
    int iterationStop = randomIntBetween(1, sortedKeys.length + 1);
    KTypeVTypePredicate<KType, VType> predicate = (k, v) -> {
      iteratedKeys.add(k);
      iteratedValues.add(v);
      return iteratedKeys.size() != iterationStop;
    };
    view.forEach(predicate);
    KType[] expectedKeys = copyKeys(sortedKeys, reversedOrder);
    expectedKeys = Arrays.copyOfRange(expectedKeys, 0, Math.min(iterationStop, expectedKeys.length));
    assertThat(iteratedKeys.toArray()).isEqualTo(expectedKeys);
    VType[] expectedValues = copyValues(sortedValues, reversedOrder);
    expectedValues = Arrays.copyOfRange(expectedValues, 0, Math.min(iterationStop, expectedValues.length));
    assertThat(iteratedValues.toArray()).isEqualTo(expectedValues);
  }

  @Test
  @Repeat(iterations = 10)
  public void testKeyIterator() {
    boolean reversedOrder = randomBoolean();
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
      new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getComparator(reversedOrder));
    Iterator<KTypeCursor<KType>> iterator = view.keys().iterator();
    for (int i = 0; i < sortedKeys.length; i++) {
      int index = reversedOrder ? sortedKeys.length - 1 - i : i;
      assertThat(iterator.hasNext()).isTrue();
      KTypeCursor<KType> cursor = iterator.next();
      assertThat(cursor.value).isEqualTo(sortedKeys[index]);
      assertThat(view.indexGet(cursor.index)).isEqualTo(sortedValues[index]);
    }
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  @Repeat(iterations = 10)
  public void testValueIterator() {
    boolean reversedOrder = randomBoolean();
    SortedIterationKTypeVTypeHashMap<KType, VType> view =
      new SortedIterationKTypeVTypeHashMap<KType, VType>(delegate, getComparator(reversedOrder));
    Iterator<KTypeCursor<VType>> iterator = view.values().iterator();
    for (int i = 0; i < sortedValues.length; i++) {
      int index = reversedOrder ? sortedValues.length - 1 - i : i;
      assertThat(iterator.hasNext()).isTrue();
      KTypeCursor<VType> cursor = iterator.next();
      assertThat(cursor.value).isEqualTo(sortedValues[index]);
      assertThat(view.indexGet(cursor.index)).isEqualTo(sortedValues[index]);
    }
    assertThat(iterator.hasNext()).isFalse();
  }

  private static void assertUnsupported(ThrowableAssert.ThrowingCallable callable) {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(callable);
  }

  /*! #if ($TemplateOptions.KTypeGeneric) !*/
  @SuppressWarnings("unchecked")
  private Comparator<KType> getComparator(boolean reversedOrder) {
    Comparator<KType> c = Comparator.nullsFirst((a, b) -> ((Comparable<KType>) a).compareTo(b));
    return reversedOrder ? c.reversed() : c;
  }
  /*! #end !*/

  /*! #if ($TemplateOptions.KTypePrimitive)
  private KTypeComparator<KType> getComparator(boolean reversedOrder) {
    KTypeComparator c = KTypeComparator.naturalOrder();
    return reversedOrder ? (a, b) -> c.compare(b, a) : c;
  }
  #end !*/

  /*! #if ($TemplateOptions.VTypeGeneric) !*/
  @SuppressWarnings("unchecked")
  private KTypeVTypeComparator<KType, VType> getValuesComparator(boolean reversedOrder) {
    Comparator<VType> c = Comparator.nullsFirst((a, b) -> ((Comparable<VType>) a).compareTo(b));
    Comparator<VType> finalComp = reversedOrder ? c.reversed() : c;
    return (k1, v1, k2, v2) -> finalComp.compare(v1, v2);
  }
  /*! #end !*/

  /*! #if ($TemplateOptions.VTypePrimitive)
  private KTypeVTypeComparator<KType, VType> getValuesComparator(boolean reversedOrder) {
    int less = reversedOrder ? 1 : -1;
    return (k1, v1, k2, v2) -> v1 == v2 ? 0 : v1 < v2 ? less : -less;
  }
  #end !*/

  private KType[] copyKeys(KType[] keys, boolean reversed) {
    KType[] copy = Arrays.copyOf(keys, keys.length);
    return reversed ? reverse(copy) : copy;
  }

  private VType[] copyValues(VType[] values, boolean reversed) {
    VType[] copy = Arrays.copyOf(values, values.length);
    return reversed ? reverse(copy) : copy;
  }
}
