package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import org.jdyna.network.sockets.PlayerHandle;

/**
 *  
 */
public class JoinGameResponse implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Player handle. */
    public PlayerHandle handle;

    /*
     * 
     */
    protected JoinGameResponse()
    {
        // For deserialization.
    }

    /*
     * 
     */
    public JoinGameResponse(PlayerHandle handle)
    {
        this.handle = handle;
    }
}
