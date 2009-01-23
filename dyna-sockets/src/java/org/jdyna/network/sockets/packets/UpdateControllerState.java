package org.jdyna.network.sockets.packets;

import java.io.Serializable;

import com.dawidweiss.dyna.IPlayerController;

/**
 *  
 */
public class UpdateControllerState implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Game identifier. */
    public int gameID;

    /** Player identifier */
    public int playerID;

    public IPlayerController.Direction direction;
    public boolean dropsBomb;

    /**
     * How many frames this controller change should be valid for? If non-zero, the state
     * will be applied only to this number of consecutive frames and reset then (unless
     * the state is changed before by another control packet).
     */
    public int validFrames;

    protected UpdateControllerState()
    {
        // Serialization.
    }

    public UpdateControllerState(int gameID, int playerID,
        IPlayerController.Direction direction, boolean dropsBomb, int validFrames)
    {
        this.gameID = gameID;
        this.playerID = playerID;
        this.direction = direction;
        this.dropsBomb = dropsBomb;
        this.validFrames = validFrames;
    }
}
