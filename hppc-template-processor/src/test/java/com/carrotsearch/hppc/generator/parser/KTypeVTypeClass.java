package com.carrotsearch.hppc.generator.parser;

import com.carrotsearch.hppc.generator.parser.subpackage.KTypeVTypeInterfaceImported;

/**
 * A list of KTypes. 
 */
public class KTypeVTypeClass<KType, VType> 
       extends    KTypeVTypeSuper<KType, VType>
       implements KTypeVTypeInterface<KType, VType>,
                  KTypeVTypeInterfaceImported<KType, VType>,
                  Cloneable
{
  KType [] keys;

  public KTypeVTypeClass() {}
  public KTypeVTypeClass(KType[] foo, VType boo) {}

  public KTypeVTypeClass(KTypeVTypeSuper<KType, VType> foo) {}
  public VType foo(KType key, VType value) { throw new RuntimeException(); }
  
  public void foo(KTypeVTypeSuper<? extends KType, ? extends VType> foo) {}
}

class KTypeVTypeSuper<KType, VType> {}
interface KTypeVTypeInterface<KType, VType> {}