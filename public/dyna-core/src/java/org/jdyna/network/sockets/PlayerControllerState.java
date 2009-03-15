package org.jdyna.network.sockets;

import org.jdyna.ControllerState;
import org.jdyna.IPlayerController;

/**
 * Remote {@link IPlayerController} state used by the game thread and by the server-side.
 */
public final class PlayerControllerState implements IPlayerController
{
    private volatile ControllerState state = ControllerState.DO_NOTHING;

    /*
     * 
     */
    @Override
    public boolean dropsBomb()
    {
        return state.dropsBomb;
    }

    /*
     * 
     */
    @Override
    public Direction getCurrent()
    {
        return state.direction;
    }

    /**
     * State update invoked on each frame. 
     */
    public void update(ControllerState newState)
    {
        if (newState != null)
        {
            this.state = new ControllerState(newState.direction, newState.dropsBomb, newState.validFrames);
        }
        else
        {
            if (state.validFrames > 0)
            {
                if (--state.validFrames == 0)
                {
                    // Reset controller state.
                    state = ControllerState.DO_NOTHING;
                }
            }
        }
    }
}
