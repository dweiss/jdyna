package org.jdyna.network.sockets.packets;

import java.io.Serializable;

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

    protected CreateGameRequest()
    {
        // Serialization.
    }
    
    public CreateGameRequest(String gameName, String boardName)
    {
        this.gameName = gameName;
        this.boardName = boardName;
    }
}
