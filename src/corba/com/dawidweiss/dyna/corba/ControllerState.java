package com.dawidweiss.dyna.corba;

import org.apache.commons.lang.ObjectUtils;

import com.dawidweiss.dyna.IController;

/**
 * A comparable state of {@link IController}.
 */
public final class ControllerState
{
    private final IController.Direction direction;
    private final boolean dropsBomb;

    public ControllerState(IController c)
    {
        this.direction = c.getCurrent();
        this.dropsBomb = c.dropsBomb();
    }

    @Override
    public int hashCode()
    {
        return (dropsBomb ? 1 : -1) ^ (direction == null ? 0 : direction.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof ControllerState)) return false;
        
        final ControllerState other = (ControllerState) obj;
        return other.dropsBomb == this.dropsBomb
            && ObjectUtils.equals(other.direction, this.direction);
    }
}
