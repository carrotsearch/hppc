package com.carrotsearch.hppc;

/**
 * Some CPU intensive tests run forever with Clover, so we hack these tests to run fewer
 * rounds if so.
 */
public class CloverSupport
{
    private final static boolean clover;
    static
    {
        boolean withClover;
        try
        {
            Class.forName("com_cenqua_clover.CoverageRecorder", true, Thread
                .currentThread().getContextClassLoader());
            withClover = true;
        }
        catch (Exception e)
        {
            withClover = false;
        }

        clover = withClover;
    }

    /**
     * @return Returns <code>true</code> if clover is in classpath (most likely indicating
     *         the tests are running under clover).
     */
    public static boolean isClover()
    {
        return clover;
    }
}
