package com.carrotsearch.hppc.structs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.processing.Messager;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * Resource loader for velocity.
 */
final class ClassRelativeResourceLoader extends ResourceLoader
{
    private final Class<?> clazz;

    @SuppressWarnings("unused")
    private final Messager msg;

    ClassRelativeResourceLoader(Messager msg, Class<?> clazz)
    {
        this.clazz = clazz;
        this.msg = msg;
    }

    @Override
    public void init(ExtendedProperties props)
    {
        // ignore.
    }

    /**
     * 
     */
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException
    {
        /*
         * Do some protocol connection magic because JAR URLs are cached and this
         * complicates development (the template is not found once loaded).
         */
        URL resource = clazz.getResource(name);
        if (resource == null) throw new ResourceNotFoundException("Resource not found: "
            + name);

        try
        {
            URLConnection connection = resource.openConnection();
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            try
            {
                return new ByteArrayInputStream(ByteStreams.toByteArray(inputStream));
            }
            finally
            {
                Closeables.closeQuietly(inputStream);
            }
        }
        catch (Exception e)
        {
            throw new ResourceNotFoundException(e);
        }
    }

    /**
     * 
     */
    @Override
    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    /**
     * 
     */
    @Override
    public long getLastModified(Resource resource)
    {
        return 0L;
    }
}
