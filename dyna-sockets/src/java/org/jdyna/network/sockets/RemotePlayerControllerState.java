package org.jdyna.network.sockets;

import com.dawidweiss.dyna.IPlayerController;

/**
 * Remote {@link IPlayerController} state used by the game thread and by the server-side.
 */
final class RemotePlayerControllerState implements IPlayerController
{
    private volatile boolean dropsBombs;
    private volatile Direction direction;
    int validFrames;

    /*
     * 
     */
    @Override
    public boolean dropsBomb()
    {
        return dropsBombs;
    }

    /*
     * 
     */
    @Override
    public Direction getCurrent()
    {
        return direction;
    }

    /*
     * 
     */
    void update(Direction direction, boolean dropsBombs, int validFrames)
    {
        this.dropsBombs = dropsBombs;
        this.direction = direction;
        this.validFrames = validFrames;
    }
}
