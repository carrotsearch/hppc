package com.carrotsearch.hppc;

import org.junit.Before;

/**
 * Unit tests for {@link KTypeHashSet}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeScatterSetTest<KType> extends KTypeHashSetTest<KType>
{
    /* */
    @Before
    public void initialize()
    {
        set = new KTypeScatterSet<>();
    }
}
