package org.jdyna.network.sockets.packets;

import java.io.Serializable;
import java.util.List;

import org.jdyna.network.sockets.GameHandle;

/**
 * Response to {@link ListGamesRequest}. 
 */
@SuppressWarnings("serial")
public class ListGamesResponse implements Serializable
{
    /** Game handles. */
    public List<GameHandle> handles;

    /*
     * 
     */
    protected ListGamesResponse()
    {
        // For deserialization.
    }

    /*
     * 
     */
    public ListGamesResponse(List<GameHandle> handles)
    {
        this.handles = handles;
    }
}
