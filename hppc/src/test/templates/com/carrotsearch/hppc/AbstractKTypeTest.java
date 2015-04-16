package com.carrotsearch.hppc;

import org.junit.Rule;
import org.junit.rules.MethodRule;

import com.carrotsearch.randomizedtesting.RandomizedTest;


/**
 * Unit helpers for <code>KType</code>.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
public abstract class AbstractKTypeTest<KType> extends RandomizedTest
{
    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* Ready to use key values. */

    protected KType keyE = Intrinsics.<KType> empty();
    protected KType key0 = cast(0), k0 = key0;
    protected KType key1 = cast(1), k1 = key1;
    protected KType key2 = cast(2), k2 = key2;
    protected KType key3 = cast(3), k3 = key3;
    protected KType key4 = cast(4), k4 = key4;
    protected KType key5 = cast(5), k5 = key5;
    protected KType key6 = cast(6), k6 = key6;
    protected KType key7 = cast(7), k7 = key7;
    protected KType key8 = cast(8), k8 = key8;
    protected KType key9 = cast(9), k9 = key9;

    /**
     * Convert to target type from an integer used to test stuff. 
     */
    public KType cast(Integer v)
    {
        /*! #if ($TemplateOptions.KTypePrimitive)
            return (KType) v.intValue();
            #else !*/ 
            // @SuppressWarnings("unchecked")        
            KType k = (KType)(Object) v;
            return k;
        /*! #end !*/
    }

    public KType [] asArray(int... ints)
    {
        KType [] values = Intrinsics.<KType> newArray(ints.length);
        for (int i = 0; i < ints.length; i++)
            values[i] = (KType) /*! #if ($TemplateOptions.KTypeGeneric) !*/ (Object) /*! #end !*/ ints[i];
        return values;
    }
    
    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    /* #if ($TemplateOptions.KTypeGeneric) */
    @SafeVarargs
    /* #end */
    public final KType [] newArray(KType... elements)
    {
        return newArray0(elements);
    }

    /* #if ($TemplateOptions.KTypeGeneric) */
    @SafeVarargs
    /* #end */
    private final KType [] newArray0(KType... elements)
    {
        return elements;
    }

    public KType [] newArray(KType v0) { return this.newArray0(v0); }
    public KType [] newArray(KType v0, KType v1) { return this.newArray0(v0, v1); }
    public KType [] newArray(KType v0, KType v1, KType v2) { return this.newArray0(v0, v1, v2); }
    public KType [] newArray(KType v0, KType v1, KType v2, KType v3) { return this.newArray0(v0, v1, v2, v3); }
    public KType [] newArray(KType v0, KType v1, KType v2, KType v3,
                             KType v4, KType v5, KType v6) { return this.newArray0(v0, v1, v2, v3, v4, v5, v6); }
    public KType [] newArray(KType v0, KType v1, KType v2, KType v3,
                             KType v4, KType v5) { return this.newArray0(v0, v1, v2, v3, v4, v5); }
    public KType [] newArray(KType v0, KType v1, KType v2, KType v3,
                             KType v4) { return this.newArray0(v0, v1, v2, v3, v4); }
    public KType [] newArray(KType v0, KType v1, KType v2, KType v3,
                             KType v4, KType v5, KType v6, KType v7) { return this.newArray0(v0, v1, v2, v3, v4, v5, v6, v7); }
}
