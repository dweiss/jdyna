package org.jdyna.network.sockets;

/*
 * 
 */
final class FailureResponseException extends RuntimeException
{
    /** */
    private static final long serialVersionUID = 1L;

    public final String message;

    public FailureResponseException(String message)
    {
        this.message = message;
    }
}
