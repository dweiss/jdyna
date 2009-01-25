package org.jdyna.network.sockets;

import java.io.Serializable;

import com.dawidweiss.dyna.BoardInfo;

/**
 * An identifier and associated information about a game running on the server.
 */
@SuppressWarnings("serial")
public final class GameHandle implements Serializable
{
    /** Board information. */
    public BoardInfo info;

    /** Game name. */
    public String gameName;
    
    /**
     * Unique game identifier. 
     */
    public int gameID;

    /*
     * For serialization.
     */
    protected GameHandle()
    {
    }

    /*
     * 
     */
    public GameHandle(int id, String gameName, BoardInfo info)
    {
        this.info = info;
        this.gameName = gameName;
        this.gameID = id;
    }
}
