package org.jdyna.network.sockets;

import java.io.Serializable;

import org.jdyna.BoardInfo;
import org.jdyna.GameConfiguration;


/**
 * An identifier and associated information about a game running on the server.
 */
@SuppressWarnings("serial")
public final class GameHandle implements Serializable
{
    /** Board information. */
    public BoardInfo info;
    
    /** Configuration. */
    public GameConfiguration conf;

    /** Game name. */
    public String gameName;
    
    /**
     * Unique game identifier. 
     */
    public int gameID;

    /**
     * Board name for the game.
     */
    public String boardName;

    /*
     * For serialization.
     */
    protected GameHandle()
    {
    }

    /*
     * 
     */
    public GameHandle(int id, String gameName, String boardName, BoardInfo info, GameConfiguration conf)
    {
        this.info = info;
        this.gameName = gameName;
        this.gameID = id;
        this.boardName = boardName;
        this.conf = conf;
    }
}
