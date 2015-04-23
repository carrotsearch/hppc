package com.carrotsearch.hppc;

import org.junit.Ignore;


/**
 * Tests for {@link KTypeVTypeOpenHashMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeScatterMapTest<KType, VType> extends KTypeVTypeOpenHashMapTest<KType, VType>
{
  @Override
  protected KTypeVTypeOpenHashMap<KType, VType> newInstance() {
    return new KTypeVTypeScatterMap<>();
  }

  @Override @Ignore
  public void testEqualsSameClass() {
  }
}
