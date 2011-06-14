package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import org.jdyna.network.sockets.GameHandle;

/**
 * Response to {@link CreateGameRequest}. 
 */
public class CreateGameResponse implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Game handle. */
    public GameHandle handle;

    /*
     * 
     */
    protected CreateGameResponse()
    {
        // For deserialization.
    }

    /*
     * 
     */
    public CreateGameResponse(GameHandle handle)
    {
        this.handle = handle;
    }
}
