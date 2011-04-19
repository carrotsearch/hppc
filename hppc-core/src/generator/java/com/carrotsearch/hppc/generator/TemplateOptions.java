package com.carrotsearch.hppc.generator;


/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions
{
    public Type ktype;
    public Type vtype;

    public TemplateOptions(Type ktype)
    {
        this(ktype, null);
    }

    public TemplateOptions(Type ktype, Type vtype)
    {
        this.ktype = ktype;
        this.vtype = vtype;
    }

    public boolean isKTypePrimitive()
    {
        return ktype != Type.GENERIC;
    }

    public boolean isKTypeGeneric()
    {
        return ktype == Type.GENERIC;
    }

    public boolean hasVType()
    {
        return vtype != null;
    }

    public Type getKType()
    {
        return ktype;
    }

    public Type getVType()
    {
        if (vtype == null) throw new RuntimeException("VType is null.");
        return vtype;
    }
}