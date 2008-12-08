package com.dawidweiss.dyna.corba;

import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallbackPOA;

/**
 * A game controller receiving and storing callback calls from a singleplayer.
 */
final class PlayerCallbackServant extends ICControllerCallbackPOA
    implements IController
{
    private volatile boolean dropsBomb;
    private volatile Direction current;

    @Override
    public boolean dropsBomb()
    {
        return dropsBomb;
    }

    @Override
    public Direction getCurrent()
    {
        return current;
    }

    @Override
    public void update(CControllerState state)
    {
        this.dropsBomb = state.dropsBomb;
        this.current = Adapters.adapt(state.direction);
    }
}
