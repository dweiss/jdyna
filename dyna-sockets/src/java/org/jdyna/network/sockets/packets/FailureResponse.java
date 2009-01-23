package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * A server failure response to any packet. 
 */
public class FailureResponse implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Problem message. */
    public String message;

    /** Stack trace, if any. */
    public Throwable throwable;

    /*
     * 
     */
    protected FailureResponse()
    {
        // For deserialization.
    }

    /*
     * 
     */
    public FailureResponse(String message, Throwable t)
    {
        this.message = message;
        this.throwable = t;
    }
    
    /*
     * 
     */
    public FailureResponse(String message)
    {
        this(message, null);
    }

    /*
     * 
     */
    public FailureResponse(Throwable t)
    {
        this(t.getMessage(), t);
    }
    
    /*
     * 
     */
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        if (message != null)
        {
            b.append(message);
            b.append("\n");
        }
        if (throwable != null)
        {
            b.append(ExceptionUtils.getFullStackTrace(throwable));
        }

        return b.toString();
    }
}
