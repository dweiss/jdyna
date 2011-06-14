package org.jdyna.network.sockets;

/**
 * Internal exception indicating a failure response should be sent to the client. 
 */
@SuppressWarnings("serial")
final class FailureResponseException extends RuntimeException
{
    public final String message;

    public FailureResponseException(String message)
    {
        this.message = message;
    }
}
