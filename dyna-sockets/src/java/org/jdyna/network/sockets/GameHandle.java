package org.jdyna.network.sockets;

import java.io.Serializable;

import com.dawidweiss.dyna.BoardInfo;

/**
 * 
 */
public final class GameHandle implements Serializable
{
    /**  */
    private static final long serialVersionUID = -1280754990692517130L;

    public BoardInfo info;
    public String gameName;
    public int id;

    /*
     * Serialization. 
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
        this.id = id;
    }
}
