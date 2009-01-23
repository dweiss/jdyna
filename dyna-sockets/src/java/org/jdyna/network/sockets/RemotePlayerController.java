package org.jdyna.network.sockets;

import com.dawidweiss.dyna.IPlayerController;

/*
 * 
 */
class RemotePlayerController implements IPlayerController
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
