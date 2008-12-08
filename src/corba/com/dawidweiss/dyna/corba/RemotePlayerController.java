package com.dawidweiss.dyna.corba;

import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/**
 * A local adapter for remote Corba player controller.
 */
class RemotePlayerController extends Thread implements IController
{
    private final ICPlayerController remote;
    private CControllerState state;

    public RemotePlayerController(PlayerData pd)
    {
        this.remote = pd.controller;
    }
    
    @Override
    public void run()
    {
        try
        {
            while (!interrupted())
            {
                final CControllerState state = this.remote.state();
                this.state = state;

                // TODO: This should be correlated with the game's frame rate.
                Thread.sleep(250);
            }
        }
        catch (Exception e)
        {
            // Ignore, exit.
        }
    }

    @Override
    public synchronized boolean dropsBomb()
    {
        return state != null && state.dropsBomb;
    }

    @Override
    public synchronized Direction getCurrent()
    {
        return state == null ? null : Adapters.adapt(state.direction);
    }
}
