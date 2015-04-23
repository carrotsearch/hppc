package com.carrotsearch.hppc;

import org.junit.Before;

/**
 * Unit tests for {@link KTypeOpenHashSet}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeScatterSetTest<KType> extends KTypeOpenHashSetTest<KType>
{
    /* */
    @Before
    public void initialize()
    {
        set = new KTypeScatterSet<>();
    }
}
