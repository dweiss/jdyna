package org.jdyna.network.sockets.packets;

import java.io.Serializable;

/**
 *  
 */
public class JoinGameRequest implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** */
    public int gameID;

    /** */
    public String playerName;

    protected JoinGameRequest()
    {
        // Serialization.
    }
    
    /*
     * 
     */
    public JoinGameRequest(int gameID, String playerName)
    {
        this.gameID = gameID;
        this.playerName = playerName;
    }
}
