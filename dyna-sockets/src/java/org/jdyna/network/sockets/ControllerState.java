package org.jdyna.network.sockets;

import org.apache.commons.lang.ObjectUtils;

import com.dawidweiss.dyna.IPlayerController;

/**
 * A comparable state of {@link IPlayerController}.
 */
public final class ControllerState
{
    final IPlayerController.Direction direction;
    final boolean dropsBomb;

    /**
     * Number of frames this state should be valid for. If zero, the state is valid
     * indefinitely.
     */
    final int validFrames;

    /*
     * 
     */
    public ControllerState(IPlayerController.Direction direction, boolean dropsBomb,
        int validFrames)
    {
        this.direction = direction;
        this.dropsBomb = dropsBomb;
        this.validFrames = validFrames;
    }

    /*
     * 
     */
    @Override
    public int hashCode()
    {
        return (dropsBomb ? 1 : -1) ^ (direction == null ? 0 : direction.hashCode());
    }

    /*
     * 
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof ControllerState)) return false;

        final ControllerState other = (ControllerState) obj;
        return other.dropsBomb == this.dropsBomb
            && ObjectUtils.equals(other.direction, this.direction);
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        return "dropsBomb: " + dropsBomb + " direction: " + direction + " valid: " + validFrames;
    }
}
