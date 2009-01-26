package com.dawidweiss.dyna;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

/**
 * A comparable snapshot state of {@link IPlayerController}.
 */
@SuppressWarnings("serial")
public final class ControllerState implements Serializable
{
    /**
     * A public 'do-nothing' constant state.
     */
    public static final ControllerState DO_NOTHING = new ControllerState(null, false, 0);

    public final IPlayerController.Direction direction;
    public final boolean dropsBomb;

    /**
     * Number of frames this state should be valid for. If zero, the state is valid
     * indefinitely.
     */
    public int validFrames;

    /**
     * Create a controller state valid indefinitely.
     */
    public ControllerState(IPlayerController.Direction direction, boolean dropsBomb)
    {
        this(direction, dropsBomb, 0);
    }

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
        return other.dropsBomb == this.dropsBomb && other.validFrames == this.validFrames
            && ObjectUtils.equals(other.direction, this.direction);
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        return "dropsBomb: " + dropsBomb + " direction: " + direction + " valid: "
            + (validFrames == 0 ? "indefinite" : Integer.toString(validFrames));
    }
}
