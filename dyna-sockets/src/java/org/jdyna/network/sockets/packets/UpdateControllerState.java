package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import com.dawidweiss.dyna.ControllerState;

/**
 *  A message indicating change in the controller state.
 */
@SuppressWarnings("serial")
public class UpdateControllerState implements Serializable
{
    /** Game identifier. */
    public int gameID;

    /** Player identifier */
    public int playerID;

    /**
     * Controller state.
     */
    public ControllerState state;

    /*
     * 
     */
    protected UpdateControllerState()
    {
        // Serialization.
    }

    /*
     * 
     */
    public UpdateControllerState(int gameID, int playerID, ControllerState state)
    {
        this.gameID = gameID;
        this.playerID = playerID;
        this.state = state;
    }
}
