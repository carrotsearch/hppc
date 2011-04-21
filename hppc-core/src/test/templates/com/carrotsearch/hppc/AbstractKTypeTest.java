package com.carrotsearch.hppc;

import org.junit.Rule;
import org.junit.rules.MethodRule;


/**
 * Unit helpers for <code>KType</code>.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeTest<KType>
{
    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /**
     * Convert to target type from an integer used to test stuff. 
     */
    public KType cast(int v)
    {
        /*! #if ($TemplateOptions.KTypePrimitive)
            return (KType) v;
            #else !*/ 
            @SuppressWarnings("unchecked")        
            KType k = (KType)(Object) v;
            return k;
        /*! #end !*/
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    public KType [] asArray(int... ints)
    {
        KType [] values = Intrinsics.newKTypeArray(ints.length);
        for (int i = 0; i < ints.length; i++)
            values[i] = (KType) /*! #if ($TemplateOptions.KTypeGeneric) !*/ (Object) /*! #end !*/ ints[i];
        return values;
    }
}
