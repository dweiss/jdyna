package com.kdyna.corba;


import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;

public class CorbaPlayerServant extends ICPlayerControllerPOA {

	final private ICPlayerControllerAdapter delegate;
	final private POA poa;

	public CorbaPlayerServant(IPlayerFactory factory, String name, POA poa) {
		this.poa = poa;
		delegate = new ICPlayerControllerAdapter(factory.getController(name));
	}

	public synchronized void onControllerSetup(ICControllerCallback callback) {
		delegate.onControllerSetup(callback);
	}

	public synchronized void onEnd(CGameResult gameResult) {
		try {
			poa.deactivate_object(poa.servant_to_id(this));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void onFrame(int frame, CGameEvent[] events) {
		delegate.onFrame(frame, events);
	}

	public synchronized void onStart(CBoardInfo bInfo, CPlayer[] p) {
		delegate.onStart(bInfo, p);
	}

}