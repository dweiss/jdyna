package com.dawidweiss.dyna.corba.server;

import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.corba.Adapters;
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

    public boolean dropsBomb()
    {
        return dropsBomb;
    }

    public Direction getCurrent()
    {
        return current;
    }

    public void update(CControllerState state)
    {
        this.dropsBomb = state.dropsBomb;
        this.current = Adapters.adapt(state.direction);
    }
}
