package com.carrotsearch.hppc.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions
{
    public Type ktype;
    public Type vtype;
    
    public File sourceFile;

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

    public boolean isVTypePrimitive()
    {
        return getVType() != Type.GENERIC;
    }

    public boolean isKTypeGeneric()
    {
        return ktype == Type.GENERIC;
    }

    public boolean isVTypeGeneric()
    {
        return getVType() == Type.GENERIC;
    }

    public boolean isAllGeneric()
    {
        return isKTypeGeneric() && isVTypeGeneric();
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
    
    /**
     * Returns the current time in ISO format.
     */
    public String getTimeNow()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
        return format.format(new Date());
    }
    
    public String getSourceFile()
    {
        return sourceFile.getName();
    }
    
    public String getGeneratedAnnotation()
    {
        return "@javax.annotation.Generated(date = \"" + 
            getTimeNow() + "\", value = \"HPPC generated from: " +
            sourceFile.getName() + "\")";
    }

    public boolean isAnyGeneric()
    {
        return isKTypeGeneric() || (hasVType() && isVTypeGeneric());
    }
}