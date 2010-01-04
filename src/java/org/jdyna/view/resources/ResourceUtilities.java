package org.jdyna.view.resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource access.
 */
public final class ResourceUtilities
{
    /** No instances. */
    private ResourceUtilities()
    {
    }
    
    /**
     * Open an input stream to a classpath-relative resource. For a resource placed
     * in
     * <pre>src/graphics/xxx/yyy.png</pre>
     * this would be
     * <pre>xxx/yyy.png</pre>
     * 
     * @param resourcePath Path to the resource.
     * @return An open {@link InputStream} that <b>must be closed</b> when done (<code>finally</code>).
     * @throws IOException when the resource is not found.
     */
    public static InputStream open(String resourcePath)
        throws IOException
    {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final InputStream is = cl.getResourceAsStream(resourcePath);
        if (is == null) throw new IOException("Resource not found: " + resourcePath);
        return is;
    }
}
