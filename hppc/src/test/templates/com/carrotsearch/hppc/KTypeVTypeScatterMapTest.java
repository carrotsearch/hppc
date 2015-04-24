package com.carrotsearch.hppc;

import org.junit.Ignore;


/**
 * Tests for {@link KTypeVTypeHashMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeScatterMapTest<KType, VType> extends KTypeVTypeHashMapTest<KType, VType>
{
  @Override
  protected KTypeVTypeHashMap<KType, VType> newInstance() {
    return new KTypeVTypeScatterMap<>();
  }

  @Override @Ignore
  public void testEqualsSameClass() {
  }
}
