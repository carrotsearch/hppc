package com.carrotsearch.hppc;

/**
 * Unit helpers for <code>KType</code>, <code>VType</code> pair.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
public class AbstractKTypeVTypeTest<KType, VType> extends AbstractKTypeTest<KType> {

  protected VType value0 = vcast(0);
  protected VType value1 = vcast(1);
  protected VType value2 = vcast(2);
  protected VType value3 = vcast(3);
  protected VType value4 = vcast(4);

  /**
   * Convert to target type from an integer used to test stuff.
   */
  protected VType vcast(int value)
  {
        /*! #if ($TemplateOptions.VTypePrimitive)
            return (VType) value;
            #else !*/
    @SuppressWarnings("unchecked")
    VType v = (VType)(Object) value;
    return v;
    /*! #end !*/
  }

  /**
   * Create a new array of a given type and copy the arguments to this array.
   */
  /* #if ($TemplateOptions.VTypeGeneric) */
  @SafeVarargs
  /* #end */
  protected final VType [] newvArray(VType... elements)
  {
    return elements;
  }
}
