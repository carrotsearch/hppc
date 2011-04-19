package com.carrotsearch.hppc.generator;

import java.io.File;

class OutputFile
{
    public final File file;
    public boolean generated;
    public boolean updated = false;

    public OutputFile(File target, boolean generated)
    {
        this.file = TemplateProcessor.canonicalFile(target);
        this.generated = generated;
    }
}