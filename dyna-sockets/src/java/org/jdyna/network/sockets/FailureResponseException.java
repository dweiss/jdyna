package org.jdyna.network.sockets;

/*
 * 
 */
final class FailureResponseException extends RuntimeException
{
    public final String message;

    public FailureResponseException(String message)
    {
        this.message = message;
    }
}
