package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import org.jdyna.GameConfiguration;

/**
 * Start a new game room. 
 */
public class CreateGameRequest implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Board name or <code>null</code> if the default should be used. */
    public String boardName;

    /** Game name. */
    public String gameName;
    
    /** Game configuration. */
    public GameConfiguration conf;

    protected CreateGameRequest()
    {
        // Serialization.
    }
    
    public CreateGameRequest(GameConfiguration conf, String gameName, String boardName)
    {
        this.conf = conf;
        this.gameName = gameName;
        this.boardName = boardName;
    }
}
