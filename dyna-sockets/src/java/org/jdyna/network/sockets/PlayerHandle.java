package org.jdyna.network.sockets;

import java.io.Serializable;

public class PlayerHandle implements Serializable
{
    /**  */
    private static final long serialVersionUID = 1L;

    public String playerName;
    public int gameID;
    public int playerID;

    /**
     * Player controller (only on the server side).
     */
    transient RemotePlayerController controller = new RemotePlayerController();

    /*
     * Serialization. 
     */
    protected PlayerHandle()
    {
    }

    /*
     * 
     */
    public PlayerHandle(int playerID, int gameID, String playerName)
    {
        this.playerID = playerID;
        this.gameID = gameID;
        this.playerName = playerName;
    }
}
