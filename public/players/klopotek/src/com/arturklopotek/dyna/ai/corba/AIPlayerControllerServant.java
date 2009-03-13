package com.arturklopotek.dyna.ai.corba;

import org.jdyna.IPlayerController;

import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;

/**
 * An adapter for {@link IPlayerController} updating controller state. Utilizes
 * {@link ICPlayerControllerAdapter} but performs some extra stuff (clean-up at 
 * the end of the game)
 */
public class AIPlayerControllerServant extends ICPlayerControllerPOA
{

    private final ICPlayerControllerPOA delegate;

    public void onControllerSetup(ICControllerCallback callback)
    {
        delegate.onControllerSetup(callback);
    }

    public void onEnd(CGameResult result)
    {
        delegate.onEnd(result);

        try
        {
            // deactivate itself
            _poa().deactivate_object(_object_id());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void onFrame(int frame, CGameEvent [] events)
    {
        delegate.onFrame(frame, events);
    }

    public void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        delegate.onStart(boardInfo, players);
    }

    public AIPlayerControllerServant(IPlayerController controller)
    {
        delegate = new ICPlayerControllerAdapter(controller);
    }
}
