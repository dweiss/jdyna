package org.jdyna.network.sockets;

import java.io.Serializable;

/**
 * Unique player identifier, game identifier and other player-related information. Player
 * names should be unique within one game.
 */
public final class PlayerHandle implements Serializable
{
    /**  */
    private static final long serialVersionUID = 1L;

    public String playerName;
    public int gameID;
    public int playerID;

    /**
     * Player controller (only on the server side).
     */
    transient RemotePlayerControllerState controller = new RemotePlayerControllerState();

    /**
     * Player address (only on the server side).
     */
    transient String address;

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
