package com.jdyna.players.jedrzejczaknowak;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerOperations;

/**
 * Simple decorator for {@link ICPlayerControllerAdapter} that adds POA cleanup
 * functionality.
 */
public class ICPlayerControllerAdapterDecorator extends Servant implements
        ICPlayerControllerOperations {
    private final ICPlayerControllerAdapter component;

    public ICPlayerControllerAdapterDecorator(IPlayerController controller) {
        component = new ICPlayerControllerAdapter(controller);
    }

    @Override
    public void onControllerSetup(ICControllerCallback arg0) {
        component.onControllerSetup(arg0);

    }

    @Override
    public void onEnd(CGameResult arg0) {
        component.onEnd(arg0);
        this._this_object()._release();
    }

    @Override
    public void onFrame(int arg0, CGameEvent[] arg1) {
        component.onFrame(arg0, arg1);

    }

    @Override
    public void onStart(CBoardInfo arg0, CPlayer[] arg1) {
        component.onStart(arg0, arg1);

    }

    @Override
    public String[] _all_interfaces(POA poa, byte[] objectId) {
        return component._all_interfaces(poa, objectId);
    }

}
